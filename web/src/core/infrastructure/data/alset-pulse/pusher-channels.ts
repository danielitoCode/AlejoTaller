import { ENV } from "../../env";

/**
 * Canales públicos Pusher (Core6).
 * Defaults alineados con .env del proyecto.
 */
export const PUSHER_CHANNELS = {
    stock: () =>
        ENV.pusherStockChannel?.trim() ||
        (import.meta.env.VITE_PUSHER_STOCK_CHANNEL as string | undefined)?.trim() ||
        "stock-updates",
    sales: () =>
        ENV.pusherSalesChannel?.trim() ||
        (import.meta.env.VITE_PUSHER_SALES_CHANNEL as string | undefined)?.trim() ||
        "sale-updates",
    support: () =>
        ENV.pusherSupportChannel?.trim() ||
        "support-updates",
    promo: () =>
        ENV.pusherPromoChannel?.trim() ||
        "promotions-updates",
    notification: () =>
        ENV.pusherNotificationChannel?.trim() ||
        "notifications-updates",
    ia: () =>
        ENV.pusherIaChannel?.trim() ||
        "ia-channel-updates",
} as const;
