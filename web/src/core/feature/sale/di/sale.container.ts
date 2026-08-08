import {infrastructureContainer} from "../../../infrastructure/di/infrastructure.container";
import {SaleNetRepository} from "../data/repository/sale.net.repository";
import {SaleOfflineFirstRepository} from "../data/repository/sale.offline-first.repository";
import {GetSalesCaseUse} from "../domain/caseuse/GetSalesCaseUse";
import { UpdateSaleVerifiedCaseUse } from "../domain/caseuse/UpdateSaleVerifiedCaseUse";
import { UpdateSaleDeliveryTypeCaseUse } from "../domain/caseuse/UpdateSaleDeliveryTypeCaseUse";
import { RegisterNewSaleCaseUse } from "../domain/caseuse/RegisterNewSaleCaseUse";
import { CancelUnverifiedSaleCaseUse } from "../domain/caseuse/CancelUnverifiedSaleCaseUse";
import { SessionSaleNotificationUserProvider } from "../data/repository/SessionSaleNotificationUserProvider";
import { TelegramNotificatorImpl } from "../data/repository/TelegramNotificatorImpl";
import {productContainer} from "../../product/di/product.container";

const netTablesDB = infrastructureContainer.appwrite.tablesDB

const saleNetRepository = new SaleNetRepository(netTablesDB)
const saleOfflineFirstRepository = new SaleOfflineFirstRepository(saleNetRepository)
const saleNotificationUserProvider = new SessionSaleNotificationUserProvider(
    () => infrastructureContainer.appwrite.account.get()
)
const telegramNotificator = new TelegramNotificatorImpl()

const getSalesCaseUse = new GetSalesCaseUse(saleOfflineFirstRepository)
const createSaleCaseUse = new RegisterNewSaleCaseUse(
    saleOfflineFirstRepository,
    saleNotificationUserProvider,
    telegramNotificator,
    productContainer.repositories.offlineFirst
)
const updateSaleVerifiedCaseUse = new UpdateSaleVerifiedCaseUse(saleOfflineFirstRepository)
const updateSaleDeliveryTypeCaseUse = new UpdateSaleDeliveryTypeCaseUse(saleOfflineFirstRepository)
const cancelUnverifiedSaleCaseUse = new CancelUnverifiedSaleCaseUse(
    saleOfflineFirstRepository,
    productContainer.repositories.offlineFirst
)

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
        updateDeliveryType: updateSaleDeliveryTypeCaseUse,
        cancelUnverified: cancelUnverifiedSaleCaseUse
    }
}
