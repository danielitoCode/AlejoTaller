import { getAuthPort, isExternalAuthProvider } from "../../../feature/auth/di/authPort.factory";
import { logNavAuthCheck } from "./debug-logger";

/** Un toque → Clerk Universal Login (sin pantallas intermedias). */
export async function requestClerkLogin(): Promise<boolean> {
    if (import.meta.env.DEV) logNavAuthCheck(false, false, "redirect-login");
    const auth = getAuthPort();
    if (!auth || !isExternalAuthProvider()) return false;
    try {
        await auth.init();
        await auth.loginWithRedirect({
            returnTo: typeof window !== "undefined" ? window.location.origin : undefined,
            screenHint: "login",
        });
        return true;
    } catch {
        if (import.meta.env.DEV) logNavAuthCheck(false, false, "clerk-redirect-failed");
        return false;
    }
}
