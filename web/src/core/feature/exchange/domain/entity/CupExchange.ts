export type SupportedExchangeCurrency = "USD" | "EUR";
export type DisplayCurrency = "CUP" | "USD";

export interface CupExchange {
    id: string;
    usdReference: number;
    euroReference: number;
    updatedAt: string;
    source: "DIRECTORIO_CUBANO";
}

export function createCupExchange(cupExchange: CupExchange): CupExchange {
    if(!cupExchange.id || cupExchange.id.trim() === "")
        throw new Error("The value of exchange identifier cannot be empty")
    if (cupExchange.usdReference <= 0 || cupExchange.euroReference <= 0)
        throw new Error("The value of exchange must by positive")
    return {
        ...cupExchange
    }
}