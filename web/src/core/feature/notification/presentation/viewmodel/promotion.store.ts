import { derived, writable } from "svelte/store"
import type { Promotion } from "../../domain/entity/Promotion"
import { notificationContainer } from "../../di/notification.container"
import { logger } from "../../../../infrastructure/presentation/util/logger.service"
import { promotionFromDTO } from "../../data/mapper/Mappers"
import { isAppwritePermissionError } from "../../../../infrastructure/data/appwrite/public-data-contract"
import {
    subscribeAppwritePromotions,
    unsubscribeAppwritePromotions,
    type AppwritePromotionChangeSignal,
} from "../../../../infrastructure/data/appwrite/appwrite-promotion-realtime"
import {
    isActiveBanner,
    isActiveProductDiscount,
} from "../../domain/policy/PromotionPolicy"
import type { PromotionDTO } from "../../data/dto/PromotionDTO"

interface PromotionState {
    items: Promotion[]
    loading: boolean
    saving: boolean
    error: string | null
}

const initialState: PromotionState = {
    items: [],
    loading: false,
    saving: false,
    error: null,
}

function normalizeError(error: unknown): string {
    return error instanceof Error ? error.message : "Unexpected error"
}

function createPromotionStore() {
    const { subscribe, update } = writable<PromotionState>(initialState)
    let appwriteUnsub: (() => void) | null = null

    async function runLoading<T>(fn: () => Promise<T>): Promise<T> {
        update((state) => ({ ...state, loading: true, error: null }))
        try {
            return await fn()
        } catch (error) {
            update((state) => ({ ...state, error: normalizeError(error) }))
            throw error
        } finally {
            update((state) => ({ ...state, loading: false }))
        }
    }

    function ensureAppwriteSubscription(): void {
        if (appwriteUnsub) return
        logger.log("[PromotionStore] Activating Appwrite RT promotions")
        appwriteUnsub = subscribeAppwritePromotions((signal) => {
            handleAppwriteSignal(signal)
        })
    }

    function handleAppwriteSignal(signal: AppwritePromotionChangeSignal): void {
        logger.log(
            `[PromotionRT] action=${signal.action} ids=${signal.promotionIds.join(",")}`
        )

        update((state) => {
            let items = [...state.items]

            for (const snap of signal.snapshots) {
                const id = String(snap.$id ?? snap.id ?? "").trim()
                if (!id) continue

                if (snap._deleted || signal.action === "delete") {
                    items = items.filter((p) => p.id !== id)
                    continue
                }

                try {
                    const promo = promotionFromDTO(snap as unknown as PromotionDTO)
                    const idx = items.findIndex((p) => p.id === promo.id)
                    if (idx >= 0) items[idx] = promo
                    else items = [...items, promo]
                } catch (e: any) {
                    logger.warn(`[PromotionRT] map failed id=${id}: ${e?.message ?? e}`)
                }
            }

            return { ...state, items }
        })
    }

    async function syncAll(options: { suppressPermissionError?: boolean } = {}): Promise<void> {
        if (options.suppressPermissionError) {
            try {
                await syncAllInner()
            } catch (error) {
                if (!isAppwritePermissionError(error)) throw error
                logger.warn(
                    "[PromotionStore] Promotions are not public for this visitor session; keeping sync silent."
                )
                update((state) => ({ ...state, error: null, loading: false }))
            }
            return
        }
        await syncAllInner()
    }

    async function syncAllInner(): Promise<void> {
        await runLoading(async () => {
            const items = await notificationContainer.useCases.promo.getAll()
            update((state) => ({ ...state, items }))
            ensureAppwriteSubscription()
        })
    }

    function cleanup(): void {
        if (appwriteUnsub) {
            appwriteUnsub()
            appwriteUnsub = null
        }
        unsubscribeAppwritePromotions()
    }

    const hasData = derived({ subscribe }, ($state) => $state.items.length > 0)

    const activePromotions = derived({ subscribe }, ($state) => {
        const now = Date.now()
        return $state.items.filter(
            (p) => isActiveProductDiscount(p, now) || isActiveBanner(p, now)
        )
    })

    const activeProductDiscounts = derived({ subscribe }, ($state) => {
        const now = Date.now()
        return $state.items.filter((p) => isActiveProductDiscount(p, now))
    })

    const activeBanners = derived({ subscribe }, ($state) => {
        const now = Date.now()
        return $state.items.filter((p) => isActiveBanner(p, now))
    })

    return {
        subscribe,
        hasData,
        activePromotions,
        activeProductDiscounts,
        activeBanners,
        syncAll,
        cleanup,
    }
}

export const promotionStore = createPromotionStore()
