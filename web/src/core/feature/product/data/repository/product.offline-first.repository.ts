import ProductNetRepository from "./product.net.repository"
import type {ProductRepository} from "../../domain/repository/product.repository";
import type {Product} from "../../domain/entity/Product";
import {db} from "../../../../infrastructure/di/dexie.db";
import {productFromDTO, productStockPatch, productToDTO} from "../mapper/Mappers";
import {logger} from "../../../../infrastructure/presentation/util/logger.service";
import type {ProductDTO} from "../dto/ProductDTO";

function sortNewestFirst(products: ProductDTO[]): ProductDTO[] {
    return [...products].sort((a, b) => (b.$createdAt ?? "").localeCompare(a.$createdAt ?? ""))
}

function formatCaughtError(error: unknown): string {
    if (error == null) return "unknown"
    if (typeof error === "string") return error
    if (error instanceof Error) {
        const anyErr = error as Error & { code?: unknown; type?: unknown }
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

function prim(value: unknown): string {
    if (value == null) return "null"
    if (typeof value === "string" || typeof value === "number" || typeof value === "boolean") {
        return String(value)
    }
    try {
        return JSON.stringify(value)
    } catch {
        return "?"
    }
}

function toPlainProductDoc(doc: ProductDTO): ProductDTO {
    const raw = doc as Record<string, unknown>
    const existence = Number(
        raw.existence ?? raw.status ?? raw.Estado ?? raw.stock ?? raw.cantidad ?? 0
    )
    const reserved = Number(raw.reserved ?? raw.reservado ?? 0)
    const plain: Record<string, unknown> = {
        $id: String(raw.$id ?? ""),
        $createdAt: raw.$createdAt ?? null,
        $updatedAt: raw.$updatedAt ?? null,
        name: raw.name ?? "",
        description: raw.description ?? "",
        price: raw.price ?? 0,
        photo_url: raw.photo_url ?? "",
        category_id: raw.category_id ?? "",
        rating: raw.rating ?? 0,
        status: Number.isFinite(existence) ? Math.max(0, Math.floor(existence)) : 0,
        existence: Number.isFinite(existence) ? Math.max(0, Math.floor(existence)) : 0,
        reserved: Number.isFinite(reserved) ? Math.max(0, Math.floor(reserved)) : 0
    }
    return plain as unknown as ProductDTO
}

function logRemoteStockSnapshot(source: string, remote: ProductDTO[]): void {
    if (!import.meta.env.DEV) return

    try {
        const lines: string[] = [
            `[product][DEV] ${source} — Appwrite → antes de Dexie (${remote.length} docs)`
        ]

        for (const doc of remote) {
            const raw = doc as Record<string, unknown>
            const mapped = productFromDTO(doc)
            const keys = Object.keys(raw)
                .filter((k) => !k.startsWith("$") || k === "$id")
                .join(",")

            lines.push(
                [
                    `id=${prim(raw.$id)}`,
                    `name=${prim(raw.name)}`,
                    `status_raw=${prim(raw.status)}`,
                    `existence_raw=${prim(raw.existence)}`,
                    `reserved_raw=${prim(raw.reserved)}`,
                    `mapped_ex=${mapped.existence}`,
                    `mapped_rs=${mapped.reserved}`,
                    `available=${mapped.existence - mapped.reserved}`,
                    `keys=[${keys}]`
                ].join(" | ")
            )
        }

        for (const line of lines) {
            console.info(line)
        }
    } catch (logErr) {
        console.warn(`[product][DEV] falló logRemoteStockSnapshot: ${formatCaughtError(logErr)}`)
    }
}

export class ProductOfflineFirstRepository implements ProductRepository {
    constructor(
        private readonly net: ProductNetRepository
    ) {}

    async getLocalAll(): Promise<Product[]> {
        const local = await db.products.toArray()
        return sortNewestFirst(local).map(productFromDTO)
    }

    async getAll(): Promise<Product[]> {
        let remote: ProductDTO[]

        try {
            remote = await this.net.getAll()
        } catch (error: unknown) {
            const msg = formatCaughtError(error)
            logger.error(`[product] getAll RED falló: ${msg}`)
            const local = await db.products.toArray()
            return sortNewestFirst(local).map(productFromDTO)
        }

        logRemoteStockSnapshot("getAll", remote)

        const plain = remote.map(toPlainProductDoc)

        try {
            await db.products.clear()
            await db.products.bulkPut(plain)
        } catch (error: unknown) {
            logger.error(`[product] getAll Dexie write falló: ${formatCaughtError(error)}`)
        }

        return remote.map(productFromDTO)
    }

    async getById(id: string): Promise<Product | null> {
        try {
            const remote = await this.net.getById(id)
            logRemoteStockSnapshot(`getById(${id})`, [remote])
            try {
                await db.products.put(toPlainProductDoc(remote))
            } catch {
                /* ignore */
            }
            return productFromDTO(remote)
        } catch (error: unknown) {
            const local = await db.products.get(id)
            return local ? productFromDTO(local) : null
        }
    }

    async getByCategory(categoryId: string): Promise<Product[]> {
        try {
            const remote = await this.net.getByCategory(categoryId)
            try {
                await db.products.bulkPut(remote.map(toPlainProductDoc))
            } catch {
                /* ignore */
            }
            return remote.map(productFromDTO)
        } catch {
            const local = await db.products
                .filter((it: any) => it?.category_id === categoryId)
                .toArray()
            return local.map(productFromDTO)
        }
    }

    async incrementReserved(id: string, quantity: number): Promise<Product | null> {
        if (quantity <= 0) return this.getById(id)

        // Read remote only to obtain the current existence used as atomic max.
        // The mutation itself is performed by Appwrite; Dexie is never the authority.
        try {
            const current = await this.net.getById(id)
            const updated = await this.net.incrementReserved(
                id,
                quantity,
                Math.max(0, Math.floor(Number(current.existence ?? current.status ?? 0)))
            )
            await db.products.put(toPlainProductDoc(updated))
            return productFromDTO(updated)
        } catch (error: unknown) {
            logger.error(`[product] atomic increment reserved failed id=${id}: ${formatCaughtError(error)}`)
            return null
        }
    }

    async decrementReserved(id: string, quantity: number): Promise<Product | null> {
        if (quantity <= 0) return this.getById(id)

        // Release is also server-authoritative and atomic; min=0 is enforced by Appwrite.
        try {
            const updated = await this.net.decrementReserved(id, quantity)
            await db.products.put(toPlainProductDoc(updated))
            return productFromDTO(updated)
        } catch (error: unknown) {
            logger.error(`[product] atomic decrement reserved failed id=${id}: ${formatCaughtError(error)}`)
            return null
        }
    }

    async create(product: Product): Promise<Product> {
        try {
            const created = await this.net.create(productToDTO(product), product.id || undefined)
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

        // Soft-hold / stock-only: payload mínimo (solo reserved / existence)
        const stockOnly =
            Object.keys(product).every((k) => k === "reserved" || k === "existence" || k === "id")

        try {
            const payload = stockOnly
                ? productStockPatch({
                      existence: product.existence,
                      reserved: product.reserved
                  })
                : productToDTO(merged)

            if (import.meta.env.DEV) {
                console.info(
                    `[product][DEV] update ${id} stockOnly=${stockOnly} payload=${JSON.stringify(payload)}`
                )
            }

            const updated = await this.net.update(id, payload)
            await db.products.put(toPlainProductDoc(updated))
            return productFromDTO(updated)
        } catch (error: unknown) {
            const msg = formatCaughtError(error)
            logger.error(`Error al actualizar producto en Appwrite: ${msg}`)
            if (import.meta.env.DEV) {
                console.error(`[product][DEV] update FAILED id=${id}: ${msg}`)
            }
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
