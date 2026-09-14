import type { ProductDTO } from "../dto/ProductDTO";
import type { ProductWriteDTO } from "../mapper/Mappers";
import { getTursoClient } from "../../../../infrastructure/turso/turso.client";
import { logger } from "../../../../infrastructure/presentation/util/logger.service";

type ProductRow = {
    id: string;
    name: string;
    description: string | null;
    existence: number | null;
    reserved: number | null;
    price: number | null;
    photo_url: string | null;
    category_id: string;
    rating: number | null;
    status?: string | null;
    created_at?: string | null;
    updated_at?: string | null;
};

function rowToDto(row: ProductRow): ProductDTO {
    const existence = Math.max(0, Math.floor(Number(row.existence ?? 0)));
    const reserved = Math.max(0, Math.floor(Number(row.reserved ?? 0)));
    return {
        $id: row.id,
        $createdAt: row.created_at ?? "",
        $updatedAt: row.updated_at ?? "",
        $permissions: [],
        $databaseId: "turso",
        $collectionId: "products",
        $sequence: 0,
        id: row.id,
        name: row.name ?? "",
        description: row.description ?? "",
        existence,
        reserved,
        price: Number(row.price ?? 0) || 0,
        photo_url: row.photo_url ?? "",
        category_id: row.category_id ?? "",
        rating: Number(row.rating ?? 0) || 0,
    } as ProductDTO;
}

const SELECT_COLS = `id, name, description, existence, reserved, price, photo_url,
    category_id, rating, status, created_at, updated_at`;

/**
 * Fuente remota Turso para productos (lectura).
 * Soft-hold atómico / writes → fase siguiente.
 */
export class ProductTursoRepository {
    async getAll(): Promise<ProductDTO[]> {
        logger.info("[turso] Cargando productos");
        const db = getTursoClient();
        const rs = await db.execute(
            `SELECT ${SELECT_COLS}
             FROM products
             WHERE COALESCE(status, 'active') = 'active'
             ORDER BY created_at DESC`,
        );
        return rs.rows.map((r) => rowToDto(r as unknown as ProductRow));
    }

    async getById(id: string): Promise<ProductDTO> {
        const db = getTursoClient();
        const rs = await db.execute({
            sql: `SELECT ${SELECT_COLS} FROM products WHERE id = ? LIMIT 1`,
            args: [id],
        });
        const row = rs.rows[0] as unknown as ProductRow | undefined;
        if (!row) throw new Error(`Product not found: ${id}`);
        return rowToDto(row);
    }

    async getByCategory(categoryId: string): Promise<ProductDTO[]> {
        const db = getTursoClient();
        const rs = await db.execute({
            sql: `SELECT ${SELECT_COLS}
                  FROM products
                  WHERE category_id = ? AND COALESCE(status, 'active') = 'active'
                  ORDER BY name ASC`,
            args: [categoryId],
        });
        return rs.rows.map((r) => rowToDto(r as unknown as ProductRow));
    }

    async create(_product: ProductWriteDTO, _id?: string): Promise<ProductDTO> {
        throw new Error("Turso product.create: escritura pendiente");
    }

    async update(_id: string, _data: Partial<ProductWriteDTO> | Record<string, unknown>): Promise<ProductDTO> {
        throw new Error("Turso product.update: escritura pendiente");
    }

    async delete(_id: string): Promise<void> {
        throw new Error("Turso product.delete: escritura pendiente");
    }

    async incrementReserved(_id: string, _quantity: number, _maxReserved: number): Promise<ProductDTO> {
        throw new Error("Turso product.incrementReserved: pendiente (fase stock)");
    }

    async decrementReserved(_id: string, _quantity: number): Promise<ProductDTO> {
        throw new Error("Turso product.decrementReserved: pendiente (fase stock)");
    }
}
