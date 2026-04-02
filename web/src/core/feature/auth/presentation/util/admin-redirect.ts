import { ENV } from "../../../../infrastructure/env";

type AuthenticatedUser = {
    role?: string | null;
};

function normalizeRole(role: string | null | undefined): string {
    return (role || "").trim().toLowerCase();
}

function isAdminLikeRole(role: string | null | undefined): boolean {
    const normalized = normalizeRole(role);
    return normalized === "admin" || normalized === "administrator" || normalized === "owner";
}

function resolveAdminDashboardUrl(): string | null {
    const rawUrl = (ENV.adminDashboardUrl || "").trim();
    if (!rawUrl) return null;
    return rawUrl;
}

export function shouldRedirectAdmin(user: AuthenticatedUser | null | undefined): boolean {
    return isAdminLikeRole(user?.role);
}

export async function redirectAdminIfNeeded(
    user: AuthenticatedUser | null | undefined,
    beforeRedirect?: () => Promise<void>
): Promise<boolean> {
    if (typeof window === "undefined") return false;
    if (!shouldRedirectAdmin(user)) return false;

    const targetUrl = resolveAdminDashboardUrl();
    if (!targetUrl) return false;

    const currentUrl = window.location.href;
    if (currentUrl === targetUrl) return false;

    if (beforeRedirect) {
        await beforeRedirect();
    }

    window.location.replace(targetUrl);
    return true;
}
