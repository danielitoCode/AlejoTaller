import type { Sale } from "../entity/Sale";
import type { SaleRepository } from "../repository/SaleRepository";

export class UpdateSaleVerifiedCaseUse {
    constructor(private salesRepository: SaleRepository) {}

    async execute(id: string, verified: string): Promise<Sale> {
        return await this.salesRepository.updateVerified(id, verified);
    }
}

