import type {ProductDTO} from "../dto/ProductDTO";
import type {Product} from "../../domain/entity/Product";

export type ProductWriteDTO = Pick<
    ProductDTO,
    "$id" | "name" | "description" | "price" | "photo_url" | "category_id" | "rating" | "existence" | "reserved"
> & {
    /** Schema real Appwrite: stock físico vive en `status` (no `existence`). */
    status?: number
};

/**
 * Lee un entero de stock desde el documento Appwrite.
 * Schema observado en prod:
 * keys = name, description, price, category_id, photo_url, rating, status, reserved
 * → stock físico = `status` (legacy / consola "Estado")
 * → soft-hold = `reserved`
 */
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
 * existence se lee de status (schema actual) o existence (si se migra el atributo).
 */
export function productFromDTO(dto: ProductDTO | Record<string, unknown>): Product {
    const src = dto as Record<string, unknown>;
    const id = String(src.$id ?? src.id ?? "");

    return {
        id,
        name: String(src.name ?? ""),
        description: String(src.description ?? ""),
        // Orden: canónico policy → schema real Appwrite → aliases
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
 * Domain → DTO para escritura Appwrite.
 * Escribe `status` (atributo real) y también `existence` si el schema lo permite.
 */
export function productToDTO(product: Product): ProductWriteDTO {
    return {
        $id: product.id,
        name: product.name,
        description: product.description,
        existence: product.existence,
        status: product.existence,
        reserved: product.reserved ?? 0,
        price: product.price,
        photo_url: product.photoUrl,
        category_id: product.categoryId,
        rating: product.rating
    };
}
