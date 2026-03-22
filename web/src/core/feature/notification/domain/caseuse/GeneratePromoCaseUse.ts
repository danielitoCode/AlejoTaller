import type {PromotionRepository} from "../repository/PromotionRepository";
import type {SaveCategoryCaseUse} from "../../../category/domain/caseuse/SaveCategoryCaseUse";
import type {Promotion} from "../entity/Promotion";

export class GeneratePromoCaseUse {
    constructor(private readonly promoRepository: PromotionRepository) {}
    async execute(data: Promotion): Promise<void> {
        await this.promoRepository.create(data)
    }
}