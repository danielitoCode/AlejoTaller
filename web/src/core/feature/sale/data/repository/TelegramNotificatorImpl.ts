import { ENV } from "../../../../infrastructure/env";
import { logger } from "../../../../infrastructure/presentation/util/logger.service";
import type { Sale } from "../../domain/entity/Sale";
import type { SaleNotifierUser } from "../../domain/entity/SaleNotifierUser";
import type { TelegramNotificator } from "../../domain/repository/TelegramNotificator";

const DEFAULT_TELEGRAM_API_URL = "https://api.telegram.org";

type TelegramApiResponse = {
    ok: boolean;
    description?: string;
    error_code?: number;
};

export class TelegramNotificatorImpl implements TelegramNotificator {
    async notify(sale: Sale, user: SaleNotifierUser): Promise<void> {
        const chatId = ENV.telegramChatId?.trim();
        if (!chatId) {
            throw new Error("Falta configurar VITE_TELEGRAM_CHAT_ID");
        }

        const endpoint = resolveTelegramSendMessageEndpoint();
        logger.log(`[TelegramNotificator] event=telegram_notify_start saleId=${sale.id} endpoint=${endpoint}`);

        const response = await fetch(endpoint, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Cache-Control": "no-cache"
            },
            body: JSON.stringify({
                chat_id: chatId,
                text: formatTelegramMessage(sale, user)
            })
        });

        const payload = await response.json() as TelegramApiResponse;
        logger.log(
            `[TelegramNotificator] event=telegram_notify_response saleId=${sale.id} ` +
            `ok=${payload.ok} errorCode=${payload.error_code ?? "null"}`
        );

        if (!response.ok || !payload.ok) {
            const description = payload.description ?? response.statusText ?? "sin descripcion";
            throw new Error(
                `Telegram API devolvio error${payload.error_code ? ` (code=${payload.error_code})` : ""}: ` +
                description
            );
        }

        logger.log(`[TelegramNotificator] event=telegram_notify_success saleId=${sale.id}`);
    }
}

function resolveTelegramSendMessageEndpoint(): string {
    const configuredUrl = ENV.telegramApiUrl?.trim().replace(/\/$/, "") ?? "";
    const botToken = ENV.telegramBotKey?.trim() ?? "";
    const baseUrl = configuredUrl || DEFAULT_TELEGRAM_API_URL;

    if (!configuredUrl) {
        if (!botToken) throw new Error("Falta configurar VITE_TELEGRAM_BOT_KEY");
        return `${baseUrl}/bot${botToken}/sendMessage`;
    }

    if (configuredUrl.endsWith("/sendMessage")) return configuredUrl;
    if (configuredUrl.includes("/bot")) return `${baseUrl}/sendMessage`;

    if (!botToken) throw new Error("Falta configurar VITE_TELEGRAM_BOT_KEY");
    return `${baseUrl}/bot${botToken}/sendMessage`;
}

function formatTelegramMessage(sale: Sale, user: SaleNotifierUser): string {
    const itemsDetails = sale.products
        .map((item) => {
            const productDisplayName = item.productName?.trim() || `ID: ${item.productId}`;
            return `🔹 Producto: ${productDisplayName} | Cantidad: ${item.quantity}`;
        })
        .join("\n");

    const deliveryBlock = sale.deliveryType === "DELIVERY" && sale.deliveryAddress
        ? [
            "",
            "📍 ENTREGA",
            `Provincia: ${sale.deliveryAddress.province}`,
            `Municipio: ${sale.deliveryAddress.municipality}`,
            `Direccion: ${sale.deliveryAddress.mainStreet} No. ${sale.deliveryAddress.houseNumber}`,
            sale.deliveryAddress.betweenStreets ? `Entre calles: ${sale.deliveryAddress.betweenStreets}` : null,
            `Telefono entrega: ${sale.deliveryAddress.phone}`,
            sale.deliveryAddress.referenceName ? `Preguntar por: ${sale.deliveryAddress.referenceName}` : null
        ].filter(Boolean).join("\n")
        : [
            "",
            "📍 ENTREGA",
            `Modalidad: ${sale.deliveryType ?? "PICKUP"}`
        ].join("\n");

    return [
        "🛍️ LISTA DE DESEOS",
        `❤️ Usuario: ${user.name}`,
        `📧 Correo: ${user.email}`,
        `📱 Telefono: ${user.phone || "No registrado"}`,
        "",
        "📝 DETALLES DEL PEDIDO",
        itemsDetails,
        "",
        `💵 Monto total: ${sale.amount.toFixed(2)}`,
        deliveryBlock
    ].join("\n");
}
