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
 * Fuente remota Turso para productos (lectura + soft-hold).
 * CRUD de catálogo lo hace el backoffice; cliente solo reserved++/--.
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
        throw new Error("Turso product.create: escritura pendiente (solo dash)");
    }

    async update(_id: string, _data: Partial<ProductWriteDTO> | Record<string, unknown>): Promise<ProductDTO> {
        throw new Error("Turso product.update: escritura pendiente (solo dash)");
    }

    async delete(_id: string): Promise<void> {
        throw new Error("Turso product.delete: escritura pendiente (solo dash)");
    }

    /**
     * Soft-hold atómico: reserved += qty solo si reserved + qty <= maxReserved
     * (maxReserved = existence del producto, pasado por offline-first).
     * Compartido con dash vía misma tabla products.
     */
    async incrementReserved(
        id: string,
        quantity: number,
        maxReserved: number,
    ): Promise<ProductDTO> {
        const qty = Math.floor(Number(quantity));
        const maxR = Math.floor(Number(maxReserved));
        if (qty <= 0) throw new Error("quantity debe ser > 0");
        if (maxR < 0) throw new Error("maxReserved debe ser >= 0");

        const db = getTursoClient();
        const rs = await db.execute({
            sql: `UPDATE products
                  SET reserved = COALESCE(reserved, 0) + ?,
                      updated_at = datetime('now')
                  WHERE id = ?
                    AND COALESCE(reserved, 0) + ? <= ?`,
            args: [qty, id, qty, maxR],
        });
        const affected = Number((rs as { rowsAffected?: number }).rowsAffected ?? 0);
        if (affected < 1) {
            try {
                const current = await this.getById(id);
                const reserved = Math.max(0, Math.floor(Number(current.reserved ?? 0)));
                throw new Error(
                    `Soft-hold insuficiente: product=${id} reserved=${reserved} +qty=${qty} max=${maxR}`,
                );
            } catch (e) {
                if (e instanceof Error && e.message.startsWith("Soft-hold")) throw e;
                throw new Error(`Product not found: ${id}`);
            }
        }
        const updated = await this.getById(id);
        logger.info(
            `[turso] soft-hold +${qty} product=${id.slice(0, 12)}… reserved=${updated.reserved}`,
        );
        return updated;
    }

    /**
     * Libera soft-hold: reserved = max(0, reserved - qty).
     */
    async decrementReserved(id: string, quantity: number): Promise<ProductDTO> {
        const qty = Math.floor(Number(quantity));
        if (qty <= 0) throw new Error("quantity debe ser > 0");

        const db = getTursoClient();
        await this.getById(id);

        await db.execute({
            sql: `UPDATE products
                  SET reserved = MAX(0, COALESCE(reserved, 0) - ?),
                      updated_at = datetime('now')
                  WHERE id = ?`,
            args: [qty, id],
        });
        const updated = await this.getById(id);
        logger.info(
            `[turso] soft-hold -${qty} product=${id.slice(0, 12)}… reserved=${updated.reserved}`,
        );
        return updated;
    }
}
