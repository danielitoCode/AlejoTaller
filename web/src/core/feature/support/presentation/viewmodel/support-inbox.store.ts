import { derived, writable } from "svelte/store";
import type { SupportMessage, SupportThread } from "../../domain/entity/SupportMessage";
import { supportContainer } from "../../di/support.container";
import { sessionStore } from "../../../auth/presentation/viewmodel/session.store";
import { logger } from "../../../../infrastructure/presentation/util/logger.service";
import {
    type SupportRealtimeEvent,
    subscribeSupportRealtime
} from "../../../../infrastructure/data/realtime/support-realtime";

interface SupportInboxState {
    items: SupportThread[];
    messages: SupportMessage[];
    activeThreadId: string | null;
    userId: string | null;
    loading: boolean;
    messagesLoading: boolean;
    sending: boolean;
    error: string | null;
}

const initialState: SupportInboxState = {
    items: [],
    messages: [],
    activeThreadId: null,
    userId: null,
    loading: false,
    messagesLoading: false,
    sending: false,
    error: null
};

function normalizeError(error: unknown): string {
    return error instanceof Error ? error.message : "Unexpected error";
}

function isAuth0(): boolean {
    return String((import.meta as any).env?.VITE_AUTH_PROVIDER || "").toLowerCase() === "auth0";
}

function createStore() {
    const { subscribe, update, set } = writable<SupportInboxState>(initialState);
    let realtimeRefCount = 0;
    let stopRealtimeInternal: (() => void) | null = null;

    async function ensureUserId(): Promise<string> {
        const user = await sessionStore.getCurrentUser();
        const id = String((user as any).$id || (user as any).id || "");
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
            update((s) => ({ ...s, activeThreadId: threadId, messages: [], messagesLoading: false }));
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
            update((s) => ({ ...s, error: normalizeError(e) }));
            throw e;
        } finally {
            update((s) => ({ ...s, messagesLoading: false }));
        }
    }

    async function markUserRead(threadId: string): Promise<void> {
        if (isAuth0()) return;
        try {
            await supportContainer.useCases.markUserRead(threadId);
            update((s) => ({
                ...s,
                items: s.items.map((t) =>
                    t.id === threadId ? { ...t, unreadUser: 0 } : t
                )
            }));
        } catch (e) {
            logger.warn(`[support] markUserRead: ${normalizeError(e)}`);
        }
    }

    function applyLocalAfterUserPost(threadId: string, body: string, atIso: string) {
        update((s) => ({
            ...s,
            messages: [
                ...s.messages,
                {
                    id: `local-${Date.now()}`,
                    threadId,
                    body,
                    senderId: s.userId || "",
                    isStaff: false,
                    createdAt: atIso
                } as SupportMessage
            ]
        }));
    }

    async function createThread(input: {
        subject?: string;
        body: string;
    }): Promise<void> {
        if (isAuth0()) {
            throw new Error("Soporte aún no migrado fuera de Appwrite (Auth0 mode)");
        }
        update((s) => ({ ...s, sending: true, error: null }));
        try {
            const userId = await ensureUserId();
            await supportContainer.useCases.createThread({ ...input, userId });
            try {
                await syncMine();
            } catch (e) {
                logger.warn(`[support] syncMine post-create: ${normalizeError(e)}`);
            }
        } catch (e) {
            update((s) => ({ ...s, error: normalizeError(e) }));
            throw e;
        } finally {
            update((s) => ({ ...s, sending: false }));
        }
    }

    async function postUserReply(threadId: string, body: string): Promise<void> {
        if (isAuth0()) {
            throw new Error("Soporte aún no migrado fuera de Appwrite (Auth0 mode)");
        }
        update((s) => ({ ...s, sending: true, error: null }));
        try {
            await supportContainer.useCases.postUserReply(threadId, body);
            applyLocalAfterUserPost(threadId, body, new Date().toISOString());
            try {
                await syncMine();
            } catch (e) {
                logger.warn(`[support] syncMine post-reply: ${normalizeError(e)}`);
            }
        } catch (e) {
            update((s) => ({ ...s, error: normalizeError(e) }));
            throw e;
        } finally {
            update((s) => ({ ...s, sending: false }));
        }
    }

    function clearActive(): void {
        update((s) => ({ ...s, activeThreadId: null, messages: [] }));
    }

    function applyThreadPayload(_payload: Record<string, unknown> | null | undefined): void {
        /* no-op simplified under Auth0 cut */
    }

    function onRealtimeEvent(evt: SupportRealtimeEvent): void {
        if (isAuth0()) return;
        syncMine().catch((e) => {
            logger.warn(`[support] RT syncMine: ${normalizeError(e)}`);
        });
    }

    function startRealtime(): () => void {
        if (isAuth0()) {
            return () => {};
        }
        realtimeRefCount += 1;
        if (realtimeRefCount === 1) {
            stopRealtimeInternal = subscribeSupportRealtime(onRealtimeEvent);
        }
        return () => {
            releaseRealtime();
        };
    }

    function releaseRealtime(): void {
        realtimeRefCount = Math.max(0, realtimeRefCount - 1);
        if (realtimeRefCount === 0 && stopRealtimeInternal) {
            stopRealtimeInternal();
            stopRealtimeInternal = null;
        }
    }

    function stopRealtime(): void {
        realtimeRefCount = 0;
        if (stopRealtimeInternal) {
            stopRealtimeInternal();
            stopRealtimeInternal = null;
        }
    }

    const hasData = derived({ subscribe }, ($s) => $s.items.length > 0);

    return {
        subscribe,
        hasData,
        syncMine,
        loadMessages,
        markUserRead,
        createThread,
        postUserReply,
        clearActive,
        startRealtime,
        stopRealtime
    };
}

export const supportInboxStore = createStore();
