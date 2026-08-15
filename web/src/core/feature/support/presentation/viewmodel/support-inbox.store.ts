import { derived, writable } from "svelte/store";
import { supportContainer } from "../../di/support.container";
import type {
    SupportChatMessage,
    SupportMessage,
    SupportReason
} from "../../domain/entity/SupportMessage";
import { sessionStore } from "../../../auth/presentation/viewmodel/session.store";
import { logger } from "../../../../infrastructure/presentation/util/logger.service";

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

function createStore() {
    const { subscribe, update } = writable<State>(initial);
    let unsubRt: (() => void) | null = null;
    let syncTimer: number | null = null;

    async function ensureUserId(): Promise<string> {
        const user = await sessionStore.getCurrentUser();
        const id = user.$id;
        update((s) => ({ ...s, userId: id }));
        return id;
    }

    async function syncMine(): Promise<void> {
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

    /**
     * Marca el hilo como leído por el usuario (unreadUser = 0).
     * Best-effort: solo actualiza local si Appwrite confirma.
     */
    async function markUserRead(threadId: string): Promise<void> {
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

    /** Actualización local del inbox tras enviar (preview + contadores). */
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
                          // El usuario acaba de hablar: sus no-leídos bajan a 0 localmente
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
        update((s) => ({ ...s, creating: true, error: null }));
        try {
            const user = await sessionStore.getCurrentUser();
            const result = await supportContainer.useCases.create({
                userId: user.$id,
                userName: user.name || "Usuario",
                userEmail: user.email || "",
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
        const text = body.trim();
        const id = threadId?.trim();
        if (!text) throw new Error("Escribe un mensaje");
        if (!id) throw new Error("Consulta inválida");

        update((s) => ({ ...s, posting: true, error: null }));
        try {
            const user = await sessionStore.getCurrentUser();
            const msg = await supportContainer.useCases.postMessage({
                threadId: id,
                senderRole: "user",
                senderId: user.$id,
                senderName: user.name || "Usuario",
                body: text
            });

            // Optimistic: fila inbox + append mensaje local
            applyLocalAfterUserPost(id, text, msg.createdAtIso || new Date().toISOString());
            update((s) => ({
                ...s,
                messages:
                    s.activeThreadId === id
                        ? [...s.messages.filter((m) => m.id !== msg.id), msg]
                        : s.messages
            }));

            // Refresh best-effort (no tumbar el envío si fallan)
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
            // Usuario acaba de ver/enviar: marcar leído best-effort
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

    function startRealtime(): () => void {
        stopRealtime();
        unsubRt = supportContainer.useCases.subscribe(() => {
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
                if (activeId) {
                    loadMessages(activeId).catch((e) => {
                        logger.warn(`[support] RT loadMessages: ${normalizeError(e)}`);
                    });
                }
            }, 250);
        });
        return stopRealtime;
    }

    function stopRealtime(): void {
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
