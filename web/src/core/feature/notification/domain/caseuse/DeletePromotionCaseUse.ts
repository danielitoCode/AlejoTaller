import type {PromotionRepository} from "../repository/PromotionRepository";
import type {Promotion} from "../entity/Promotion";

export class DeletePromotionCaseUse {
    constructor(private readonly promotionRepository: PromotionRepository) {}
    async execute(id: string): Promise<void> {
        await this.promotionRepository.delete(id)
    }
}