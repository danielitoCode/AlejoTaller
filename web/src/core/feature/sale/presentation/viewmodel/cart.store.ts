import { derived, get, writable } from "svelte/store";
import type { Product } from "../../../product/domain/entity/Product";
import { promotionStore } from "../../../notification/presentation/viewmodel/promotion.store";
import { effectivePrice } from "../../../notification/domain/policy/PromotionPolicy";
import { availableStock } from "../../../product/domain/entity/Product";

export interface CartItem {
    product: Product;
    quantity: number;
}

interface CartState {
    items: CartItem[];
}

/** Resultado de mutar cantidad: permite toast en UI. */
export type CartQtyResult =
    | { ok: true; quantity: number; max: number; clamped: boolean }
    | { ok: false; reason: "out_of_stock" | "not_found"; max: number };

const STORAGE_KEY = "alejo-taller-web-cart";

function maxQtyFor(product: Product): number {
    return availableStock(product);
}

function clampQty(product: Product, desired: number): number {
    const max = maxQtyFor(product);
    if (max <= 0) return 0;
    return Math.min(Math.max(0, Math.floor(desired)), max);
}

function normalizeItems(items: CartItem[]): CartItem[] {
    return items
        .map((item) => {
            const qty = clampQty(item.product, item.quantity);
            return { product: item.product, quantity: qty };
        })
        .filter((item) => item.quantity > 0);
}

function readInitialState(): CartState {
    if (typeof window === "undefined") return { items: [] };

    const raw = window.localStorage.getItem(STORAGE_KEY);
    if (!raw) return { items: [] };

    try {
        const parsed = JSON.parse(raw) as CartState;
        const items = Array.isArray(parsed.items) ? parsed.items : [];
        return { items: normalizeItems(items) };
    } catch {
        return { items: [] };
    }
}

function persist(state: CartState): void {
    if (typeof window === "undefined") return;
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
}

function createCartStore() {
    const { subscribe, update, set } = writable<CartState>(readInitialState());

    function commit(mutator: (state: CartState) => CartState): void {
        update((state) => {
            const next = mutator(state);
            persist(next);
            return next;
        });
    }

    function addProduct(product: Product, quantity = 1): CartQtyResult {
        const max = maxQtyFor(product);
        if (max <= 0) {
            return { ok: false, reason: "out_of_stock", max: 0 };
        }

        let result: CartQtyResult = { ok: true, quantity: 0, max, clamped: false };

        commit((state) => {
            const existing = state.items.find((item) => item.product.id === product.id);
            const currentQty = existing?.quantity ?? 0;
            const desired = currentQty + Math.max(1, Math.floor(quantity));
            const nextQty = clampQty(product, desired);
            const clamped = nextQty < desired;

            result = { ok: true, quantity: nextQty, max, clamped };

            if (nextQty <= 0) {
                return {
                    items: state.items.filter((item) => item.product.id !== product.id)
                };
            }

            if (existing) {
                return {
                    items: state.items.map((item) =>
                        item.product.id === product.id
                            ? { product, quantity: nextQty }
                            : item
                    )
                };
            }

            return {
                items: [...state.items, { product, quantity: nextQty }]
            };
        });

        return result;
    }

    function setQuantity(productId: string, quantity: number): CartQtyResult {
        let result: CartQtyResult = { ok: false, reason: "not_found", max: 0 };

        commit((state) => {
            const existing = state.items.find((item) => item.product.id === productId);
            if (!existing) {
                result = { ok: false, reason: "not_found", max: 0 };
                return state;
            }

            const max = maxQtyFor(existing.product);
            const desired = Math.floor(quantity);
            const nextQty = clampQty(existing.product, desired);
            const clamped = desired > max;

            result =
                nextQty <= 0 && desired > 0 && max <= 0
                    ? { ok: false, reason: "out_of_stock", max: 0 }
                    : { ok: true, quantity: nextQty, max, clamped };

            return {
                items: state.items
                    .map((item) =>
                        item.product.id === productId
                            ? { ...item, quantity: nextQty }
                            : item
                    )
                    .filter((item) => item.quantity > 0)
            };
        });

        return result;
    }

    function remove(productId: string): void {
        commit((state) => ({
            items: state.items.filter((item) => item.product.id !== productId)
        }));
    }

    function clear(): void {
        commit(() => ({ items: [] }));
    }

    function refreshProductStock(products: Product[]): void {
        commit((state) => {
            const byId = new Map(products.map((p) => [p.id, p]));
            return {
                items: normalizeItems(
                    state.items.map((item) => {
                        const fresh = byId.get(item.product.id);
                        return fresh ? { product: fresh, quantity: item.quantity } : item;
                    })
                )
            };
        });
    }

    const totalItems = derived({ subscribe }, ($state) =>
        $state.items.reduce((sum, item) => sum + item.quantity, 0)
    );
    const totalAmount = derived({ subscribe }, ($state) => {
        const promos = get(promotionStore).items;
        const now = Date.now();
        return $state.items.reduce((sum, item) => {
            const unit = effectivePrice(item.product.price, item.product.id, promos, now);
            return sum + unit * item.quantity;
        }, 0);
    });

    return {
        subscribe,
        addProduct,
        setQuantity,
        remove,
        clear,
        refreshProductStock,
        totalItems,
        totalAmount,
        maxFor: maxQtyFor
    };
}

export const cartStore = createCartStore();
