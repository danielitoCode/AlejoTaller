import { derived, writable } from "svelte/store";
import { supportContainer } from "../../di/support.container";
import type {
    SupportChatMessage,
    SupportMessage,
    SupportReason,
    SupportSenderRole
} from "../../domain/entity/SupportMessage";
import type { SupportRealtimeEvent } from "../../domain/repository/support.repository";
import { asSenderRole, asStatus } from "../../data/mapper/Mappers";
import { sessionStore } from "../../../auth/presentation/viewmodel/session.store";
import { logger } from "../../../../infrastructure/presentation/util/logger.service";
import { isAppwriteDataStackDisabled } from "../../../../infrastructure/platform.flags";

type State = {
    items: SupportMessage[];
    loading: boolean;
    error: string | null;
    activeThreadId: string | null;
    messages: SupportChatMessage[];
    messagesLoading: boolean;
    posting: boolean;
    creating: boolean;
    userId: string | null;
};

const initial: State = {
    items: [],
    loading: false,
    error: null,
    activeThreadId: null,
    messages: [],
    messagesLoading: false,
    posting: false,
    creating: false,
    userId: null
};

function normalizeError(error: unknown): string {
    return error instanceof Error ? error.message : "Error inesperado";
}

/** Legacy name: true si el stack Appwrite está desconectado (Clerk + Turso). */
function isAuth0(): boolean {
    return isAppwriteDataStackDisabled();
}

function asString(value: unknown, fallback = ""): string {
    if (typeof value === "string" && value.trim()) return value.trim();
    if (typeof value === "number" || typeof value === "boolean") return String(value);
    return fallback;
}

