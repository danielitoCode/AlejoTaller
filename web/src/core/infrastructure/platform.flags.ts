import { ENV } from "./env";

/** Auth0 activo → no Account/Session Appwrite. */
export function isAuth0Provider(): boolean {
    return String(ENV.authProvider ?? "").trim().toLowerCase() === "auth0";
}

/** Datos en Turso → no Databases Appwrite para catálogo (y features migradas). */
export function isTursoDataProvider(): boolean {
    return String(ENV.dataProvider ?? "").trim().toLowerCase() === "turso";
}

/** Cualquier uso residual de Appwrite Account debe cortarse. */
export function isAppwriteAuthDisabled(): boolean {
    return isAuth0Provider();
}

/**
 * Features aún no migradas (promo, sale, support RT) no deben tocar Appwrite
 * mientras el stack operativo sea Auth0 + Turso.
 */
export function isAppwriteDataStackDisabled(): boolean {
    return isAuth0Provider() || isTursoDataProvider();
}
