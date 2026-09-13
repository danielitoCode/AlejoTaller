import { getAuthPort } from "../../di/authPort.factory";

/**
 * Cierra sesión Auth0 (redirect a returnTo) o Appwrite + cleanup local.
 * Si Auth0 activo, `auth.logout()` redirige y no vuelve a ejecutar cleanup local.
 */
export async function performLogout(opts: {
    closeAppwriteSession: () => Promise<void>;
    onLocalCleanup: () => void;
}): Promise<"auth0-redirect" | "local"> {
    const auth = getAuthPort();
    if (auth) {
        await auth.logout();
        return "auth0-redirect";
    }
    try {
        await opts.closeAppwriteSession();
    } finally {
        opts.onLocalCleanup();
    }
    return "local";
}
