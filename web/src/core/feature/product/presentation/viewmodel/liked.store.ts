/**
 * Likes locales de productos (solo device).
 * Key alineada para futura paridad con Android DataStore/SharedPreferences.
 * No toca backend ni tipos de dominio.
 */
import { writable, get } from "svelte/store";

export const LIKED_PRODUCTS_STORAGE_KEY = "alejo_liked_product_ids";

function readIds(): string[] {
    if (typeof localStorage === "undefined") return [];
    try {
        const raw = localStorage.getItem(LIKED_PRODUCTS_STORAGE_KEY);
        if (!raw) return [];
        const parsed = JSON.parse(raw);
        if (!Array.isArray(parsed)) return [];
        return parsed.map((x) => String(x)).filter(Boolean);
    } catch {
        return [];
    }
}

function writeIds(ids: string[]) {
    if (typeof localStorage === "undefined") return;
    try {
        localStorage.setItem(LIKED_PRODUCTS_STORAGE_KEY, JSON.stringify(ids));
    } catch {
        /* quota / private mode */
    }
}

function createLikedStore() {
    const { subscribe, set, update } = writable<string[]>(readIds());

    return {
        subscribe,
        isLiked(productId: string): boolean {
            const id = String(productId || "").trim();
            if (!id) return false;
            return get({ subscribe }).includes(id);
        },
        toggle(productId: string): boolean {
            const id = String(productId || "").trim();
            if (!id) return false;
            let nowLiked = false;
            update((ids) => {
                const setIds = new Set(ids);
                if (setIds.has(id)) {
                    setIds.delete(id);
                    nowLiked = false;
                } else {
                    setIds.add(id);
                    nowLiked = true;
                }
                const next = Array.from(setIds);
                writeIds(next);
                return next;
            });
            return nowLiked;
        },
        like(productId: string) {
            const id = String(productId || "").trim();
            if (!id) return;
            update((ids) => {
                if (ids.includes(id)) return ids;
                const next = [...ids, id];
                writeIds(next);
                return next;
            });
        },
        unlike(productId: string) {
            const id = String(productId || "").trim();
            if (!id) return;
            update((ids) => {
                const next = ids.filter((x) => x !== id);
                writeIds(next);
                return next;
            });
        },
        hydrate() {
            set(readIds());
        },
    };
}

export const likedStore = createLikedStore();

export function isProductLiked(productId: string, likedIds: string[]): boolean {
    const id = String(productId || "").trim();
    return id !== "" && likedIds.includes(id);
}
