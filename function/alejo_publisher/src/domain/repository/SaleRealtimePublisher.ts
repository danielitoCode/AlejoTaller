import type { PublishSaleVerificationCommand } from "../entity/PublishSaleVerificationCommand";

export interface SaleRealtimePublisher {
  publishSaleVerification(command: PublishSaleVerificationCommand): Promise<{
    channel: string;
    eventName: string;
  }>;
}
