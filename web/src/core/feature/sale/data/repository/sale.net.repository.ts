import type { SaleDTO } from "../dto/SaleDTO";
import { type TablesDB, ID, Query } from "appwrite";
import type { Models } from "appwrite";
import { ENV } from "../../../../infrastructure/env";

const TABLE_ID = "sale";

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
    constructor(private tablesDB: TablesDB) {}

    private get databaseId(): string {
        const id = ENV.databaseId;
        if (!id) throw new Error("Falta configurar VITE_APPWRITE_DATABASE_ID");
        return id;
    }

    async getAll(): Promise<SaleDTO[]> {
        const response = await this.tablesDB.listRows<SaleDTO>({
            databaseId: this.databaseId,
            tableId: TABLE_ID,
        });

        return response.rows as unknown as SaleDTO[];
    }

    async create(
        data: Omit<SaleDTO, keyof Models.Row> | Record<string, unknown>
    ): Promise<SaleDTO> {
        const payload = stripMeta(data as Record<string, unknown>);
        return await this.tablesDB.createRow<SaleDTO>({
            databaseId: this.databaseId,
            tableId: TABLE_ID,
            rowId: ID.unique(),
            data: payload,
        }) as unknown as SaleDTO;
    }

    async getByUser(userId: string): Promise<SaleDTO[]> {
        const response = await this.tablesDB.listRows<SaleDTO>({
            databaseId: this.databaseId,
            tableId: TABLE_ID,
            queries: [Query.equal("user_id", userId)],
        });

        return response.rows as unknown as SaleDTO[];
    }

    async updateVerified(id: string, verified: string): Promise<SaleDTO> {
        return await this.tablesDB.updateRow<SaleDTO>({
            databaseId: this.databaseId,
            tableId: TABLE_ID,
            rowId: id,
            data: { buy_state: verified },
        }) as unknown as SaleDTO;
    }

    async updateDeliveryType(id: string, deliveryType: string): Promise<SaleDTO> {
        return await this.tablesDB.updateRow<SaleDTO>({
            databaseId: this.databaseId,
            tableId: TABLE_ID,
            rowId: id,
            data: { delivery_type: deliveryType },
        }) as unknown as SaleDTO;
    }

    async updateStockHoldApplied(id: string, value: boolean): Promise<SaleDTO> {
        return await this.tablesDB.updateRow<SaleDTO>({
            databaseId: this.databaseId,
            tableId: TABLE_ID,
            rowId: id,
            data: { stock_hold_applied: value },
        }) as unknown as SaleDTO;
    }
}
