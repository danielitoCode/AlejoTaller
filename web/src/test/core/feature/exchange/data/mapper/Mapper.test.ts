import { describe, expect, it } from "vitest";
import { toDomain } from "../../../../../../core/feature/exchange/data/mapper/Mappers";

describe("exchange mapper", () => {
    it("maps direct currency fields from elTOQUE-like payloads", () => {
        const exchange = toDomain({
            date: "2026-06-08T12:00:00.000Z",
            USD: 320,
            EUR: 345
        });

        expect(exchange.usdReference).toBe(320);
        expect(exchange.euroReference).toBe(345);
        expect(exchange.source).toBe("elTOQUE");
    });

    it("maps list payloads with moneda/tasa pairs", () => {
        const exchange = toDomain({
            tasas: [
                { moneda: "USD", tasa: "315.50", fecha: "2026-06-08" },
                { moneda: "EUR", tasa: 340 }
            ]
        });

        expect(exchange.usdReference).toBe(315.5);
        expect(exchange.euroReference).toBe(340);
    });
});