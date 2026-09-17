import type { Sale } from "../entity/Sale";
import type { SaleNotificationUserProvider } from "../repository/SaleNotificationUserProvider";
import type { SaleRepository } from "../repository/SaleRepository";
import type { TelegramNotificator } from "../repository/TelegramNotificator";
import type { ProductRepository } from "../../../product/domain/repository/product.repository";
import { availableStock } from "../../../product/domain/entity/Product";
import { publishStockChanged } from "../../../../infrastructure/data/alset-pulse/stock-pulse";
import { publishSaleEvent } from "../../../../infrastructure/data/alset-pulse/sale-pulse";

const productHoldQueues = new Map<string, Promise<void>>();

function enqueueProductHold(productId: string, task: () => Promise<void>): Promise<void> {
    const prev = productHoldQueues.get(productId) ?? Promise.resolve();
    const next = prev.then(task, task);
    productHoldQueues.set(productId, next.catch(() => {}));
    return next;
}

export class RegisterNewSaleCaseUse {
    constructor(
        private readonly repository: SaleRepository,
        private readonly notificationUserProvider: SaleNotificationUserProvider,
        private readonly telegramNotificator: TelegramNotificator,
        private readonly productRepository: ProductRepository
    ) {}

    async execute(sale: Sale): Promise<Sale> {
        const created = await this.repository.create({ ...sale, stockHoldApplied: false });

        let holdApplied = false;
        let holdError: string | null = null;
        const touchedIds: string[] = [];

        try {
            const result = await this.applySoftHold(created);
            holdApplied = result.applied;
            touchedIds.push(...result.productIds);
            console.info(
                `[RegisterNewSaleCaseUse] soft-hold OK saleId=${created.id} applied=${holdApplied} ids=${touchedIds.join(",")}`
            );
        } catch (error) {
            holdError = error instanceof Error ? error.message : String(error);
            console.error(
                `[RegisterNewSaleCaseUse] event=soft_hold_failure saleId=${created.id} cause=${holdError}`
            );
        }

        let result: Sale = { ...created, stockHoldApplied: holdApplied };

        if (holdApplied) {
            try {
                const repoAny = this.repository as SaleRepository & {
                    updateStockHoldApplied?: (id: string, value: boolean) => Promise<Sale>;
                };
                if (typeof repoAny.updateStockHoldApplied === "function") {
                    result = await repoAny.updateStockHoldApplied(created.id, true);
                }
            } catch (flagErr) {
                console.warn(
                    `[RegisterNewSaleCaseUse] stock_hold_applied flag: ${flagErr instanceof Error ? flagErr.message : String(flagErr)}`
                );
            }

            try {
                await publishStockChanged({
                    productIds: touchedIds,
                    reason: "hold",
                    saleId: created.id,
                    timestamp: new Date().toISOString(),
                });
            } catch (pubErr) {
                console.warn(
                    `[RegisterNewSaleCaseUse] publishStockChanged: ${pubErr instanceof Error ? pubErr.message : String(pubErr)}`
                );
            }
        }

        try {
            const productIds = (result.products ?? []).map((p) => p.productId).filter(Boolean);
            await publishSaleEvent("sale:created", {
                saleId: result.id,
                userId: result.userId ?? null,
                verified: String(result.verified ?? "UNVERIFIED"),
                productIds,
            });
        } catch (salePubErr) {
            console.warn(
                `[RegisterNewSaleCaseUse] publishSaleEvent: ${salePubErr instanceof Error ? salePubErr.message : String(salePubErr)}`
            );
        }

        try {
            const user = await this.notificationUserProvider.getCurrentUser();
            await this.telegramNotificator.notify(result, user);
        } catch (error) {
            console.warn(
                `[RegisterNewSaleCaseUse] telegram best-effort saleId=${result.id}: ${error instanceof Error ? error.message : String(error)}`
            );
        }

        if (!holdApplied && holdError) {
            (result as Sale & { softHoldError?: string }).softHoldError = holdError;
        }

        return result;
    }

    private async applySoftHold(sale: Sale): Promise<{ applied: boolean; productIds: string[] }> {
        if (sale.stockHoldApplied) return { applied: true, productIds: [] };
        if (!sale.products?.length) throw new Error("Sale sin líneas: no hay soft-hold que aplicar");

        const touched = new Map<string, number>();
        try {
            for (const item of sale.products) {
                await enqueueProductHold(item.productId, async () => {
                    const product = this.productRepository.refreshFromRemote
                        ? await this.productRepository.refreshFromRemote(item.productId)
                        : await this.productRepository.getById(item.productId);
                    if (!product) {
                        throw new Error(`Soft-hold: producto no disponible productId=${item.productId}`);
                    }
                    const available = availableStock(product);
                    if (item.quantity > available) {
                        throw new Error(
                            `Stock insuficiente: ${item.productName ?? item.productId} (pedido=${item.quantity}, disponible=${available})`
                        );
                    }
                    const updated = await this.productRepository.incrementReserved(item.productId, item.quantity);
                    if (!updated) throw new Error(`Soft-hold atómico rechazado productId=${item.productId}`);
                    touched.set(item.productId, (touched.get(item.productId) ?? 0) + item.quantity);
                });
            }
        } catch (error) {
            await this.compensateSoftHold(touched, sale.id);
            throw error;
        }
        return { applied: touched.size > 0, productIds: [...touched.keys()] };
    }

    private async compensateSoftHold(quantities: Map<string, number>, saleId: string): Promise<void> {
        for (const [productId, quantity] of [...quantities.entries()].reverse()) {
            await enqueueProductHold(productId, async () => {
                try {
                    const released = await this.productRepository.decrementReserved(productId, quantity);
                    if (!released) throw new Error("rollback returned null");
                    console.warn(`[RegisterNewSaleCaseUse] soft_hold_compensated saleId=${saleId} productId=${productId}`);
                } catch (error) {
                    console.error(`[RegisterNewSaleCaseUse] soft_hold_compensation_failed saleId=${saleId}`, error);
                }
            });
        }
    }
}
