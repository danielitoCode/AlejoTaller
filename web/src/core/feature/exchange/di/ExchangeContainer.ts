import { ExchangeNetRepository } from "../data/repository/exchange.net.repository";
import { ExchangeOfflineFirsRepository } from "../data/repository/exchange.offline-firs.repository";
import { GetTodayExchangeCaseUse } from "../domain/caseuse/GetTodayExchangeCaseUse";

const exchangeNetRepository = new ExchangeNetRepository();
const exchangeOfflineFirstRepository = new ExchangeOfflineFirsRepository(exchangeNetRepository);
const getTodayExchangeCaseUse = new GetTodayExchangeCaseUse(exchangeOfflineFirstRepository);

export const exchangeContainer = {
    repositories: {
        net: exchangeNetRepository,
        offlineFirst: exchangeOfflineFirstRepository
    },
    useCases: {
        getToday: getTodayExchangeCaseUse
    }
};