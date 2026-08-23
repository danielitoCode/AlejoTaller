import { type Databases, Query } from "node-appwrite";
import type { IPromotionRepository } from "../../../repositories/promotion.repository.js";
import type {
  Promotion,
  PromotionKind,
  PromotionSource,
  PromotionStatus,
} from "../../../domain/promotion.js";
import { COLLECTIONS } from "../config.js";

/**
 * Appwrite implementation of IPromotionRepository.
 */
export class AppwritePromotionRepository implements IPromotionRepository {
  constructor(
    private readonly databases: Databases,
    private readonly databaseId: string
  ) {}

  async listAll(): Promise<Promotion[]> {
    const res = await this.databases.listDocuments(
      this.databaseId,
      COLLECTIONS.promotions,
      [Query.limit(100)]
    );
    return res.documents.map((d) => this.toPromotion(d as unknown as AppwritePromotionDoc));
  }

  async getById(promotionId: string): Promise<Promotion | null> {
    try {
      const doc = await this.databases.getDocument(
        this.databaseId,
        COLLECTIONS.promotions,
        promotionId
      );
      return this.toPromotion(doc as unknown as AppwritePromotionDoc);
    } catch {
      return null;
    }
  }

  private toPromotion(doc: AppwritePromotionDoc): Promotion {
    return {
      id: doc.$id,
      productId: doc.productId ?? doc.product_id ?? null,
      title: doc.title ?? "",
      message: doc.message ?? "",
      imageUrl: doc.imageUrl ?? doc.image_url ?? null,
      oldPrice: doc.oldPrice ?? doc.old_price ?? null,
      currentPrice: doc.currentPrice ?? doc.current_price ?? null,
      validFromEpochMillis: Number(doc.validFromEpochMillis ?? doc.valid_from_epoch_millis ?? 0),
      validUntilEpochMillis: Number(doc.validUntilEpochMillis ?? doc.valid_until_epoch_millis ?? 0),
      source: (doc.source as PromotionSource) ?? "manual",
      kind: (doc.kind as PromotionKind) ?? "banner",
      status: (doc.status as PromotionStatus) ?? "draft",
    };
  }
}

interface AppwritePromotionDoc {
  $id: string;
  productId?: string;
  product_id?: string;
  title?: string;
  message?: string;
  imageUrl?: string;
  image_url?: string;
  oldPrice?: number;
  old_price?: number;
  currentPrice?: number;
  current_price?: number;
  validFromEpochMillis?: number;
  valid_from_epoch_millis?: number;
  validUntilEpochMillis?: number;
  valid_until_epoch_millis?: number;
  source?: string;
  kind?: string;
  status?: string;
}
