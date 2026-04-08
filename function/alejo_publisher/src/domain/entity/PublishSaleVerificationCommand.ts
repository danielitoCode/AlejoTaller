import type { SaleDecision } from "./SaleDecision.js";

export interface PublishSaleVerificationCommand {
  saleId: string;
  userId: string;
  decision: SaleDecision;
  amount?: number | null;
  productCount?: number | null;
  cause?: string | null;
}
