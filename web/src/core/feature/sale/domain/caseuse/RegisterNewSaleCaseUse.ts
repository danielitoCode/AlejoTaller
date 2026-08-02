import type { Sale } from "../entity/Sale";
import type { SaleNotificationUserProvider } from "../repository/SaleNotificationUserProvider";
import type { SaleRepository } from "../repository/SaleRepository";
import type { TelegramNotificator } from "../repository/TelegramNotificator";
import type { ProductRepository } from "../../../product/domain/repository/product.repository";
import { availableStock } from "../../../product/domain/entity/Product";

/**
 * Registra una nueva venta (UNVERIFIED) y aplica soft-hold de inventario.
 *
 * Flujo:
 * 1. Persist sale (camino crítico)
 * 2. Por cada línea: reserved += quantity (si aún no stock_hold_applied)
 * 3. Marca stock_hold_applied en sale (idempotencia)
 * 4. Telegram best-effort
 *
 * El stock físico (existence) solo baja en VERIFIED (operador).
 */
export class RegisterNewSaleCaseUse {
    constructor(
        private readonly repository: SaleRepository,
        private readonly notificationUserProvider: SaleNotificationUserProvider,
        private readonly telegramNotificator: TelegramNotificator,
        private readonly productRepository: ProductRepository
    ) {}

    async execute(sale: Sale): Promise<Sale> {
        const created = await this.repository.create({
            ...sale,
            stockHoldApplied: false
        });

        let holdApplied = false;
        try {
            holdApplied = await this.applySoftHold(created);
        } catch (error) {
            console.warn(
                `[RegisterNewSaleCaseUse] event=soft_hold_partial_failure ` +
                `saleId=${created.id} cause=${error instanceof Error ? error.message : String(error)}`
            );
        }

        let result = created;
        if (holdApplied && !created.stockHoldApplied) {
            try {
                // Best-effort: marcar flag en remoto si el repo lo soporta vía updateVerified no aplica;
                // se deja en dominio local; el flag se puede persistir en una actualización futura.
                result = { ...created, stockHoldApplied: true };
            } catch {
                result = { ...created, stockHoldApplied: true };
            }
        }

        try {
            const user = await this.notificationUserProvider.getCurrentUser();
            await this.telegramNotificator.notify(result, user);
        } catch (error) {
            console.warn(
                `[RegisterNewSaleCaseUse] event=telegram_notify_best_effort_failure ` +
                `saleId=${result.id} cause=${error instanceof Error ? error.message : String(error)}`
            );
        }

        return result;
    }

    /**
     * Incrementa `reserved` por cada línea del pedido.
     * Re-valida available justo antes de escribir (reduce race).
     * @returns true si se aplicó hold en al menos un producto
     */
    private async applySoftHold(sale: Sale): Promise<boolean> {
        if (sale.stockHoldApplied) {
            return true;
        }

        let anyApplied = false;

        for (const item of sale.products) {
            const product = await this.productRepository.getById(item.productId);
            if (!product) {
                console.warn(
                    `[RegisterNewSaleCaseUse] soft-hold skip missing productId=${item.productId}`
                );
                continue;
            }

            const available = availableStock(product);
            if (item.quantity > available) {
                throw new Error(
                    `Stock insuficiente para soft-hold: ${item.productName ?? item.productId} ` +
                    `(pedido=${item.quantity}, disponible=${available})`
                );
            }

            const nextReserved = (product.reserved ?? 0) + item.quantity;
            await this.productRepository.update(item.productId, {
                reserved: nextReserved
            });
            anyApplied = true;
        }

        return anyApplied;
    }
}
