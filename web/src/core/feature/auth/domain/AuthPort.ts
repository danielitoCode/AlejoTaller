import type { AuthSession } from "./entity/AuthSession";

export type AuthLoginOptions = {
    returnTo?: string;
    /** e.g. `google-oauth2` — Auth0 free plan social */
    connection?: string;
};

export interface AuthPort {
    init(): Promise<void>;
    loginWithRedirect(appState?: AuthLoginOptions): Promise<void>;
    handleRedirectCallback(): Promise<void>;
    logout(): Promise<void>;
    getSession(): Promise<AuthSession | null>;
    getAccessToken(): Promise<string | null>;
    getSubject(): Promise<string | null>;
    isAuthenticated(): Promise<boolean>;
}
