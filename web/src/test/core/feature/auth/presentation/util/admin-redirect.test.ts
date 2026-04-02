import { describe, expect, it, vi, beforeEach, afterEach } from "vitest";

const originalLocation = window.location;

vi.mock("../../../../../../core/infrastructure/env", () => ({
    ENV: {
        adminDashboardUrl: "https://admin.example.com/#/dashboard"
    }
}));

describe("admin redirect", () => {
    beforeEach(() => {
        window.sessionStorage.clear();
        Object.defineProperty(window, "location", {
            configurable: true,
            value: {
                href: "https://client.example.com/#/home/dashboard",
                replace: vi.fn()
            }
        });
    });

    afterEach(() => {
        Object.defineProperty(window, "location", {
            configurable: true,
            value: originalLocation
        });
        vi.resetModules();
    });

    it("ofrece selector cuando el usuario es admin y existe URL configurada", async () => {
        const { shouldOfferAdminChoice } = await import("../../../../../../core/feature/auth/presentation/util/admin-redirect");

        const redirected = shouldOfferAdminChoice({ role: "admin" });

        expect(redirected).toBe(true);
        expect(window.location.replace).not.toHaveBeenCalled();
    });

    it("persiste la eleccion de continuar en cliente", async () => {
        const { rememberAdminChoice, getStoredAdminChoice } = await import("../../../../../../core/feature/auth/presentation/util/admin-redirect");

        rememberAdminChoice("client");

        expect(getStoredAdminChoice()).toBe("client");
        expect(window.location.replace).not.toHaveBeenCalled();
    });

    it("redirige al dashboard cuando se ejecuta la accion explicita", async () => {
        const { goToAdminDashboard } = await import("../../../../../../core/feature/auth/presentation/util/admin-redirect");

        const redirected = await goToAdminDashboard();

        expect(redirected).toBe(true);
        expect(window.location.replace).toHaveBeenCalledWith("https://admin.example.com/#/dashboard");
    });
});
