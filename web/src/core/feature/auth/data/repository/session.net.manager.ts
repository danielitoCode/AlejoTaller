import {OAuthProvider, type Account} from "appwrite";
import type {SessionNetManager} from "../../domain/repository/session.net.manager";

export class SessionNetManagerImpl implements SessionNetManager{
    constructor(private readonly account: Account) {}

    async createEmailSession(email:string, password:string): Promise<string> {
        let response = await this.account.createEmailPasswordSession(email, password);
        return response.userId
    }

    async createTokenSession(userId: string, secret: string): Promise<string> {
        const session = await (this.account as any).createSession(userId, secret);
        return session?.userId ?? userId;
    }

    async createOAuthSession(provider: "google", successUrl: string, failureUrl: string): Promise<void> {
        const providerMap = {
            google: OAuthProvider.Google
        } as const;

        await this.account.createOAuth2Session(providerMap[provider], successUrl, failureUrl);
    }

    async closeSessions(): Promise<void> {
        await this.account.deleteSessions()
    }
}
