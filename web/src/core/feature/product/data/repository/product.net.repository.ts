
import type { ProductDTO } from "../dto/ProductDTO";
import { type Databases, ID, Query } from "appwrite";
import type { ProductWriteDTO } from "../mapper/Mappers";
import { ENV } from "../../../../infrastructure/env";

const COLLECTION_ID = "product";

class ProductNetRepository {
    constructor(private readonly databases: Databases) {}

    private get databaseId(): string {
        const id = ENV.databaseId;
        if (!id) throw new Error("Falta configurar VITE_APPWRITE_DATABASE_ID");
        return id;
    }

    async getAll(): Promise<ProductDTO[]> {
        const response = await this.databases.listDocuments<ProductDTO>(
            this.databaseId,
            COLLECTION_ID
        )

        return response.documents
    }

    async getById(id: string): Promise<ProductDTO> {
        return await this.databases.getDocument<ProductDTO>(
            this.databaseId,
            COLLECTION_ID,
            id
        )
    }

    async update(id: string, data: Partial<ProductWriteDTO>): Promise<ProductDTO> {
        return await this.databases.updateDocument<ProductDTO>(
            this.databaseId,
            COLLECTION_ID,
            id,
            data
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

    async create(product: ProductWriteDTO): Promise<ProductDTO> {
        return await this.databases.createDocument<ProductDTO>(
            this.databaseId,
            COLLECTION_ID,
            product.$id || ID.unique(),
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
