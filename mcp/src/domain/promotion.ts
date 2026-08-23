/**
 * Domain — Promotion
 *
 * Maps to the `promotions` collection in Appwrite.
 * Promotions can be product discounts or banners.
 */
export type PromotionSource = "automatic" | "manual";
export type PromotionKind = "product_discount" | "banner";
export type PromotionStatus = "draft" | "active" | "ended" | "cancelled";

export interface Promotion {
  id: string;
  productId: string | null;
  title: string;
  message: string;
  imageUrl: string | null;
  oldPrice: number | null;
  currentPrice: number | null;
  validFromEpochMillis: number;
  validUntilEpochMillis: number;
  source: PromotionSource;
  kind: PromotionKind;
  status: PromotionStatus;
}

export function isPromotionActive(
  promotion: Promotion,
  nowEpochMillis: number
): boolean {
  return (
    promotion.status === "active" &&
    nowEpochMillis >= promotion.validFromEpochMillis &&
    nowEpochMillis <= promotion.validUntilEpochMillis
  );
}
