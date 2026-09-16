import type { AuthSession } from "./entity/AuthSession";

export type AuthLoginOptions = {
    returnTo?: string;
    /** e.g. `google-oauth2` — Auth0 Social (plan free) */
    connection?: string;
    /** Auth0 Universal Login: "signup" | "login" */
    screenHint?: "signup" | "login";
    /** Pre-rellena email en Universal Login */
    loginHint?: string;
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
