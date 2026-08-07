import type { Sale } from "../entity/Sale";
import type { SaleNotificationUserProvider } from "../repository/SaleNotificationUserProvider";
import type { SaleRepository } from "../repository/SaleRepository";
import type { TelegramNotificator } from "../repository/TelegramNotificator";
import type { ProductRepository } from "../../../product/domain/repository/product.repository";
import { availableStock } from "../../../product/domain/entity/Product";
import { publishStockChanged } from "../../../../infrastructure/data/alset-pulse/stock-pulse";

/** Cola por productId para serializar soft-holds concurrentes en el mismo tab/proceso. */
const productHoldQueues = new Map<string, Promise<void>>();

function enqueueProductHold(productId: string, task: () => Promise<void>): Promise<void> {
    const prev = productHoldQueues.get(productId) ?? Promise.resolve();
    const next = prev.then(task, task);
    productHoldQueues.set(
        productId,
        next.catch(() => {
            /* keep queue alive */
        })
    );
    return next;
}

/**
 * Registra venta UNVERIFIED + soft-hold con revalidación concurrente.
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
        const touchedIds: string[] = [];

        try {
            const result = await this.applySoftHold(created);
            holdApplied = result.applied;
            touchedIds.push(...result.productIds);
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
                    `[RegisterNewSaleCaseUse] stock_hold_applied flag: ` +
                    `${flagErr instanceof Error ? flagErr.message : String(flagErr)}`
                );
            }

            void publishStockChanged({
                productIds: touchedIds,
                reason: "hold",
                saleId: created.id,
                timestamp: new Date().toISOString()
            });
        }

        try {
            const user = await this.notificationUserProvider.getCurrentUser();
            await this.telegramNotificator.notify(result, user);
        } catch (error) {
            console.warn(
                `[RegisterNewSaleCaseUse] telegram best-effort saleId=${result.id}: ` +
                `${error instanceof Error ? error.message : String(error)}`
            );
        }

        if (!holdApplied && holdError) {
            (result as Sale & { softHoldError?: string }).softHoldError = holdError;
        }

        return result;
    }

    private async applySoftHold(
        sale: Sale
    ): Promise<{ applied: boolean; productIds: string[] }> {
        if (sale.stockHoldApplied) {
            return { applied: true, productIds: [] };
        }

        if (!sale.products?.length) {
            throw new Error("Sale sin líneas: no hay soft-hold que aplicar");
        }

        const productIds: string[] = [];

        for (const item of sale.products) {
            await enqueueProductHold(item.productId, async () => {
                // Re-lectura fresca justo antes de escribir (reduce race entre clientes)
                const product = await this.productRepository.getById(item.productId);
                if (!product) {
                    throw new Error(
                        `Soft-hold: producto no encontrado productId=${item.productId}`
                    );
                }

                const available = availableStock(product);
                if (item.quantity > available) {
                    throw new Error(
                        `Stock insuficiente (concurrencia): ${item.productName ?? item.productId} ` +
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

                await this.productRepository.update(item.productId, {
                    reserved: nextReserved
                });
                productIds.push(item.productId);
            });
        }

        return { applied: productIds.length > 0, productIds: [...new Set(productIds)] };
    }
}
