import type { Sale } from "../entity/Sale";
import type { SaleNotificationUserProvider } from "../repository/SaleNotificationUserProvider";
import type { SaleRepository } from "../repository/SaleRepository";
import type { TelegramNotificator } from "../repository/TelegramNotificator";

/**
 * Registra una nueva venta.
 * El guardado es el camino crítico; Telegram es best-effort.
 */
export class RegisterNewSaleCaseUse {
    constructor(
        private readonly repository: SaleRepository,
        private readonly notificationUserProvider: SaleNotificationUserProvider,
        private readonly telegramNotificator: TelegramNotificator
    ) {}

    async execute(sale: Sale): Promise<Sale> {
        // Camino crítico: persistir la venta (local + remoto via repository)
        const created = await this.repository.create(sale);

        // Notificación operativa: no debe tumbar el checkout
        try {
            const user = await this.notificationUserProvider.getCurrentUser();
            await this.telegramNotificator.notify(created, user);
        } catch (error) {
            console.warn(
                `[RegisterNewSaleCaseUse] event=telegram_notify_best_effort_failure ` +
                `saleId=${created.id} cause=${error instanceof Error ? error.message : String(error)}`
            );
        }

        return created;
    }
}
