
import type { ProductDTO } from "../dto/ProductDTO";
import type { Models } from "appwrite";
import {type Databases, ID,  Query} from "appwrite";
import type { ProductWriteDTO } from "../mapper/Mappers";
import { ENV } from "../../../../infrastructure/env";
import { APPWRITE_COLLECTIONS } from "../../../../infrastructure/data/appwrite/public-data-contract";

const PAGE_SIZE = 100;

const COLLECTION_ID = APPWRITE_COLLECTIONS.product;


class ProductNetRepository {
    constructor(private readonly databases: Databases) {}

    private get databaseId(): string {
        const id = ENV.databaseId;
        if (!id) throw new Error("Falta configurar VITE_APPWRITE_DATABASE_ID");
        return id;
    }

    private async listAll(queries: string[] = []): Promise<ProductDTO[]> {
        const documents: ProductDTO[] = []
        let cursor: string | null = null

        while (true) {
            const page: Models.DocumentList<ProductDTO> = await this.databases.listDocuments<ProductDTO>(
                this.databaseId,
                COLLECTION_ID,
                [
                    ...queries,
                    Query.orderDesc("$createdAt"),
                    Query.limit(PAGE_SIZE),
                    ...(cursor ? [Query.cursorAfter(cursor)] : [])
                ]
            )

            documents.push(...page.documents)

            if (page.documents.length < PAGE_SIZE) break

            cursor = page.documents.at(-1)?.$id ?? null
            if (!cursor) break
        }

        return documents
    }

    async getAll(): Promise<ProductDTO[]> {
        return await this.listAll()
    }

    async getById(id: string): Promise<ProductDTO> {
        return await this.databases.getDocument<ProductDTO>(
            this.databaseId,
            COLLECTION_ID,
            id
        )
    }

    async update(id: string, data: Partial<ProductWriteDTO> | Record<string, unknown>): Promise<ProductDTO> {
        // Nunca enviar $id / meta Appwrite en el body
        const clean: Record<string, unknown> = {}
        for (const [key, value] of Object.entries(data ?? {})) {
            if (key.startsWith("$")) continue
            if (value === undefined) continue
            clean[key] = value
        }
        return await this.databases.updateDocument<ProductDTO>(
            this.databaseId,
            COLLECTION_ID,
            id,
            clean
        )
    }

    async getByCategory(categoryId: string): Promise<ProductDTO[]> {
        const response = await this.databases.listDocuments<ProductDTO>(
            this.databaseId,
            COLLECTION_ID,
            [Query.equal("category_id", categoryId)]
        )

        return response.documents
    }

    async create(product: ProductWriteDTO, id?: string): Promise<ProductDTO> {
        return await this.databases.createDocument<ProductDTO>(
            this.databaseId,
            COLLECTION_ID,
            id || ID.unique(),
            product as ProductDTO
        )
    }

    async delete(id: string): Promise<void> {
        await this.databases.deleteDocument(
            this.databaseId,
            COLLECTION_ID,
            id
        )
    }
}

export default ProductNetRepository
