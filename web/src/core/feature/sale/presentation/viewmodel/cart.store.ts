import { derived, writable } from "svelte/store";
import type { Product } from "../../../product/domain/entity/Product";

export interface CartItem {
    product: Product;
    quantity: number;
}

interface CartState {
    items: CartItem[];
}

const STORAGE_KEY = "alejo-taller-web-cart";

function readInitialState(): CartState {
    if (typeof window === "undefined") return { items: [] };

    const raw = window.localStorage.getItem(STORAGE_KEY);
    if (!raw) return { items: [] };

    try {
        const parsed = JSON.parse(raw) as CartState;
        return { items: Array.isArray(parsed.items) ? parsed.items : [] };
    } catch {
        return { items: [] };
    }
}

function persist(state: CartState): void {
    if (typeof window === "undefined") return;
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
}

function createCartStore() {
    const { subscribe, update } = writable<CartState>(readInitialState());

    function commit(mutator: (state: CartState) => CartState): void {
        update((state) => {
            const next = mutator(state);
            persist(next);
            return next;
        });
    }

    function addProduct(product: Product, quantity = 1): void {
        commit((state) => {
            const existing = state.items.find((item) => item.product.id === product.id);
            if (existing) {
                return {
                    items: state.items.map((item) =>
                        item.product.id === product.id
                            ? { ...item, quantity: item.quantity + quantity }
                            : item
                    )
                };
            }

            return {
                items: [...state.items, { product, quantity }]
            };
        });
    }

    function setQuantity(productId: string, quantity: number): void {
        commit((state) => ({
            items: state.items
                .map((item) =>
                    item.product.id === productId ? { ...item, quantity: Math.max(0, quantity) } : item
                )
                .filter((item) => item.quantity > 0)
        }));
    }

    function remove(productId: string): void {
        commit((state) => ({
            items: state.items.filter((item) => item.product.id !== productId)
        }));
    }

    function clear(): void {
        commit(() => ({ items: [] }));
    }

    const totalItems = derived({ subscribe }, ($state) =>
        $state.items.reduce((sum, item) => sum + item.quantity, 0)
    );
    const totalAmount = derived({ subscribe }, ($state) =>
        $state.items.reduce((sum, item) => sum + item.product.price * item.quantity, 0)
    );

    return {
        subscribe,
        addProduct,
        setQuantity,
        remove,
        clear,
        totalItems,
        totalAmount
    };
}

export const cartStore = createCartStore();