function createStore() {
    const { subscribe, update } = writable<State>(initial);
    let unsubRt: (() => void) | null = null;
    let syncTimer: number | null = null;
    let rtRefCount = 0;

    async function ensureUserId(): Promise<string> {
        const user = await sessionStore.getCurrentUser();
        const id =
            asString((user as Record<string, unknown>).$id) ||
            asString((user as Record<string, unknown>).id) ||
            asString((user as Record<string, unknown>).sub);
        if (!id) throw new Error("No hay usuario de sesión");
        update((s) => ({ ...s, userId: id }));
        return id;
    }

    async function syncMine(): Promise<void> {
        if (isAuth0()) {
            update((s) => ({ ...s, items: [], loading: false, error: null }));
            return;
        }
        update((s) => ({ ...s, loading: true, error: null }));
        try {
            const userId = await ensureUserId();
            const items = await supportContainer.useCases.listMine(userId);
            update((s) => ({ ...s, items }));
        } catch (e) {
            update((s) => ({ ...s, error: normalizeError(e) }));
            throw e;
        } finally {
            update((s) => ({ ...s, loading: false }));
        }
    }

    async function loadMessages(threadId: string): Promise<void> {
        if (isAuth0()) {
            update((s) => ({
                ...s,
                activeThreadId: threadId,
                messages: [],
                messagesLoading: false,
                error: null
            }));
            return;
        }
        update((s) => ({
            ...s,
            activeThreadId: threadId,
            messagesLoading: true,
            error: null
        }));
        try {
            const messages = await supportContainer.useCases.listMessages(threadId);
            update((s) => ({ ...s, messages }));
        } catch (e) {
            update((s) => ({ ...s, error: normalizeError(e), messages: [] }));
            throw e;
        } finally {
            update((s) => ({ ...s, messagesLoading: false }));
        }
    }

    async function markUserRead(threadId: string): Promise<void> {
        if (isAuth0()) return;

        const id = threadId?.trim();
        if (!id) {
            logger.warn("[support] markUserRead: threadId vacío");
            return;
        }

        let alreadyRead = false;
        const unsub = subscribe((s) => {
            const row = s.items.find((m) => m.id === id);
            alreadyRead = !row || (row.unreadUser ?? 0) === 0;
        });
        unsub();
        if (alreadyRead) return;

        try {
            await supportContainer.useCases.markRead(id, "user");
            update((s) => ({
                ...s,
                items: s.items.map((m) =>
                    m.id === id ? { ...m, unreadUser: 0 } : m
                )
            }));
        } catch (e) {
            logger.warn(
                `[support] markUserRead falló id=${id}: ${normalizeError(e)}`
            );
        }
    }

    function applyLocalAfterUserPost(threadId: string, body: string, atIso: string) {
        const preview = body.length > 180 ? `${body.slice(0, 177)}…` : body;
        update((s) => ({
            ...s,
            items: s.items.map((m) =>
                m.id === threadId
                    ? {
                          ...m,
                          body: preview,
                          createdAtIso: atIso,
                          lastSenderRole: "user" as const,
                          unreadUser: 0
                      }
                    : m
            )
        }));
    }

    async function createThread(input: {
        reason: SupportReason;
        subject: string;
        body: string;
    }): Promise<string> {
        if (isAuth0()) {
            throw new Error("Soporte aún no migrado (modo sin Appwrite)");
        }
        update((s) => ({ ...s, creating: true, error: null }));
        try {
            const user = await sessionStore.getCurrentUser();
            const u = user as Record<string, unknown>;
            const result = await supportContainer.useCases.create({
                userId: asString(u.$id) || asString(u.id),
                userName: asString(u.name) || "Usuario",
                userEmail: asString(u.email),
                reason: input.reason,
                subject: input.subject,
                body: input.body
            });
            try {
                await syncMine();
            } catch (e) {
                logger.warn(`[support] syncMine post-create: ${normalizeError(e)}`);
            }
            return result.thread.id;
        } catch (e) {
            update((s) => ({ ...s, error: normalizeError(e) }));
            throw e;
        } finally {
            update((s) => ({ ...s, creating: false }));
        }
    }

    async function postUserReply(threadId: string, body: string): Promise<void> {
        if (isAuth0()) {
            throw new Error("Soporte aún no migrado (modo sin Appwrite)");
        }

        const text = body.trim();
        const id = threadId?.trim();
        if (!text) throw new Error("Escribe un mensaje");
        if (!id) throw new Error("Consulta inválida");

        update((s) => ({ ...s, posting: true, error: null }));
        try {
            const user = await sessionStore.getCurrentUser();
            const u = user as Record<string, unknown>;
            const msg = await supportContainer.useCases.postMessage({
                threadId: id,
                senderRole: "user",
                senderId: asString(u.$id) || asString(u.id),
                senderName: asString(u.name) || "Usuario",
                body: text
            });

            applyLocalAfterUserPost(id, text, msg.createdAtIso || new Date().toISOString());
            update((s) => ({
                ...s,
                messages:
                    s.activeThreadId === id
                        ? [...s.messages.filter((m) => m.id !== msg.id), msg]
                        : s.messages
            }));

            try {
                await loadMessages(id);
            } catch (e) {
                logger.warn(`[support] loadMessages post-reply: ${normalizeError(e)}`);
            }
            try {
                await syncMine();
            } catch (e) {
                logger.warn(`[support] syncMine post-reply: ${normalizeError(e)}`);
            }
            void markUserRead(id);
        } catch (e) {
            update((s) => ({ ...s, error: normalizeError(e) }));
            throw e;
        } finally {
            update((s) => ({ ...s, posting: false }));
        }
    }

    function clearActive(): void {
        update((s) => ({
            ...s,
            activeThreadId: null,
            messages: [],
            messagesLoading: false
        }));
    }

    function applyThreadPayload(payload: Record<string, unknown> | null | undefined): void {
        if (!payload) return;
        const id = String(payload.$id ?? payload.id ?? "").trim();
        if (!id) return;

        const hasStatus = "status" in payload;
        const hasPreview = "lastPreview" in payload;
        const hasLastAt = "lastMessageAt" in payload;
        const hasSender = "lastSenderRole" in payload;
        const hasUnreadUser = "unreadUser" in payload;
        if (!hasStatus && !hasPreview && !hasLastAt && !hasSender && !hasUnreadUser) {
            return;
        }

        update((s) => ({
            ...s,
            items: s.items.map((m) => {
                if (m.id !== id) return m;
                const next: SupportMessage = { ...m };
                if (hasStatus) {
                    next.status = asStatus(payload.status);
                }
                if (hasPreview) {
                    next.body = String(payload.lastPreview ?? m.body ?? "");
                }
                if (hasLastAt) {
                    next.createdAtIso = String(
                        payload.lastMessageAt ?? m.createdAtIso ?? ""
                    );
                }
                if (hasSender) {
                    next.lastSenderRole = asSenderRole(
                        payload.lastSenderRole
                    ) as SupportSenderRole;
                }
                if (hasUnreadUser) {
                    const n = Number(payload.unreadUser);
                    next.unreadUser = Number.isFinite(n) ? Math.max(0, Math.floor(n)) : 0;
                }
                return next;
            })
        }));
    }

    function onRealtimeEvent(evt: SupportRealtimeEvent): void {
        logger.info(
            `[support] RT → target=${evt.target ?? "unknown"} status=${
                evt.payload && "status" in evt.payload
                    ? String(evt.payload.status)
                    : "-"
            }`
        );

        if (evt.target === "threads") {
            applyThreadPayload(evt.payload);
        }

        if (syncTimer) window.clearTimeout(syncTimer);
        syncTimer = window.setTimeout(() => {
            let activeId: string | null = null;
            const u = subscribe((s) => {
                activeId = s.activeThreadId;
            });
            u();
            syncMine().catch((e) => {
                logger.warn(`[support] RT syncMine: ${normalizeError(e)}`);
            });
            if (activeId && (evt.target === "messages" || evt.target === "threads")) {
                loadMessages(activeId).catch((e) => {
                    logger.warn(`[support] RT loadMessages: ${normalizeError(e)}`);
                });
            }
        }, 250);
    }

    function startRealtime(): () => void {
        if (isAuth0()) {
            return () => {};
        }

        rtRefCount += 1;
        if (rtRefCount === 1 && !unsubRt) {
            unsubRt = supportContainer.useCases.subscribe(onRealtimeEvent);
        }
        return () => {
            releaseRealtime();
        };
    }

    function releaseRealtime(): void {
        rtRefCount = Math.max(0, rtRefCount - 1);
        if (rtRefCount > 0) return;
        if (syncTimer) {
            window.clearTimeout(syncTimer);
            syncTimer = null;
        }
        if (unsubRt) {
            try {
                unsubRt();
            } catch {
                /* ignore */
            }
            unsubRt = null;
        }
    }

    function stopRealtime(): void {
        rtRefCount = 0;
        if (syncTimer) {
            window.clearTimeout(syncTimer);
            syncTimer = null;
        }
        if (unsubRt) {
            try {
                unsubRt();
            } catch {
                /* ignore */
            }
            unsubRt = null;
        }
    }

    const unread = derived({ subscribe }, ($s) =>
        $s.items.reduce((acc, m) => acc + (m.unreadUser ?? 0), 0)
    );

    return {
        subscribe,
        syncMine,
        loadMessages,
        markUserRead,
        createThread,
        postUserReply,
        clearActive,
        startRealtime,
        stopRealtime,
        unread
    };
}

export const supportInboxStore = createStore();
