import ProductNetRepository from "./product.net.repository"
import type {ProductRepository} from "../../domain/repository/product.repository";
import type {Product} from "../../domain/entity/Product";
import {db} from "../../../../infrastructure/di/dexie.db";
import {productFromDTO, productToDTO} from "../mapper/Mappers";
import type Dexie from "dexie";
import {logger} from "../../../../infrastructure/presentation/util/logger.service";

export class ProductOfflineFirstRepository implements ProductRepository {
    constructor(
        private readonly net: ProductNetRepository
    ) {}

    async getAll(): Promise<Product[]> {
        try {
            const remote = await this.net.getAll()
            await db.products.bulkPut(remote)
            return remote.map(productFromDTO)
        } catch(error: any) {
            logger.error(error.message, error.stack);
            const local = await db.products.toArray()
            return local.map(productFromDTO)
        }
    }

    async getById(id: string): Promise<Product | null> {
        try {
            const remote = await this.net.getById(id)
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
        await db.products.clear()
        await db.products.bulkPut(remote)
    }
}
