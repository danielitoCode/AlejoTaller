import { derived, writable } from "svelte/store";
import { supportContainer } from "../../di/support.container";
import type {
    SupportChatMessage,
    SupportMessage,
    SupportReason
} from "../../domain/entity/SupportMessage";
import { sessionStore } from "../../../auth/presentation/viewmodel/session.store";

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
            await syncMine();
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
        if (!text) throw new Error("Escribe un mensaje");
        update((s) => ({ ...s, posting: true, error: null }));
        try {
            const user = await sessionStore.getCurrentUser();
            await supportContainer.useCases.postMessage({
                threadId,
                senderRole: "user",
                senderId: user.$id,
                senderName: user.name || "Usuario",
                body: text
            });
            await loadMessages(threadId);
            await syncMine();
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
                syncMine().catch(() => {});
                if (activeId) loadMessages(activeId).catch(() => {});
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
        createThread,
        postUserReply,
        clearActive,
        startRealtime,
        stopRealtime,
        unread
    };
}

export const supportInboxStore = createStore();
