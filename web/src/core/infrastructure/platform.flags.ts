import { ENV } from "./env";
import { isExternalAuthProvider, resolveAuthProvider } from "../feature/auth/di/authPort.factory";

/** @deprecated usar isExternalAuthProvider — true si Clerk o Auth0 */
export function isAuth0Provider(): boolean {
    return isExternalAuthProvider();
}

export function isClerkProvider(): boolean {
    return resolveAuthProvider() === "clerk";
}

/** Datos en Turso → no Databases Appwrite para catálogo migrado. */
export function isTursoDataProvider(): boolean {
    return String(ENV.dataProvider ?? "").trim().toLowerCase() === "turso";
}

/** Cualquier uso residual de Appwrite Account debe cortarse. */
export function isAppwriteAuthDisabled(): boolean {
    return isExternalAuthProvider();
}

/**
 * Features aún no migradas no deben tocar Appwrite
 * mientras el stack operativo sea Clerk/Auth0 + Turso.
 */
export function isAppwriteDataStackDisabled(): boolean {
    return isExternalAuthProvider() || isTursoDataProvider();
}
