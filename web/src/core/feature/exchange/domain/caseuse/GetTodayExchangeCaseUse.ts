import type { CupExchange } from "../entity/CupExchange";
import type { ExchangeRepository } from "../repository/ExchangeRepository";

export class GetTodayExchangeCaseUse {
    constructor(private readonly repository: ExchangeRepository) {}

    async execute(): Promise<CupExchange> {
        return this.repository.getToday();
    }
}