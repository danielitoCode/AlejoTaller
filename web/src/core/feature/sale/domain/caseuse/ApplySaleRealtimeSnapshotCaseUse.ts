import type { Sale } from "../entity/Sale";
import type { SaleRepository } from "../repository/SaleRepository";

/**
 * Aplica el documento de venta emitido por Appwrite Realtime a la cache local.
 * Sin re-fetch: el payload ya es post-mutación (fuente de verdad).
 */
export class ApplySaleRealtimeSnapshotCaseUse {
    constructor(private readonly repository: SaleRepository) {}

    async execute(snapshot: Record<string, unknown>): Promise<Sale | null> {
        if (!snapshot || !(snapshot.$id || snapshot.id)) {
            console.warn("[ApplySaleRealtimeSnapshot] snapshot sin id");
            return null;
        }

        if (typeof this.repository.applyLocalSnapshot !== "function") {
            console.warn("[ApplySaleRealtimeSnapshot] repository sin applyLocalSnapshot");
            return null;
        }

        const sale = await this.repository.applyLocalSnapshot(snapshot);
        if (sale) {
            console.info(
                `[ApplySaleRealtimeSnapshot] OK saleId=${sale.id} buy_state=${sale.verified} userId=${sale.userId}`
            );
        }
        return sale;
    }
}
