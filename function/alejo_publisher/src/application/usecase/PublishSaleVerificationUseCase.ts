import type { PublishSaleVerificationCommand } from "../../domain/entity/PublishSaleVerificationCommand";
import type { SaleRealtimePublisher } from "../../domain/repository/SaleRealtimePublisher";

export class PublishSaleVerificationUseCase {
  constructor(private readonly publisher: SaleRealtimePublisher) {}

  async execute(command: PublishSaleVerificationCommand): Promise<{
    channel: string;
    eventName: string;
  }> {
    validateCommand(command);
    return this.publisher.publishSaleVerification(command);
  }
}

function validateCommand(command: PublishSaleVerificationCommand): void {
  if (!command.saleId?.trim()) {
    throw new Error("saleId es obligatorio");
  }

  if (!command.userId?.trim()) {
    throw new Error("userId es obligatorio");
  }

  if (command.decision !== "confirmed" && command.decision !== "rejected") {
    throw new Error("decision debe ser confirmed o rejected");
  }
}
