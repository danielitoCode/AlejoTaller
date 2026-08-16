import { describe, expect, it } from "vitest";
import { toDomain } from "../../../../../../core/feature/exchange/data/mapper/Mappers";

/**
 * Alineado con CupExchangeDTO (Directorio Cubano):
 * tasas.USD.CUP / tasas.EUR.CUP
 */
describe("exchange mapper", () => {
    it("maps Directorio Cubano tasas payload (USD/EUR → CUP)", () => {
        const exchange = toDomain({
            ok: true,
            fecha: "2026-06-08",
            hora: "12:00",
            actualizado: "2026-06-08T12:00:00.000Z",
            tasas: {
                USD: { CUP: 320, MLC: null, USD: 1 },
                EUR: { CUP: 345, MLC: null, USD: null }
            }
        });

        expect(exchange.usdReference).toBe(320);
        expect(exchange.euroReference).toBe(345);
        expect(exchange.source).toBe("DIRECTORIO_CUBANO");
    });

    it("throws when USD/CUP or EUR/CUP rates are missing", () => {
        expect(() =>
            toDomain({
                ok: true,
                fecha: "2026-06-08",
                hora: "12:00",
                actualizado: "2026-06-08T12:00:00.000Z",
                tasas: {
                    USD: { CUP: null, MLC: null, USD: 1 }
                }
            })
        ).toThrow(/USD\/CUP y EUR\/CUP/);
    });
});
