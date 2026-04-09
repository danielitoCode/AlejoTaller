import type { SaleDecision } from "./SaleDecision";

export interface PublishSaleVerificationCommand {
  saleId: string;
  userId: string;
  decision: SaleDecision;
  amount?: number | null;
  productCount?: number | null;
  cause?: string | null;
}
