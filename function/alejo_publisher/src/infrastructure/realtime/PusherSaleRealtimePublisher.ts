import Pusher from "pusher";
import type { PublishSaleVerificationCommand } from "../../domain/entity/PublishSaleVerificationCommand.js";
import type { SaleRealtimePublisher } from "../../domain/repository/SaleRealtimePublisher.js";

export class PusherSaleRealtimePublisher implements SaleRealtimePublisher {
  constructor(private readonly pusher: Pusher) {}

  async publishSaleVerification(command: PublishSaleVerificationCommand): Promise<{
    channel: string;
    eventName: string;
  }> {
    const channel = `sale-verification-${command.userId}`;
    const eventName = command.decision === "confirmed" ? "sale:confirmed" : "sale:rejected";

    await this.pusher.trigger(channel, eventName, {
      saleId: command.saleId,
      userId: command.userId,
      decision: command.decision,
      timestamp: Date.now().toString(),
      amount: command.amount ?? null,
      productCount: command.productCount ?? null,
      type: eventName,
      cause: command.cause ?? null
    });

    return { channel, eventName };
  }
}
