import type { Product } from "../entity/Product";
import type { ProductRepository } from "../repository/product.repository";

/**
 * Reacciona a Appwrite Realtime: el servidor ya mutó y envía el documento.
 * Solo proyecta esos snapshots en el offline-first local (Dexie) y devuelve
 * entidades de dominio para el store/UI — sin getById de red.
 */
export class ApplyProductRealtimeSnapshotsCaseUse {
    constructor(private readonly repository: ProductRepository) {}

    async execute(rawDocuments: Record<string, unknown>[]): Promise<Product[]> {
        const docs = rawDocuments.filter((d) => d && (d.$id || d.id));
        if (docs.length === 0) {
            console.warn("[ApplyProductRealtimeSnapshots] sin documentos válidos");
            return [];
        }

        console.info(
            `[ApplyProductRealtimeSnapshots] apply local count=${docs.length} ` +
                `ids=${docs.map((d) => String(d.$id ?? d.id)).join(",")}`
        );

        if (typeof this.repository.applyLocalSnapshots !== "function") {
            console.warn(
                "[ApplyProductRealtimeSnapshots] repository sin applyLocalSnapshots — noop"
            );
            return [];
        }

        const updated = await this.repository.applyLocalSnapshots(docs);
        for (const p of updated) {
            const available = p.existence - (p.reserved ?? 0);
            console.info(
                `[ApplyProductRealtimeSnapshots] OK id=${p.id} existence=${p.existence} ` +
                    `reserved=${p.reserved ?? 0} available=${available}`
            );
        }
        return updated;
    }
}
