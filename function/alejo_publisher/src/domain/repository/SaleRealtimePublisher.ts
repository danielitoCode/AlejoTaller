import type { PublishSaleVerificationCommand } from "../entity/PublishSaleVerificationCommand.js";

export interface SaleRealtimePublisher {
  publishSaleVerification(command: PublishSaleVerificationCommand): Promise<{
    channel: string;
    eventName: string;
  }>;
}
