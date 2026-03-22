import type { CategoryDTO } from "../dto/CategoryDTO";
import type { Databases, Models } from "appwrite";
import { ID } from "appwrite";
import type { CategoryWriteDTO } from "../mapper/Mappers";
import { logger } from "../../../../infrastructure/presentation/util/logger.service";
import { ENV } from "../../../../infrastructure/env";

const COLLECTION_ID = "category";

export class CategoryNetRepository {
    constructor(private readonly databases: Databases) {}

    private get databaseId(): string {
        const id = ENV.databaseId;
        if (!id) throw new Error("Falta configurar VITE_APPWRITE_DATABASE_ID");
        return id;
    }

    async getAll(): Promise<CategoryDTO[]> {
        logger.info("Cargando categorías");
        const response = await this.databases.listDocuments<CategoryDTO>(
            this.databaseId,
            COLLECTION_ID
        )

        //logger.info(`categorias cargadas correctamente ${response.documents.toString()}`)

        return response.documents
    }

    async create(
        data: Omit<CategoryDTO, keyof Models.Document>
    ): Promise<CategoryDTO> {
        return await this.databases.createDocument<CategoryDTO>(
            this.databaseId,
            COLLECTION_ID,
            ID.unique(),
            data
        )
    }

    async getById(id: string): Promise<CategoryDTO> {
        return await this.databases.getDocument<CategoryDTO>(
            this.databaseId,
            COLLECTION_ID,
            id
        )
    }

    async delete(id: string): Promise<void> {
        await this.databases.deleteDocument(
            this.databaseId,
            COLLECTION_ID,
            id
        )
    }

    async update(id: string, data: Partial<CategoryWriteDTO>): Promise<CategoryDTO> {
        return await this.databases.updateDocument<CategoryDTO>(
            this.databaseId,
            COLLECTION_ID,
            id,
            data
        )
    }
}
