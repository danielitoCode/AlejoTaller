import {infrastructureContainer} from "../../../infrastructure/di/infrastructure.container";
import {SaleNetRepository} from "../data/repository/sale.net.repository";
import {SaleOfflineFirstRepository} from "../data/repository/sale.offline-first.repository";
import {GetSalesCaseUse} from "../domain/caseuse/GetSalesCaseUse";
import { UpdateSaleVerifiedCaseUse } from "../domain/caseuse/UpdateSaleVerifiedCaseUse";
import { CreateSaleCaseUse } from "../domain/caseuse/CreateSaleCaseUse";
import { UpdateSaleDeliveryTypeCaseUse } from "../domain/caseuse/UpdateSaleDeliveryTypeCaseUse";

// Infrastructure instance
const netDatabases= infrastructureContainer.appwrite.databases

// Data
const saleNetRepository = new SaleNetRepository(netDatabases)
const saleOfflineFirstRepository = new SaleOfflineFirstRepository(saleNetRepository)

// Domain
const getSalesCaseUse = new GetSalesCaseUse(saleOfflineFirstRepository)
const createSaleCaseUse = new CreateSaleCaseUse(saleOfflineFirstRepository)
const updateSaleVerifiedCaseUse = new UpdateSaleVerifiedCaseUse(saleOfflineFirstRepository)
const updateSaleDeliveryTypeCaseUse = new UpdateSaleDeliveryTypeCaseUse(saleOfflineFirstRepository)

export const saleContainer = {
    repositories: {
        net: saleNetRepository,
        offlineFirst: saleOfflineFirstRepository
    },
    useCases: {
        getAll: getSalesCaseUse,
        create: createSaleCaseUse,
        updateVerified: updateSaleVerifiedCaseUse,
        updateDeliveryType: updateSaleDeliveryTypeCaseUse
    }
}
