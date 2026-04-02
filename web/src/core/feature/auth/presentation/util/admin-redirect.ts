import { ENV } from "../../../../infrastructure/env";

type AuthenticatedUser = {
    role?: string | null;
};

export type AdminClientChoice = "client" | "admin";

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

function getChoiceStorageKey(): string {
    return "alejo-admin-client-choice";
}

export function shouldOfferAdminChoice(user: AuthenticatedUser | null | undefined): boolean {
    return isAdminLikeRole(user?.role) && !!resolveAdminDashboardUrl();
}

export function getStoredAdminChoice(): AdminClientChoice | null {
    if (typeof window === "undefined") return null;
    const choice = window.sessionStorage.getItem(getChoiceStorageKey());
    return choice === "client" || choice === "admin" ? choice : null;
}

export function rememberAdminChoice(choice: AdminClientChoice): void {
    if (typeof window === "undefined") return;
    window.sessionStorage.setItem(getChoiceStorageKey(), choice);
}

export function clearAdminChoice(): void {
    if (typeof window === "undefined") return;
    window.sessionStorage.removeItem(getChoiceStorageKey());
}

export async function goToAdminDashboard(
    beforeRedirect?: () => Promise<void>
): Promise<boolean> {
    if (typeof window === "undefined") return false;
    const targetUrl = resolveAdminDashboardUrl();
    if (!targetUrl) return false;
    if (window.location.href === targetUrl) return false;
    if (beforeRedirect) await beforeRedirect();
    window.location.replace(targetUrl);
    return true;
}
