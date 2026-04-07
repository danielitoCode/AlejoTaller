import type { SaleNotifierUser } from "../entity/SaleNotifierUser";

export interface SaleNotificationUserProvider {
    getCurrentUser(): Promise<SaleNotifierUser>
}
