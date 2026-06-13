import { createCupExchange, type CupExchange } from "../../domain/entity/CupExchange";
import type { CupExchangeDTO } from "../dto/CupExchangeDTO";

export function toDomain(dto: CupExchangeDTO): CupExchange {
    const usdReference = dto.tasas?.USD?.CUP;
    const euroReference = dto.tasas?.EUR?.CUP;

    if (
        typeof usdReference !== "number" ||
        typeof euroReference !== "number"
    ) {
        throw new Error(
            "No se pudieron obtener las tasas USD/CUP y EUR/CUP."
        );
    }

    const updatedAt =
        dto.actualizado ??
        new Date().toISOString();

    return createCupExchange({
        id: `directorioCubano-${updatedAt.slice(0, 10)}`,
        usdReference,
        euroReference,
        updatedAt,
        source: "DIRECTORIO_CUBANO"
    });
}
