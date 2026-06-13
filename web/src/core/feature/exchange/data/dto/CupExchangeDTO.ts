export interface CurrencyRateDTO {
    CUP: number | null;
    MLC: number | null;
    USD: number | null;
}

export interface CupExchangeDTO {
    ok: boolean;
    fecha: string;
    hora: string;
    actualizado: string;
    tasas: Record<string, CurrencyRateDTO>;
}