import type { Models } from "appwrite";
import type { SaleNotifierUser } from "../../domain/entity/SaleNotifierUser";
import type { SaleNotificationUserProvider } from "../../domain/repository/SaleNotificationUserProvider";

export class SessionSaleNotificationUserProvider implements SaleNotificationUserProvider {
    constructor(
        private readonly fetchCurrentUser: () => Promise<Models.User<Models.Preferences>>
    ) {}

    async getCurrentUser(): Promise<SaleNotifierUser> {
        const user = await this.fetchCurrentUser();

        return {
            id: user.$id,
            name: user.name,
            email: user.email,
            phone: user.phone || null
        };
    }
}
