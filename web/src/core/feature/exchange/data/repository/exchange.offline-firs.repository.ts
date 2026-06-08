import type { CupExchange } from "../../domain/entity/CupExchange";
import type {ExchangeRepository} from "../../domain/repository/ExchangeRepository";
import type {ExchangeNetRepository} from "./exchange.net.repository";
import {db} from "../../../../infrastructure/di/dexie.db";

export class ExchangeOfflineFirsRepository implements ExchangeRepository{

    constructor(
        private readonly net: ExchangeNetRepository
    ) {}

    async getToADay(date: Date): Promise<CupExchange> {

        try {
            const remote = await this.net.getExchangeToADay(date)
        } catch (error: any) {

        }
    }

    getToARankOfDays(): Promise<CupExchange[]> {
        return Promise.resolve([]);
    }

    getToday(): Promise<CupExchange> {
        return Promise.resolve();
    }

}