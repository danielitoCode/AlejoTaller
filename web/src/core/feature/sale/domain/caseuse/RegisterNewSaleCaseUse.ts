import type { Sale } from "../entity/Sale";
import type { SaleNotificationUserProvider } from "../repository/SaleNotificationUserProvider";
import type { SaleRepository } from "../repository/SaleRepository";
import type { TelegramNotificator } from "../repository/TelegramNotificator";

export class RegisterNewSaleCaseUse {
    constructor(
        private readonly repository: SaleRepository,
        private readonly notificationUserProvider: SaleNotificationUserProvider,
        private readonly telegramNotificator: TelegramNotificator
    ) {}

    async execute(sale: Sale): Promise<Sale> {
        const user = await this.notificationUserProvider.getCurrentUser();
        await this.telegramNotificator.notify(sale, user);
        return this.repository.create(sale);
    }
}
