import { ExchangeNetRepository } from "../data/repository/exchange.net.repository";
import { ExchangeOfflineFirstRepository } from "../data/repository/exchange-offline-first.repository";
import { GetTodayExchangeCaseUse } from "../domain/caseuse/GetTodayExchangeCaseUse";
import { GetCachedTodayExchangeCaseUse } from "../domain/caseuse/GetCachedTodayExchangeCaseUse"

const exchangeNetRepository = new ExchangeNetRepository();
const exchangeOfflineFirstRepository = new ExchangeOfflineFirstRepository(exchangeNetRepository);
const getTodayExchangeCaseUse = new GetTodayExchangeCaseUse(exchangeOfflineFirstRepository);
const getCachedTodayExchangeCaseUse = new GetCachedTodayExchangeCaseUse(exchangeOfflineFirstRepository);

export const exchangeContainer = {
    repositories: {
        net: exchangeNetRepository,
        offlineFirst: exchangeOfflineFirstRepository
    },
    useCases: {
        getToday: getTodayExchangeCaseUse,
        getCachedToday: getCachedTodayExchangeCaseUse
    }
};