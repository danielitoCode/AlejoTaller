import type { IPromotionRepository } from "../repositories/promotion.repository.js";
import { type Promotion, isPromotionActive } from "../domain/promotion.js";

/**
 * Service — Promotion
 * Business logic for customer promotions.
 */
export class PromotionService {
  constructor(private readonly promotionRepo: IPromotionRepository) {}

  /**
   * List currently active promotions.
   */
  async listActivePromotions(): Promise<Promotion[]> {
    const all = await this.promotionRepo.listAll();
    const now = Date.now();
    return all.filter((promo) => isPromotionActive(promo, now));
  }
}
