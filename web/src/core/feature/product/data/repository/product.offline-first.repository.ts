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

/** Serializa el error sin romper el logger (evita "Cannot convert object to primitive value"). */
function formatCaughtError(error: unknown): string {
    if (error == null) return "unknown"
    if (typeof error === "string") return error
    if (error instanceof Error) {
        const anyErr = error as Error & { code?: unknown; type?: unknown; response?: unknown }
        const parts = [
            error.message || error.name || "Error",
            anyErr.code != null ? `code=${String(anyErr.code)}` : null,
            typeof anyErr.type === "string" ? `type=${anyErr.type}` : null
        ].filter(Boolean)
        return parts.join(" | ")
    }
    try {
        return JSON.stringify(error)
    } catch {
        return Object.prototype.toString.call(error)
    }
}

/** Documento plano apto para Dexie (sin prototipos de Appwrite SDK). */
function toPlainProductDoc(doc: ProductDTO): ProductDTO {
    const raw = doc as Record<string, unknown>
    return {
        ...(raw as object),
        $id: String(raw.$id ?? ""),
        $createdAt: raw.$createdAt,
        $updatedAt: raw.$updatedAt,
        name: raw.name,
        description: raw.description,
        price: raw.price,
        photo_url: raw.photo_url,
        category_id: raw.category_id,
        rating: raw.rating,
        existence: raw.existence,
        reserved: raw.reserved,
        // alias por si Appwrite usa otros keys
        Estado: raw.Estado,
        reservado: raw.reservado,
        stock: raw.stock,
        cantidad: raw.cantidad
    } as ProductDTO
}

/** Solo en DEV: inspecciona payload Appwrite antes de escribir Dexie. */
function logRemoteStockSnapshot(source: string, remote: ProductDTO[]): void {
    if (!import.meta.env.DEV) return

    try {
        const rows = remote.map((doc) => {
            const raw = doc as Record<string, unknown>
            const mapped = productFromDTO(doc)
            return {
                $id: String(raw.$id ?? ""),
                name: String(raw.name ?? ""),
                existence_raw: raw.existence,
                reserved_raw: raw.reserved,
                Estado: raw.Estado,
                reservado: raw.reservado,
                stock: raw.stock,
                cantidad: raw.cantidad,
                existence_mapped: mapped.existence,
                reserved_mapped: mapped.reserved,
                available_mapped: mapped.existence - mapped.reserved,
                keys: Object.keys(raw)
                    .filter((k) => !k.startsWith("$") || k === "$id")
                    .join(",")
            }
        })

        console.group(`[product][DEV] ${source} — Appwrite → antes de Dexie (${remote.length} docs)`)
        console.table(rows)
        if (remote[0]) {
            try {
                console.log("[product][DEV] documento crudo (primer item) keys:", Object.keys(remote[0] as object))
                console.log("[product][DEV] documento crudo (primer item):", JSON.parse(JSON.stringify(remote[0])))
            } catch {
                console.log("[product][DEV] documento crudo (primer item, no serializable):", remote[0])
            }
        }
        const withoutExistence = rows.filter(
            (r) =>
                r.existence_raw == null &&
                r.Estado == null &&
                r.stock == null &&
                r.cantidad == null
        )
        if (withoutExistence.length > 0) {
            console.warn(
                `[product][DEV] ${withoutExistence.length}/${rows.length} docs SIN existence/Estado/stock/cantidad. keys:`,
                withoutExistence[0]?.keys
            )
        }
        console.groupEnd()
    } catch (logErr) {
        console.warn("[product][DEV] falló logRemoteStockSnapshot:", formatCaughtError(logErr))
    }
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
     * Al fallo de red: sirve cache local.
     */
    async getAll(): Promise<Product[]> {
        let remote: ProductDTO[]

        try {
            remote = await this.net.getAll()
        } catch (error: unknown) {
            const msg = formatCaughtError(error)
            logger.error(`[product] getAll RED falló: ${msg}`)
            if (import.meta.env.DEV) {
                console.error("[product][DEV] getAll RED error completo:", error)
            }
            const local = await db.products.toArray()
            if (import.meta.env.DEV) {
                console.warn("[product][DEV] fallback Dexie", local.length, "docs (stock puede estar stale)")
            }
            return sortNewestFirst(local).map(productFromDTO)
        }

        logRemoteStockSnapshot("getAll", remote)

        const plain = remote.map(toPlainProductDoc)

        try {
            await db.products.clear()
            await db.products.bulkPut(plain)
        } catch (error: unknown) {
            const msg = formatCaughtError(error)
            logger.error(`[product] getAll Dexie write falló: ${msg}`)
            if (import.meta.env.DEV) {
                console.error("[product][DEV] Dexie bulkPut error:", error)
            }
            // Igual devolvemos dominio mapeado desde red (UI correcta aunque cache falle)
        }

        return remote.map(productFromDTO)
    }

    async getById(id: string): Promise<Product | null> {
        try {
            const remote = await this.net.getById(id)
            logRemoteStockSnapshot(`getById(${id})`, [remote])
            try {
                await db.products.put(toPlainProductDoc(remote))
            } catch (dexieErr) {
                if (import.meta.env.DEV) {
                    console.warn("[product][DEV] put Dexie falló:", formatCaughtError(dexieErr))
                }
            }
            return productFromDTO(remote)
        } catch (error: unknown) {
            if (import.meta.env.DEV) {
                console.warn("[product][DEV] getById red falló:", formatCaughtError(error))
            }
            const local = await db.products.get(id)
            return local ? productFromDTO(local) : null
        }
    }

    async getByCategory(categoryId: string): Promise<Product[]> {
        try {
            const remote = await this.net.getByCategory(categoryId)
            logRemoteStockSnapshot(`getByCategory(${categoryId})`, remote)
            try {
                await db.products.bulkPut(remote.map(toPlainProductDoc))
            } catch {
                /* ignore dexie */
            }
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
            await db.products.put(toPlainProductDoc(created))
            return productFromDTO(created)
        } catch (error: unknown) {
            logger.error(`Error al crear producto en Appwrite: ${formatCaughtError(error)}`)
            throw error
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
            await db.products.put(toPlainProductDoc(updated))
            return productFromDTO(updated)
        } catch (error: unknown) {
            logger.error(`Error al actualizar producto en Appwrite: ${formatCaughtError(error)}`)
            throw error
        }
    }

    async delete(id: string): Promise<void> {
        try {
            await this.net.delete(id)
            await db.products.delete(id)
        } catch (error: unknown) {
            logger.error(`Error al eliminar producto en Appwrite: ${formatCaughtError(error)}`)
            throw error
        }
    }

    async sync(): Promise<void> {
        const remote = await this.net.getAll()
        logRemoteStockSnapshot("sync", remote)
        await db.products.clear()
        await db.products.bulkPut(remote.map(toPlainProductDoc))
    }
}
