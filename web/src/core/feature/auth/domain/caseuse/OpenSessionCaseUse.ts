import type {SessionNetManager} from "../repository/session.net.manager";

export class OpenSessionCaseUse {
    constructor(private readonly sessionNetManager: SessionNetManager) {}
    async openCustomSession(email: string, pass: string): Promise<string> {
        return await this.sessionNetManager.createEmailSession(email, pass);
    }

    async openTokenSession(userId: string, secret: string): Promise<string> {
        return await this.sessionNetManager.createTokenSession(userId, secret);
    }

    async openGoogleSession(successUrl: string, failureUrl: string): Promise<void> {
        await this.sessionNetManager.createOAuthSession("google", successUrl, failureUrl);
    }
}
