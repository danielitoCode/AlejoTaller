import type {PromotionRepository} from "../repository/PromotionRepository";
import type {Promotion} from "../entity/Promotion";

export class GetAllPromosCaseUse {
    constructor(private readonly promoRepository: PromotionRepository) {}
    async execute(): Promise<Promotion[]> {
        return await this.promoRepository.getAll()
    }
}