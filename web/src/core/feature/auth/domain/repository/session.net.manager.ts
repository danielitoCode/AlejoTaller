export interface SessionNetManager {
    createEmailSession(email:string, password:string): Promise<string>
    createTokenSession(userId: string, secret: string): Promise<string>
    createOAuthSession(provider: "google", successUrl: string, failureUrl: string): Promise<void>
    createAnonymousSession(): Promise<string>
    closeSessions(): Promise<void>
}
