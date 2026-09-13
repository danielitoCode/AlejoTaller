/** Sesión IdP desacoplada (Auth0). B2C: roles suelen ser vacíos / viewer. */
export interface AuthSession {
    subject: string;
    accessToken: string;
    email?: string | null;
    displayName?: string | null;
    roles: string[];
    expiresAtMs?: number;
    pictureUrl?: string | null;
}

export const AUTH_ROLES_CLAIM = "https://alejotaller.app/roles";
