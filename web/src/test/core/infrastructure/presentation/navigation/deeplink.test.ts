import { beforeEach, describe, expect, it, vi } from "vitest";
import {
    buildHomeHash,
    buildTopLevelHash,
    parseDeepLinkHash
} from "../../../../../core/infrastructure/presentation/navigation/deeplink";
import {
    clearPendingDeepLink,
    consumePendingDeepLink,
    peekPendingDeepLink,
    rememberPendingDeepLink
} from "../../../../../core/infrastructure/presentation/navigation/pending-deeplink.store";

function createSessionStorageMock() {
    const data = new Map<string, string>();
    return {
        getItem: vi.fn((key: string) => data.get(key) ?? null),
        setItem: vi.fn((key: string, value: string) => {
            data.set(key, value);
        }),
        removeItem: vi.fn((key: string) => {
            data.delete(key);
        })
    };
}

describe("deeplink helpers", () => {
    beforeEach(() => {
        vi.stubGlobal("window", {
            sessionStorage: createSessionStorageMock()
        });
        clearPendingDeepLink();
    });

    it("parsea correctamente un deep link interno al detalle de reserva", () => {
        expect(parseDeepLinkHash("#/home/reservation-detail?reservationId=sale-1")).toEqual({
            top: "home",
            nested: "reservation-detail",
            args: { reservationId: "sale-1" }
        });
    });

    it("normaliza rutas top-level y genera hashes estables", () => {
        expect(buildTopLevelHash("login")).toBe("#/login");
        expect(buildHomeHash("dashboard")).toBe("#/home/dashboard");
        expect(parseDeepLinkHash("#/home/unknown")).toEqual({
            top: "home",
            nested: "dashboard",
            args: {}
        });
    });

    it("recuerda y consume un deep link pendiente una sola vez", () => {
        rememberPendingDeepLink("#/home/reservation-detail?reservationId=sale-22");

        expect(peekPendingDeepLink()).toBe("#/home/reservation-detail?reservationId=sale-22");
        expect(consumePendingDeepLink()).toBe("#/home/reservation-detail?reservationId=sale-22");
        expect(peekPendingDeepLink()).toBe("");
    });
});
