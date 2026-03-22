import type {CategoryRepository} from "../../domain/repository/CategoryRepository";
import {CategoryNetRepository} from "./category.net.repository";
import type {Category} from "../../domain/entity/Category";
import {db} from "../../../../infrastructure/di/dexie.db";
import {categoryFromDTO, categoryToDTO} from "../mapper/Mappers";
import {logger} from "../../../../infrastructure/presentation/util/logger.service";
import {ID} from "appwrite";

export class CategoryOfflineFirstRepository implements CategoryRepository {
    constructor(private readonly net: CategoryNetRepository) {}

    async getAll(): Promise<Category[]> {
        try {
            const remote = await this.net.getAll()
            await db.categories.bulkPut(remote)
            return remote.map(categoryFromDTO)
        } catch(error: any) {
            logger.error("Error al cargar categorias", error.stack)
            const local = await db.categories.toArray()
            return local.map(categoryFromDTO)
        }
    }

    async getById(id: string): Promise<Category | null> {
        try {
            const remote = await this.net.getById(id)
            await db.categories.put(remote)
            return categoryFromDTO(remote)
        } catch {
            const local = await db.categories.get(id)
            return local ? categoryFromDTO(local) : null
        }
    }

    async update(id: string, category: Partial<Category>): Promise<Category> {
        const current = await this.getById(id)
        if (!current) {
            throw new Error(`Category with id ${id} not found`)
        }

        const merged: Category = {
            ...current,
            ...category,
            id
        }

        try {
            const updated = await this.net.update(id, categoryToDTO(merged))
            await db.categories.put(updated)
            return categoryFromDTO(updated)
        } catch (error: any) {
            logger.error(
                `Error al actualizar categoría en Appwrite: ${error?.message ?? "desconocido"}`,
                error?.stack
            );
            throw error;
        }
    }

    async delete(id: string): Promise<void> {
        try {
            await this.net.delete(id)
            await db.categories.delete(id)
        } catch (error: any) {
            logger.error(
                `Error al eliminar categoría en Appwrite: ${error?.message ?? "desconocido"}`,
                error?.stack
            );
            throw error;
        }
    }

    async create(category: Category): Promise<Category> {
        try {
            const created = await this.net.create({
                name: category.name,
                description: category.description,
                photo_url: category.photoUrl ?? "",
                status: category.status
            })
            await db.categories.put(created)
            return categoryFromDTO(created)
        } catch (error: any) {
            logger.error(
                `Error al crear categoría en Appwrite: ${error?.message ?? "desconocido"}`,
                error?.stack
            );
            throw error;
        }
    }

    async sync(): Promise<void> {
        const remote = await this.net.getAll()
        await db.categories.clear()
        await db.categories.bulkPut(remote)
    }
}
