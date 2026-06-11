// This file contains de mapper to change the CupExchange into a DTO data layer and invert the mapper
import { createCupExchange, type CupExchange } from "../../domain/entity/CupExchange";
import type { CupExchangeDTO } from "../dto/CupExchangeDTO";

const USD_KEYS = ["USD", "USDCUP", "USD_CUP", "DOLAR", "DÓLAR", "DOLLAR"];
const EUR_KEYS = ["EUR", "EURO", "EURO_CUP", "EURCUP"];
const RATE_KEYS = ["rate", "tasa", "valor", "value", "median", "mediana", "trmi", "exchangeRate"];
const DATE_KEYS = ["date", "fecha", "updatedAt", "updated_at", "createdAt", "timestamp"];
const LIST_KEYS = ["data", "rates", "tasas", "items", "results", "records"];

function normalizeKey(value: string): string {
    return value
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .replace(/[^a-zA-Z0-9]/g, "")
        .toUpperCase();
}

function toFiniteNumber(value: unknown): number | null {
    if (typeof value === "number" && Number.isFinite(value)) return value;
    if (typeof value === "string") {
        const parsed = Number(value.replace(",", "."));
        return Number.isFinite(parsed) ? parsed : null;
    }
    return null;
}

function firstStringByKeys(record: Record<string, unknown>, keys: string[]): string | null {
    const normalized = new Map(Object.entries(record).map(([key, value]) => [normalizeKey(key), value]));
    for (const key of keys) {
        const value = normalized.get(normalizeKey(key));
        if (typeof value === "string" && value.trim()) return value;
        if (typeof value === "number" && Number.isFinite(value)) return String(value);
    }
    return null;
}

function firstNumberByKeys(record: Record<string, unknown>, keys: string[]): number | null {
    const normalized = new Map(Object.entries(record).map(([key, value]) => [normalizeKey(key), value]));
    for (const key of keys) {
        const found = toFiniteNumber(normalized.get(normalizeKey(key)));
        if (found !== null) return found;
    }
    return null;
}

function getNestedRecord(record: Record<string, unknown>, keyAliases: string[]): Record<string, unknown> | null {
    const normalized = new Map(Object.entries(record).map(([key, value]) => [normalizeKey(key), value]));
    for (const alias of keyAliases) {
        const found = normalized.get(normalizeKey(alias));
        if (found && typeof found === "object" && !Array.isArray(found)) return found as Record<string, unknown>;
    }
    return null;
}

function readCurrencyValue(record: Record<string, unknown>, currencyAliases: string[]): number | null {
    const direct = firstNumberByKeys(record, currencyAliases);
    if (direct !== null) return direct;

    const nested = getNestedRecord(record, currencyAliases);
    if (nested) {
        const nestedRate = firstNumberByKeys(nested, RATE_KEYS);
        if (nestedRate !== null) return nestedRate;
    }

    const code = firstStringByKeys(record, ["currency", "moneda", "code", "symbol", "name"]);
    if (code && currencyAliases.some((alias) => normalizeKey(code).includes(normalizeKey(alias)))) {
        return firstNumberByKeys(record, RATE_KEYS);
    }

    return null;
}

function collectCandidateRecords(input: unknown): Record<string, unknown>[] {
    if (!input || typeof input !== "object") return [];
    if (Array.isArray(input)) {
        return input.flatMap((item) => collectCandidateRecords(item));
    }

    const record = input as Record<string, unknown>;
    const candidates: Record<string, unknown>[] = [record];

    for (const key of LIST_KEYS) {
        const value = record[key];
        if (Array.isArray(value)) candidates.push(...value.flatMap((item) => collectCandidateRecords(item)));
        if (value && typeof value === "object" && !Array.isArray(value)) candidates.push(...collectCandidateRecords(value));
    }

    return candidates;
}

export function toDomain(dto: CupExchangeDTO | CupExchangeDTO[]): CupExchange {
    const records = collectCandidateRecords(dto);
    let usdReference: number | null = null;
    let euroReference: number | null = null;
    let updatedAt: string | null = null;

    for (const record of records) {
        usdReference ??= readCurrencyValue(record, USD_KEYS);
        euroReference ??= readCurrencyValue(record, EUR_KEYS);
        updatedAt ??= firstStringByKeys(record, DATE_KEYS);
    }

    if (usdReference === null || euroReference === null) {
        throw new Error("No se pudo interpretar la tasa USD/EUR desde la respuesta de elTOQUE.");
    }

    const normalizedUpdatedAt = updatedAt || new Date().toISOString();
    return createCupExchange({
        id: `eltoque-${normalizedUpdatedAt.slice(0, 10)}`,
        usdReference,
        euroReference,
        updatedAt: normalizedUpdatedAt,
        source: "elTOQUE"
    });
}
