import type {PromotionRepository} from "../repository/PromotionRepository";
import type {Promotion} from "../entity/Promotion";

export class GetActivePromosCaseUse {
    constructor(private readonly promotionRepository: PromotionRepository) {}
    async execute(row: number): Promise<Promotion[]> {
        return await this.promotionRepository.getActive(row)
    }
}