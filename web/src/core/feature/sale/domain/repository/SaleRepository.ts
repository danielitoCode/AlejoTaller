import type {Sale} from "../entity/Sale";

export interface SaleRepository {
    getAllSales(): Promise<Sale[]>;
    create(sale: Sale): Promise<Sale>
    getByUser(userId: string): Promise<Sale[]>
    updateVerified(id: string, verified: string): Promise<Sale>
}
