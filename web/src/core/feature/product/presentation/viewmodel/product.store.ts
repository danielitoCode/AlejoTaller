import {derived, writable} from "svelte/store";
import type {Product} from "../../domain/entity/Product";
import {productContainer} from "../../di/product.container";

interface ProductState {
    items: Product[]
    selected: Product | null
    /** true solo cuando no hay items locales y se espera red */
    loading: boolean
    /** true mientras se refresca desde Appwrite (catálogo visible) */
    stockSyncing: boolean
    saving: boolean
    error: string | null
}

const initialState: ProductState = {
    items: [],
    selected: null,
    loading: false,
    stockSyncing: false,
    saving: false,
    error: null
}

function normalizeError(error: unknown): string {
    return error instanceof Error ? error.message : "Unexpected error"
}

function sortNewestFirst(products: Product[]): Product[] {
    return [...products].sort((a, b) => (b.createdAtIso ?? "").localeCompare(a.createdAtIso ?? ""))
}

function createProductStore() {
    const {subscribe, update} = writable<ProductState>(initialState)

    async function runSaving<T>(task: () => Promise<T>): Promise<T> {
        update((state) => ({...state, saving: true, error: null}))
        try {
            return await task()
        } catch (error) {
            update((state) => ({...state, error: normalizeError(error)}))
            throw error
        } finally {
            update((state) => ({...state, saving: false}))
        }
    }

    /**
     * Offline-first + fuente de verdad remota:
     * 1) Pinta cache local al instante (sin bloquear catálogo).
     * 2) stockSyncing = true → badges no muestran "Agotado" falso.
     * 3) Refresca desde Appwrite y actualiza Dexie + UI.
     */
    async function syncAll(): Promise<void> {
        update((state) => ({
            ...state,
            error: null,
            stockSyncing: true,
            loading: state.items.length === 0
        }))

        try {
            const repo = productContainer.repository as {
                getLocalAll?: () => Promise<Product[]>
                getAll: () => Promise<Product[]>
            }

            if (typeof repo.getLocalAll === "function") {
                try {
                    const local = await repo.getLocalAll()
                    if (local.length > 0) {
                        update((state) => ({
                            ...state,
                            items: sortNewestFirst(local),
                            loading: false
                        }))
                    }
                } catch {
                    /* ignore local read errors */
                }
            }

            const products = await productContainer.useCases.getAll.execute()
            update((state) => ({
                ...state,
                items: sortNewestFirst(products),
                loading: false,
                stockSyncing: false
            }))
        } catch (error) {
            update((state) => ({
                ...state,
                error: normalizeError(error),
                loading: false,
                stockSyncing: false
            }))
        }
    }

    async function syncById(id: string): Promise<Product | null> {
        update((state) => ({...state, stockSyncing: true, error: null}))
        try {
            const product = await productContainer.useCases.getById.execute(id)
            update((state) => ({
                ...state,
                selected: product,
                stockSyncing: false,
                items: product
                    ? sortNewestFirst(
                          state.items.some((p) => p.id === product.id)
                              ? state.items.map((p) => (p.id === product.id ? product : p))
                              : [...state.items, product]
                      )
                    : state.items
            }))
            return product
        } catch (error) {
            update((state) => ({
                ...state,
                error: normalizeError(error),
                stockSyncing: false
            }))
            throw error
        }
    }

    async function create(data: Product): Promise<void> {
        await runSaving(async () => {
            await productContainer.useCases.create.execute(data)
            await syncAll()
        })
    }

    async function updatePrice(product: Product, newPrice: number): Promise<void> {
        await runSaving(async () => {
            await productContainer.useCases.updatePrice.execute(newPrice, product)
            await syncAll()
            const synced = await productContainer.useCases.getById.execute(product.id)
            update((state) => ({...state, selected: synced}))
        })
    }

    async function removeById(id: string): Promise<void> {
        await runSaving(async () => {
            await productContainer.useCases.delete.execute(id)
            await syncAll()
            update((state) => ({
                ...state,
                selected: state.selected?.id === id ? null : state.selected
            }))
        })
    }

    function clearError(): void {
        update((state) => ({...state, error: null}))
    }

    function reset(): void {
        update(() => initialState)
    }

    const hasData = derived({subscribe}, ($state) => $state.items.length > 0)

    return {
        subscribe,
        hasData,
        syncAll,
        syncById,
        create,
        updatePrice,
        removeById,
        clearError,
        reset
    }
}

export const productStore = createProductStore()
