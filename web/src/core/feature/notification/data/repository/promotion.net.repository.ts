import type { PromotionDTO } from "../dto/PromotionDTO";
import { type Databases, ID, Query } from "appwrite";
import type { PromotionWriteDTO } from "../mapper/Mappers";
import { ENV } from "../../../../infrastructure/env";

const COLLECTION_ID = "promotions";

export class PromotionNetRepository {
    constructor(private readonly databases: Databases) {}

    private get databaseId(): string {
        const id = ENV.databaseId;
        if (!id) throw new Error("Falta configurar VITE_APPWRITE_DATABASE_ID");
        return id;
    }

    async getAll(): Promise<PromotionDTO[]> {
        const response = await this.databases.listDocuments<PromotionDTO>(
            this.databaseId,
            COLLECTION_ID
        )

        return response.documents
    }

    async getActive(now: number): Promise<PromotionDTO[]> {
        const response = await this.databases.listDocuments<PromotionDTO>(
            this.databaseId,
            COLLECTION_ID,
            [
                Query.lessThanEqual("validFromEpochMillis", now),
                Query.greaterThanEqual("validUntilEpochMillis", now)
            ]
        )

        return response.documents
    }
}
