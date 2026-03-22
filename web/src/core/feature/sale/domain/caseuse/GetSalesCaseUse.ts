import type {Databases} from "appwrite";
import type {SaleRepository} from "../repository/SaleRepository";
import type {Sale} from "../entity/Sale";

export class GetSalesCaseUse {
    constructor(private salesRepository: SaleRepository) {}
    async execute(): Promise<Sale[]> {
        return await this.salesRepository.getAllSales()
    }
}