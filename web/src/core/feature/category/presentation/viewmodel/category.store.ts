import type {Category} from "../../domain/entity/Category";
import {derived, writable} from "svelte/store";
import {categoryContainer} from "../../di/category.container";

interface CategoryState {
    items: Category[]
    selected: Category | null
    loading: boolean
    saving: boolean
    error: string | null
}

const initialState: CategoryState = {
    items: [],
    selected: null,
    loading: false,
    saving: false,
    error: null
}

function normalizeError(error: unknown): string {
    return error instanceof Error ? error.message : "Unexpected error"
}

function createCategoryStore() {
    const {subscribe, update} = writable<CategoryState>(initialState)

    async function runLoading<T>(task: () => Promise<T>): Promise<T> {
        update((state) => ({...state, loading: true, error: null}))
        try {
            return await task()
        } catch (error) {
            update((state) => ({...state, error: normalizeError(error)}))
            throw error
        } finally {
            update((state) => ({...state, loading: false}))
        }
    }

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

    async function syncAll(): Promise<void> {
        await runLoading(async () => {
            const categories = await categoryContainer.useCases.getAll.execute()
            update((state) => ({...state, items: categories}))
        })
    }

    async function syncById(id: string): Promise<Category | null> {
        return await runLoading(async () => {
            const category = await categoryContainer.useCases.getById.execute(id)
            update((state) => ({...state, selected: category}))
            return category
        })
    }

    async function create(data: Category): Promise<void> {
        await runSaving(async () => {
            await categoryContainer.useCases.create.execute(data)
            await syncAll()
        })
    }

    async function updateById(id: string, data: Category): Promise<void> {
        await runSaving(async () => {
            await categoryContainer.useCases.update.execute(id, data)
            await syncAll()
            if (id === data.id) {
                update((state) => ({...state, selected: data}))
            }
        })
    }

    async function removeById(id: string): Promise<void> {
        await runSaving(async () => {
            await categoryContainer.useCases.delete.execute(id)
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
        updateById,
        removeById,
        clearError,
        reset
    }
}

export const categoryStore = createCategoryStore()