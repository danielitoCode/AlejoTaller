import { describe, expect, it, vi, beforeEach, afterEach } from "vitest";

const originalLocation = window.location;

vi.mock("../../../../../../core/infrastructure/env", () => ({
    ENV: {
        adminDashboardUrl: "https://admin.example.com/#/dashboard"
    }
}));

describe("admin redirect", () => {
    beforeEach(() => {
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

    it("redirige cuando el usuario es admin y existe URL configurada", async () => {
        const { redirectAdminIfNeeded } = await import("../../../../../../core/feature/auth/presentation/util/admin-redirect");

        const redirected = redirectAdminIfNeeded({ role: "admin" });

        expect(redirected).toBe(true);
        expect(window.location.replace).toHaveBeenCalledWith("https://admin.example.com/#/dashboard");
    });

    it("no redirige cuando el usuario no es admin", async () => {
        const { redirectAdminIfNeeded } = await import("../../../../../../core/feature/auth/presentation/util/admin-redirect");

        const redirected = redirectAdminIfNeeded({ role: "viewer" });

        expect(redirected).toBe(false);
        expect(window.location.replace).not.toHaveBeenCalled();
    });
});
