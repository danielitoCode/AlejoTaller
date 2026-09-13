import type { AuthSession } from "./entity/AuthSession";

export interface AuthPort {
    init(): Promise<void>;
    loginWithRedirect(appState?: { returnTo?: string }): Promise<void>;
    handleRedirectCallback(): Promise<void>;
    logout(): Promise<void>;
    getSession(): Promise<AuthSession | null>;
    getAccessToken(): Promise<string | null>;
    getSubject(): Promise<string | null>;
    isAuthenticated(): Promise<boolean>;
}
