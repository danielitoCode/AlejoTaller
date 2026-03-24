import type { Sale } from "../entity/Sale";
import type { SaleRepository } from "../repository/SaleRepository";

export class CreateSaleCaseUse {
    constructor(private readonly repository: SaleRepository) {}

    execute(sale: Sale): Promise<Sale> {
        return this.repository.create(sale);
    }
}
