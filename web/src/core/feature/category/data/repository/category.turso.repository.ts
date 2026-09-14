import type { CategoryDTO } from "../dto/CategoryDTO";
import type { CategoryWriteDTO } from "../mapper/Mappers";
import { getTursoClient } from "../../../../infrastructure/turso/turso.client";
import { logger } from "../../../../infrastructure/presentation/util/logger.service";

type CategoryRow = {
    id: string;
    name: string;
    description: string | null;
    photo_url: string | null;
    status: string | null;
    created_at?: string | null;
    updated_at?: string | null;
};

function rowToDto(row: CategoryRow): CategoryDTO {
    return {
        $id: row.id,
        $createdAt: row.created_at ?? "",
        $updatedAt: row.updated_at ?? "",
        $permissions: [],
        $databaseId: "turso",
        $collectionId: "categories",
        $sequence: 0,
        name: row.name ?? "",
        description: row.description ?? "",
        photo_url: row.photo_url,
        status: row.status === "inactive" ? "inactive" : "active",
    } as CategoryDTO;
}

/**
 * Fuente remota Turso para categorías (lectura).
 * Misma forma que CategoryNetRepository para offline-first.
 */
export class CategoryTursoRepository {
    async getAll(): Promise<CategoryDTO[]> {
        logger.info("[turso] Cargando categorías");
        const db = getTursoClient();
        const rs = await db.execute(
            `SELECT id, name, description, photo_url, status, created_at, updated_at
             FROM categories
             ORDER BY name ASC`,
        );
        return rs.rows.map((r) => rowToDto(r as unknown as CategoryRow));
    }

    async getById(id: string): Promise<CategoryDTO> {
        const db = getTursoClient();
        const rs = await db.execute({
            sql: `SELECT id, name, description, photo_url, status, created_at, updated_at
                  FROM categories WHERE id = ? LIMIT 1`,
            args: [id],
        });
        const row = rs.rows[0] as unknown as CategoryRow | undefined;
        if (!row) throw new Error(`Category not found: ${id}`);
        return rowToDto(row);
    }

    async create(_data: Omit<CategoryDTO, "$id" | "$createdAt" | "$updatedAt" | "$permissions" | "$databaseId" | "$collectionId" | "$sequence">): Promise<CategoryDTO> {
        throw new Error("Turso category.create: escritura pendiente (solo lectura en este esqueleto)");
    }

    async update(_id: string, _data: Partial<CategoryWriteDTO>): Promise<CategoryDTO> {
        throw new Error("Turso category.update: escritura pendiente (solo lectura en este esqueleto)");
    }

    async delete(_id: string): Promise<void> {
        throw new Error("Turso category.delete: escritura pendiente (solo lectura en este esqueleto)");
    }
}
