import { beforeEach, describe, expect, it, vi } from "vitest";
import { get } from "svelte/store";

const mocks = vi.hoisted(() => ({
    accountGet: vi.fn()
}));

vi.mock("../../../../../../core/infrastructure/di/infrastructure.container", () => ({
    infrastructureContainer: {
        appwrite: {
            account: {
                get: mocks.accountGet
            }
        }
    }
}));

async function loadSessionStore() {
    const module = await import("../../../../../../core/feature/auth/presentation/viewmodel/session.store");
    return module.sessionStore;
}

describe("sessionStore", () => {
    beforeEach(() => {
        vi.resetModules();
        mocks.accountGet.mockReset();
    });

    it("marca el ultimo action cuando getCurrentUser termina correctamente", async () => {
        mocks.accountGet.mockResolvedValue({ $id: "user-1" });

        const sessionStore = await loadSessionStore();
        const result = await sessionStore.getCurrentUser();

        expect(result).toEqual({ $id: "user-1" });
        expect(get(sessionStore).loading).toBe(false);
        expect(get(sessionStore).lastAction).toBe("getCurrentUser");
        expect(get(sessionStore).error).toBeNull();
    });

    it("expone error legible cuando Appwrite falla y permite limpiarlo", async () => {
        mocks.accountGet.mockRejectedValue(new Error("session expired"));

        const sessionStore = await loadSessionStore();
        await expect(sessionStore.getCurrentUser()).rejects.toThrow("session expired");
        expect(get(sessionStore).loading).toBe(false);
        expect(get(sessionStore).error).toBe("session expired");
        expect(get(sessionStore.hasError)).toBe(true);

        sessionStore.clearError();
        expect(get(sessionStore).error).toBeNull();
        expect(get(sessionStore.hasError)).toBe(false);
    });
});
