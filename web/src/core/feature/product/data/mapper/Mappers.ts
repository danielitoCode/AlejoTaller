import type {ProductDTO} from "../dto/ProductDTO";
import type {Product} from "../../domain/entity/Product";

/**
 * Schema objetivo Core 1 / Appwrite:
 * - existence: stock físico (fuente de verdad)
 * - reserved: soft-hold UNVERIFIED
 *
 * `status` se mantiene solo como fallback de lectura mientras migras datos.
 * Nunca incluir $id en el body de update/create (Appwrite lo rechaza).
 */
export type ProductWriteDTO = {
    name: string
    description: string
    price: number
    photo_url: string
    category_id: string
    rating?: number
    existence: number
    reserved: number
};

function readNonNegInt(source: Record<string, unknown>, keys: string[]): number {
    for (const key of keys) {
        const raw = source[key];
        if (raw == null || raw === "") continue;
        const n = typeof raw === "number" ? raw : Number(raw);
        if (!Number.isNaN(n) && Number.isFinite(n)) {
            return Math.max(0, Math.floor(n));
        }
    }
    return 0;
}

/**
 * DTO → Domain
 * Preferir `existence`; si aún está vacío, caer a `status` (legacy).
 */
export function productFromDTO(dto: ProductDTO | Record<string, unknown>): Product {
    const src = dto as Record<string, unknown>;
    const id = String(src.$id ?? src.id ?? "");

    return {
        id,
        name: String(src.name ?? ""),
        description: String(src.description ?? ""),
        existence: readNonNegInt(src, ["existence", "status", "Estado", "stock", "cantidad"]),
        reserved: readNonNegInt(src, ["reserved", "reservado"]),
        price: Number(src.price ?? 0) || 0,
        photoUrl: String(src.photo_url ?? ""),
        categoryId: String(src.category_id ?? ""),
        rating: Number(src.rating ?? 0) || 0,
        createdAtIso: typeof src.$createdAt === "string" ? src.$createdAt : undefined
    };
}

/**
 * Domain → payload Appwrite (sin $id).
 */
export function productToDTO(product: Product): ProductWriteDTO {
    return {
        name: product.name,
        description: product.description,
        existence: product.existence,
        reserved: product.reserved ?? 0,
        price: product.price,
        photo_url: product.photoUrl,
        category_id: product.categoryId,
        rating: product.rating
    };
}

/** Solo campos de stock para soft-hold / operador (payload mínimo). */
export function productStockPatch(patch: {
    existence?: number
    reserved?: number
}): Record<string, number> {
    const out: Record<string, number> = {};
    if (patch.existence != null && Number.isFinite(patch.existence)) {
        out.existence = Math.max(0, Math.floor(patch.existence));
    }
    if (patch.reserved != null && Number.isFinite(patch.reserved)) {
        out.reserved = Math.max(0, Math.floor(patch.reserved));
    }
    return out;
}
