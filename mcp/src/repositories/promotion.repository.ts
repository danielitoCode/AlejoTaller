import type { Promotion } from "../domain/promotion.js";

/**
 * Repository interface — Promotion
 *
 * Maps to the `promotions` Appwrite collection.
 * Implementations: AppwritePromotionRepository, MockPromotionRepository (tests)
 */
export interface IPromotionRepository {
  /** Get all promotions (status filtering done at service layer) */
  listAll(): Promise<Promotion[]>;

  /** Get a single promotion by ID */
  getById(promotionId: string): Promise<Promotion | null>;
}
