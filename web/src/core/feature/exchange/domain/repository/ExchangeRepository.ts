import type {CupExchange} from "../entity/CupExchange";

export interface ExchangeRepository {
    getToday(): Promise<CupExchange>

    getToADay(date: Date): Promise<CupExchange>

    getToARankOfDays(dateFrom: Date, dateTo: Date): Promise<CupExchange[]>
}