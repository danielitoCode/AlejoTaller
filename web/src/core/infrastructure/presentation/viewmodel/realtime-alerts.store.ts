import {derived, writable} from "svelte/store";

export type RealtimeAlertKind = "support" | "sales";

export type RealtimeAlert = {
    id: string;
    kind: RealtimeAlertKind;
    createdAtIso: string;
    title: string;
    detail: string;
    entityId: string | null;
    read: boolean;
};

type AlertsState = {
    items: RealtimeAlert[];
};

const initialState: AlertsState = { items: [] };

function nowIso(): string {
    return new Date().toISOString();
}

function alertId(): string {
    return `${Date.now()}-${Math.floor(Math.random() * 1e6)}`;
}

function createRealtimeAlertsStore() {
    const { subscribe, update, set } = writable<AlertsState>(initialState);

    function reset(): void {
        set(initialState);
    }

    function add(kind: RealtimeAlertKind, title: string, detail: string, entityId?: string | null): void {
        const createdAtIso = nowIso();

        update((s) => {
            const recent = s.items.slice(0, 12);
            const isDup = recent.some(
                (a) =>
                    a.kind === kind &&
                    (a.entityId ?? null) === (entityId ?? null) &&
                    a.title === title &&
                    Date.parse(createdAtIso) - Date.parse(a.createdAtIso) < 30_000
            );
            if (isDup) return s;

            const next: RealtimeAlert = {
                id: alertId(),
                kind,
                createdAtIso,
                title,
                detail,
                entityId: entityId ?? null,
                read: false
            };

            return { ...s, items: [next, ...s.items].slice(0, 50) };
        });
    }

    function markRead(id: string): void {
        update((s) => ({
            ...s,
            items: s.items.map((a) => (a.id === id ? { ...a, read: true } : a))
        }));
    }

    function markAllRead(kind: RealtimeAlertKind): void {
        update((s) => ({
            ...s,
            items: s.items.map((a) => (a.kind === kind ? { ...a, read: true } : a))
        }));
    }

    function clear(kind?: RealtimeAlertKind): void {
        update((s) => ({
            ...s,
            items: kind ? s.items.filter((a) => a.kind !== kind) : []
        }));
    }

    const counts = derived({ subscribe }, ($s) => {
        const supportUnread = $s.items.filter((a) => a.kind === "support" && !a.read).length;
        const salesUnread = $s.items.filter((a) => a.kind === "sales" && !a.read).length;
        return { supportUnread, salesUnread, totalUnread: supportUnread + salesUnread };
    });

    const supportItems = derived({ subscribe }, ($s) => $s.items.filter((a) => a.kind === "support"));
    const salesItems = derived({ subscribe }, ($s) => $s.items.filter((a) => a.kind === "sales"));

    return { subscribe, reset, add, markRead, markAllRead, clear, counts, supportItems, salesItems };
}

export const realtimeAlertsStore = createRealtimeAlertsStore();

