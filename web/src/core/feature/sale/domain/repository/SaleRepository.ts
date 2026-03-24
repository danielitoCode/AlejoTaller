import type { DeliveryType } from "../entity/enums";
import type {Sale} from "../entity/Sale";

export interface SaleRepository {
    getAllSales(): Promise<Sale[]>;
    create(sale: Sale): Promise<Sale>
    getByUser(userId: string): Promise<Sale[]>
    updateVerified(id: string, verified: string): Promise<Sale>
    updateDeliveryType(id: string, deliveryType: DeliveryType): Promise<Sale>
}
