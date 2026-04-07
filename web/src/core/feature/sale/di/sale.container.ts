import {infrastructureContainer} from "../../../infrastructure/di/infrastructure.container";
import {SaleNetRepository} from "../data/repository/sale.net.repository";
import {SaleOfflineFirstRepository} from "../data/repository/sale.offline-first.repository";
import {GetSalesCaseUse} from "../domain/caseuse/GetSalesCaseUse";
import { UpdateSaleVerifiedCaseUse } from "../domain/caseuse/UpdateSaleVerifiedCaseUse";
import { UpdateSaleDeliveryTypeCaseUse } from "../domain/caseuse/UpdateSaleDeliveryTypeCaseUse";
import { RegisterNewSaleCaseUse } from "../domain/caseuse/RegisterNewSaleCaseUse";
import { SessionSaleNotificationUserProvider } from "../data/repository/SessionSaleNotificationUserProvider";
import { TelegramNotificatorImpl } from "../data/repository/TelegramNotificatorImpl";

// Infrastructure instance
const netDatabases= infrastructureContainer.appwrite.databases

// Data
const saleNetRepository = new SaleNetRepository(netDatabases)
const saleOfflineFirstRepository = new SaleOfflineFirstRepository(saleNetRepository)
const saleNotificationUserProvider = new SessionSaleNotificationUserProvider(
    () => infrastructureContainer.appwrite.account.get()
)
const telegramNotificator = new TelegramNotificatorImpl()

// Domain
const getSalesCaseUse = new GetSalesCaseUse(saleOfflineFirstRepository)
const createSaleCaseUse = new RegisterNewSaleCaseUse(
    saleOfflineFirstRepository,
    saleNotificationUserProvider,
    telegramNotificator
)
const updateSaleVerifiedCaseUse = new UpdateSaleVerifiedCaseUse(saleOfflineFirstRepository)
const updateSaleDeliveryTypeCaseUse = new UpdateSaleDeliveryTypeCaseUse(saleOfflineFirstRepository)

export const saleContainer = {
    repositories: {
        net: saleNetRepository,
        offlineFirst: saleOfflineFirstRepository,
        telegramNotificator
    },
    useCases: {
        getAll: getSalesCaseUse,
        create: createSaleCaseUse,
        updateVerified: updateSaleVerifiedCaseUse,
        updateDeliveryType: updateSaleDeliveryTypeCaseUse
    }
}
