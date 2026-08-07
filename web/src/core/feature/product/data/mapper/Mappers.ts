import type {ProductDTO} from "../dto/ProductDTO";
import type {Product} from "../../domain/entity/Product";

export type ProductWriteDTO = Pick<
    ProductDTO,
    "$id" | "name" | "description" | "price" | "photo_url" | "category_id" | "rating" | "existence" | "reserved"
>;

/**
 * Lee un entero de stock desde el documento Appwrite.
 * Acepta claves canónicas y alias (consola ES / legacy).
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
 * DTO → Domain (create/update entity)
 * Se recupera el $id del proporcionado por AppWrite.
 *
 * existence / reserved: claves oficiales + alias por si el atributo
 * en consola se etiquetó distinto (Estado, reservado, etc.).
 */
export function productFromDTO(dto: ProductDTO | Record<string, unknown>): Product {
    const src = dto as Record<string, unknown>;
    const id = String(src.$id ?? src.id ?? "");

    return {
        id,
        name: String(src.name ?? ""),
        description: String(src.description ?? ""),
        existence: readNonNegInt(src, ["existence", "Estado", "stock", "cantidad"]),
        reserved: readNonNegInt(src, ["reserved", "reservado"]),
        price: Number(src.price ?? 0) || 0,
        photoUrl: String(src.photo_url ?? ""),
        categoryId: String(src.category_id ?? ""),
        rating: Number(src.rating ?? 0) || 0,
        createdAtIso: typeof src.$createdAt === "string" ? src.$createdAt : undefined
    };
}

/**
 * Domain → DTO (create/update payload)
 * El id de dominio se serializa en $id de Appwrite.
 * Siempre escribe las claves canónicas del schema Core 1.
 */
export function productToDTO(product: Product): ProductWriteDTO {
    return {
        $id: product.id,
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
