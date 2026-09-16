import { ENV } from "./env";
import { resolveAuthProvider } from "../feature/auth/di/authPort.factory";

/** Auth0 activo (incluye auto-detect por domain+clientId). */
export function isAuth0Provider(): boolean {
    return resolveAuthProvider() === "auth0";
}

/** Datos en Turso → no Databases Appwrite para catálogo migrado. */
export function isTursoDataProvider(): boolean {
    return String(ENV.dataProvider ?? "").trim().toLowerCase() === "turso";
}

/** Cualquier uso residual de Appwrite Account debe cortarse. */
export function isAppwriteAuthDisabled(): boolean {
    return isAuth0Provider();
}

/**
 * Features aún no migradas no deben tocar Appwrite
 * mientras el stack operativo sea Auth0 + Turso.
 */
export function isAppwriteDataStackDisabled(): boolean {
    return isAuth0Provider() || isTursoDataProvider();
}
