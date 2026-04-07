import type { Sale } from "../entity/Sale";
import type { SaleNotifierUser } from "../entity/SaleNotifierUser";

export interface TelegramNotificator {
    notify(sale: Sale, user: SaleNotifierUser): Promise<void>
}
