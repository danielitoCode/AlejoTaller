import type {ProductDTO} from "../dto/ProductDTO";
import type {Product} from "../../domain/entity/Product";

/**
 * Payload de escritura alineado al schema real de Appwrite:
 * stock físico = `status`, soft-hold = `reserved`.
 * No enviar `existence` (atributo no existe en la colección).
 */
export type ProductWriteDTO = {
    $id: string
    name: string
    description: string
    price: number
    photo_url: string
    category_id: string
    rating?: number
    /** Stock físico (label consola: Estado) */
    status: number
    /** Soft-hold UNVERIFIED */
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
 * existence (dominio) ← status | existence | aliases
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
 * Domain → DTO Appwrite (solo atributos que existen en la colección).
 */
export function productToDTO(product: Product): ProductWriteDTO {
    return {
        $id: product.id,
        name: product.name,
        description: product.description,
        status: product.existence,
        reserved: product.reserved ?? 0,
        price: product.price,
        photo_url: product.photoUrl,
        category_id: product.categoryId,
        rating: product.rating
    };
}
