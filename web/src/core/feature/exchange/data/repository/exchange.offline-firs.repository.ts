import type { CupExchange } from "../../domain/entity/CupExchange";
import type {ExchangeRepository} from "../../domain/repository/ExchangeRepository";
import type {ExchangeNetRepository} from "./exchange.net.repository";
import {db} from "../../../../infrastructure/di/dexie.db";
import { toDomain } from "../mapper/Mappers";

const TODAY_CACHE_ID = "eltoque-today";

export class ExchangeOfflineFirsRepository implements ExchangeRepository {
    constructor(private readonly net: ExchangeNetRepository) {}

    async getToADay(date: Date): Promise<CupExchange> {
        const cacheId = this.toDayCacheId(date);
        try {
            const remote = toDomain(await this.net.getExchangeToADay(date));
            await db.exchanges.put({ ...remote, id: cacheId });
            return remote;
        } catch (error) {
            const cached = await db.exchanges.get(cacheId);
            if (cached) return cached;
            throw error;
        }
    }

    async getToARankOfDays(dateFrom: Date, dateTo: Date): Promise<CupExchange[]> {
        try {
            const remote = (await this.net.getExchangeToRankOfDay(dateFrom, dateTo)).map((dto) => toDomain(dto));
            await db.exchanges.bulkPut(remote);
            return remote;
        } catch (error) {
            const cached = await db.exchanges.toArray();
            if (cached.length) return cached;
            throw error;
        }
    }

    async getToday(): Promise<CupExchange> {
        try {
            const remote = toDomain(await this.net.getExchangeToday());
            await db.exchanges.put({ ...remote, id: TODAY_CACHE_ID });
            return remote;
        } catch (error) {
            const cached = await db.exchanges.get(TODAY_CACHE_ID);
            if (cached) return cached;
            throw error;
        }
    }

    private toDayCacheId(date: Date): string {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, "0");
        const day = String(date.getDate()).padStart(2, "0");
        return `eltoque-${year}-${month}-${day}`;
    }
}