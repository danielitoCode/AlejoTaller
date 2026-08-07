import ProductNetRepository from "./product.net.repository"
import type {ProductRepository} from "../../domain/repository/product.repository";
import type {Product} from "../../domain/entity/Product";
import {db} from "../../../../infrastructure/di/dexie.db";
import {productFromDTO, productToDTO} from "../mapper/Mappers";
import {logger} from "../../../../infrastructure/presentation/util/logger.service";
import type {ProductDTO} from "../dto/ProductDTO";

function sortNewestFirst(products: ProductDTO[]): ProductDTO[] {
    return [...products].sort((a, b) => (b.$createdAt ?? "").localeCompare(a.$createdAt ?? ""))
}

/** Solo en DEV: inspecciona payload Appwrite antes de escribir Dexie. */
function logRemoteStockSnapshot(source: string, remote: ProductDTO[]): void {
    if (!import.meta.env.DEV) return

    const rows = remote.map((doc) => {
        const raw = doc as Record<string, unknown>
        const mapped = productFromDTO(doc)
        return {
            $id: doc.$id,
            name: doc.name,
            // claves canónicas
            existence_raw: raw.existence,
            reserved_raw: raw.reserved,
            // alias posibles
            Estado: raw.Estado,
            reservado: raw.reservado,
            stock: raw.stock,
            cantidad: raw.cantidad,
            // tras mapper
            existence_mapped: mapped.existence,
            reserved_mapped: mapped.reserved,
            available_mapped: mapped.existence - mapped.reserved,
            // todas las keys del documento (para ver el nombre real del atributo)
            keys: Object.keys(raw).filter((k) => !k.startsWith("$") || k === "$id")
        }
    })

    console.group(`[product][DEV] ${source} — Appwrite → antes de Dexie (${remote.length} docs)`)
    console.table(rows)
    if (remote[0]) {
        console.log("[product][DEV] documento crudo completo (primer item):", remote[0])
    }
    const withoutExistence = rows.filter(
        (r) => r.existence_raw == null && r.Estado == null && r.stock == null && r.cantidad == null
    )
    if (withoutExistence.length > 0) {
        console.warn(
            `[product][DEV] ${withoutExistence.length}/${rows.length} docs SIN campo existence/Estado/stock/cantidad. Keys del primero:`,
            withoutExistence[0]?.keys
        )
    }
    console.groupEnd()
}

export class ProductOfflineFirstRepository implements ProductRepository {
    constructor(
        private readonly net: ProductNetRepository
    ) {}

    /** Solo cache local (sin red). Para pintar catálogo al instante. */
    async getLocalAll(): Promise<Product[]> {
        const local = await db.products.toArray()
        return sortNewestFirst(local).map(productFromDTO)
    }

    /**
     * Fuente de verdad = red.
     * Al éxito: sobrescribe Dexie con documentos remotos (incluye existence/reserved).
     * Al fallo: sirve cache local.
     */
    async getAll(): Promise<Product[]> {
        try {
            const remote = await this.net.getAll()
            logRemoteStockSnapshot("getAll", remote)
            await db.products.clear()
            await db.products.bulkPut(remote)
            return remote.map(productFromDTO)
        } catch (error: any) {
            logger.error(error?.message ?? "product.getAll network fail", error?.stack)
            const local = await db.products.toArray()
            if (import.meta.env.DEV) {
                console.warn("[product][DEV] getAll falló red → fallback Dexie", local.length, "docs")
            }
            return sortNewestFirst(local).map(productFromDTO)
        }
    }

    async getById(id: string): Promise<Product | null> {
        try {
            const remote = await this.net.getById(id)
            logRemoteStockSnapshot(`getById(${id})`, [remote])
            await db.products.put(remote)
            return productFromDTO(remote)
        } catch {
            const local = await db.products.get(id)
            return local ? productFromDTO(local) : null
        }
    }

    async getByCategory(categoryId: string): Promise<Product[]> {
        try {
            const remote = await this.net.getByCategory(categoryId)
            logRemoteStockSnapshot(`getByCategory(${categoryId})`, remote)
            await db.products.bulkPut(remote)
            return remote.map(productFromDTO)
        } catch {
            const local = await db.products
                .filter((it: any) => it?.category_id === categoryId)
                .toArray()
            return local.map(productFromDTO)
        }
    }

    async create(product: Product): Promise<Product> {
        try {
            const created = await this.net.create(productToDTO(product))
            await db.products.put(created)
            return productFromDTO(created)
        } catch (error: any) {
            logger.error(
                `Error al crear producto en Appwrite: ${error?.message ?? "desconocido"}`,
                error?.stack
            );
            throw error;
        }
    }

    async update(id: string, product: Partial<Product>): Promise<Product> {
        const current = await this.getById(id)
        if (!current) {
            throw new Error(`Product with id ${id} not found`)
        }

        const merged: Product = {
            ...current,
            ...product,
            id
        }

        try {
            const updated = await this.net.update(id, productToDTO(merged))
            await db.products.put(updated)
            return productFromDTO(updated)
        } catch (error: any) {
            logger.error(
                `Error al actualizar producto en Appwrite: ${error?.message ?? "desconocido"}`,
                error?.stack
            );
            throw error;
        }
    }

    async delete(id: string): Promise<void> {
        try {
            await this.net.delete(id)
            await db.products.delete(id)
        } catch (error: any) {
            logger.error(
                `Error al eliminar producto en Appwrite: ${error?.message ?? "desconocido"}`,
                error?.stack
            );
            throw error;
        }
    }

    async sync(): Promise<void> {
        const remote = await this.net.getAll()
        logRemoteStockSnapshot("sync", remote)
        await db.products.clear()
        await db.products.bulkPut(remote)
    }
}
