import type { RealtimeResponseEvent } from "appwrite"
import { client } from "../../di/appwrite.config"
import { ENV } from "../../env"
import { APPWRITE_COLLECTIONS } from "./public-data-contract"

export type AppwritePromotionChangeAction = "create" | "update" | "delete" | "unknown"

export interface AppwritePromotionChangeSignal {
    promotionIds: string[]
    action: AppwritePromotionChangeAction
    snapshots: Record<string, unknown>[]
    timestamp: string
}

export type AppwritePromotionRealtimeHandler = (signal: AppwritePromotionChangeSignal) => void
export type AppwritePromotionRealtimeUnsubscribe = () => void

const LOG = "[appwrite-rt-promo]"

let activeUnsub: AppwritePromotionRealtimeUnsubscribe | null = null
let activeHandler: AppwritePromotionRealtimeHandler | null = null

const DEBOUNCE_MS = 280
let pendingById = new Map<string, Record<string, unknown>>()
let pendingAction: AppwritePromotionChangeAction = "update"
let debounceTimer: ReturnType<typeof setTimeout> | null = null

function buildPromotionsChannel(): string | null {
    const databaseId = ENV.databaseId?.trim()
    if (!databaseId) {
        console.warn(`${LOG} omitido: falta VITE_APPWRITE_DATABASE_ID`)
        return null
    }
    return `databases.${databaseId}.collections.${APPWRITE_COLLECTIONS.promotion}.documents`
}

function classifyAction(events: string[]): AppwritePromotionChangeAction {
    const joined = events.join(" ").toLowerCase()
    if (joined.includes(".create")) return "create"
    if (joined.includes(".delete")) return "delete"
    if (joined.includes(".update")) return "update"
    return "unknown"
}

function extractId(payload: Record<string, unknown>): string | null {
    const id = String(payload.$id ?? payload.id ?? "").trim()
    return id || null
}

function flushPending(): void {
    debounceTimer = null
    const snapshots = [...pendingById.values()]
    const promotionIds = [...pendingById.keys()]
    const action = pendingAction
    pendingById = new Map()
    pendingAction = "update"

    if (promotionIds.length === 0 || !activeHandler) return
    activeHandler({
        promotionIds,
        action,
        snapshots,
        timestamp: new Date().toISOString(),
    })
}

function scheduleFlush(): void {
    if (debounceTimer) clearTimeout(debounceTimer)
    debounceTimer = setTimeout(flushPending, DEBOUNCE_MS)
}

export function subscribeAppwritePromotions(
    handler: AppwritePromotionRealtimeHandler
): AppwritePromotionRealtimeUnsubscribe {
    if (activeUnsub) {
        activeUnsub()
        activeUnsub = null
    }
    activeHandler = handler

    const channel = buildPromotionsChannel()
    if (!channel) {
        return () => {
            activeHandler = null
        }
    }

    try {
        const unsub = client.subscribe(channel, (response: RealtimeResponseEvent<Record<string, unknown>>) => {
            const payload = (response.payload ?? {}) as Record<string, unknown>
            const id = extractId(payload)
            if (!id) return
            const action = classifyAction(response.events ?? [])
            pendingAction = action
            if (action === "delete") {
                pendingById.set(id, { $id: id, _deleted: true })
            } else {
                pendingById.set(id, payload)
            }
            scheduleFlush()
        })

        activeUnsub = () => {
            try {
                unsub()
            } catch {
                /* ignore */
            }
            if (debounceTimer) clearTimeout(debounceTimer)
            debounceTimer = null
            pendingById = new Map()
            activeHandler = null
            activeUnsub = null
            console.log(`${LOG} unsubscribed ${channel}`)
        }

        console.log(`${LOG} subscribed ${channel}`)
        return activeUnsub
    } catch (e: any) {
        console.warn(`${LOG} subscribe failed: ${e?.message ?? e}`)
        activeHandler = null
        return () => {}
    }
}

export function unsubscribeAppwritePromotions(): void {
    if (activeUnsub) activeUnsub()
}
