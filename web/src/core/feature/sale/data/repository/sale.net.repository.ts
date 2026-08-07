import type { SaleDTO } from "../dto/SaleDTO";
import { type Databases, ID, Query } from "appwrite";
import type { Models } from "appwrite";
import { ENV } from "../../../../infrastructure/env";

const COLLECTION_ID = "sale";

function stripMeta(data: Record<string, unknown>): Record<string, unknown> {
    const clean: Record<string, unknown> = {};
    for (const [key, value] of Object.entries(data)) {
        if (key.startsWith("$")) continue;
        if (value === undefined) continue;
        clean[key] = value;
    }
    return clean;
}

export class SaleNetRepository {
    constructor(private databases: Databases) {}

    private get databaseId(): string {
        const id = ENV.databaseId;
        if (!id) throw new Error("Falta configurar VITE_APPWRITE_DATABASE_ID");
        return id;
    }

    async getAll(): Promise<SaleDTO[]> {
        const response = await this.databases.listDocuments<SaleDTO>(
            this.databaseId,
            COLLECTION_ID
        )

        return response.documents
    }

    async create(
        data: Omit<SaleDTO, keyof Models.Document> | Record<string, unknown>
    ): Promise<SaleDTO> {
        const payload = stripMeta(data as Record<string, unknown>);
        return await this.databases.createDocument<SaleDTO>(
            this.databaseId,
            COLLECTION_ID,
            ID.unique(),
            payload
        )
    }

    async getByUser(userId: string): Promise<SaleDTO[]> {
        const response = await this.databases.listDocuments<SaleDTO>(
            this.databaseId,
            COLLECTION_ID,
            [Query.equal("user_id", userId)]
        )

        return response.documents
    }

    async updateVerified(id: string, verified: string): Promise<SaleDTO> {
        return await this.databases.updateDocument<SaleDTO>(
            this.databaseId,
            COLLECTION_ID,
            id,
            { buy_state: verified }
        );
    }

    async updateDeliveryType(id: string, deliveryType: string): Promise<SaleDTO> {
        return await this.databases.updateDocument<SaleDTO>(
            this.databaseId,
            COLLECTION_ID,
            id,
            { delivery_type: deliveryType }
        );
    }

    async updateStockHoldApplied(id: string, value: boolean): Promise<SaleDTO> {
        return await this.databases.updateDocument<SaleDTO>(
            this.databaseId,
            COLLECTION_ID,
            id,
            { stock_hold_applied: value }
        );
    }
}
