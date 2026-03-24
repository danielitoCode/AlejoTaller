import type { DeliveryType } from "../entity/enums";
import type { Sale } from "../entity/Sale";
import type { SaleRepository } from "../repository/SaleRepository";

export class UpdateSaleDeliveryTypeCaseUse {
    constructor(private readonly repository: SaleRepository) {}

    execute(id: string, deliveryType: DeliveryType): Promise<Sale> {
        return this.repository.updateDeliveryType(id, deliveryType);
    }
}
