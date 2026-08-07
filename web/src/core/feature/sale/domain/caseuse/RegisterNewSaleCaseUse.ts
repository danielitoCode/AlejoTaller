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
 * 3. Marca stock_hold_applied en dominio (+ best-effort remoto si el repo lo permite)
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
        let holdError: string | null = null;

        try {
            holdApplied = await this.applySoftHold(created);
            if (import.meta.env.DEV) {
                console.info(
                    `[RegisterNewSaleCaseUse] soft-hold OK saleId=${created.id} applied=${holdApplied}`
                );
            }
        } catch (error) {
            holdError = error instanceof Error ? error.message : String(error);
            console.error(
                `[RegisterNewSaleCaseUse] event=soft_hold_failure ` +
                `saleId=${created.id} cause=${holdError}`
            );
        }

        let result: Sale = {
            ...created,
            stockHoldApplied: holdApplied
        };

        // Best-effort: persistir flag si el repositorio tiene update parcial de stock_hold_applied
        if (holdApplied) {
            try {
                const repoAny = this.repository as SaleRepository & {
                    updateStockHoldApplied?: (id: string, value: boolean) => Promise<Sale>
                };
                if (typeof repoAny.updateStockHoldApplied === "function") {
                    result = await repoAny.updateStockHoldApplied(created.id, true);
                }
            } catch (flagErr) {
                console.warn(
                    `[RegisterNewSaleCaseUse] no se pudo persistir stock_hold_applied saleId=${created.id}: ` +
                    `${flagErr instanceof Error ? flagErr.message : String(flagErr)}`
                );
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

        // Si el hold falló, la venta existe pero el inventario no se reservó:
        // se anexa señal para que la UI avise (no tumba el pedido).
        if (!holdApplied && holdError) {
            (result as Sale & { softHoldError?: string }).softHoldError = holdError;
        }

        return result;
    }

    /**
     * Incrementa `reserved` por cada línea del pedido.
     * Re-valida available justo antes de escribir (reduce race).
     */
    private async applySoftHold(sale: Sale): Promise<boolean> {
        if (sale.stockHoldApplied) {
            return true;
        }

        if (!sale.products?.length) {
            throw new Error("Sale sin líneas: no hay soft-hold que aplicar");
        }

        let anyApplied = false;

        for (const item of sale.products) {
            const product = await this.productRepository.getById(item.productId);
            if (!product) {
                throw new Error(
                    `Soft-hold: producto no encontrado productId=${item.productId}`
                );
            }

            const available = availableStock(product);
            if (item.quantity > available) {
                throw new Error(
                    `Stock insuficiente para soft-hold: ${item.productName ?? item.productId} ` +
                    `(pedido=${item.quantity}, disponible=${available})`
                );
            }

            const nextReserved = (product.reserved ?? 0) + item.quantity;

            if (import.meta.env.DEV) {
                console.info(
                    `[RegisterNewSaleCaseUse] soft-hold product=${item.productId} ` +
                    `qty=${item.quantity} reserved ${product.reserved ?? 0} → ${nextReserved}`
                );
            }

            // Payload mínimo: solo reserved (evita $id y campos ajenos)
            await this.productRepository.update(item.productId, {
                reserved: nextReserved
            });
            anyApplied = true;
        }

        return anyApplied;
    }
}
