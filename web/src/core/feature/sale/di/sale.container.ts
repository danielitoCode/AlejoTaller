import {infrastructureContainer} from "../../../infrastructure/di/infrastructure.container";
import {SaleNetRepository} from "../data/repository/sale.net.repository";
import {SaleOfflineFirstRepository} from "../data/repository/sale.offline-first.repository";
import {GetAllProductCaseUse} from "../../product/domain/caseuse/GetAllProductCaseUse";
import {GetSalesCaseUse} from "../domain/caseuse/GetSalesCaseUse";
import { UpdateSaleVerifiedCaseUse } from "../domain/caseuse/UpdateSaleVerifiedCaseUse";

// Infrastructure instance
const netDatabases= infrastructureContainer.appwrite.databases

// Data
const saleNetRepository = new SaleNetRepository(netDatabases)
const saleOfflineFirstRepository = new SaleOfflineFirstRepository(saleNetRepository)

// Domain
const getSalesCaseUse = new GetSalesCaseUse(saleOfflineFirstRepository)
const updateSaleVerifiedCaseUse = new UpdateSaleVerifiedCaseUse(saleOfflineFirstRepository)

export const saleContainer = {
    repositories: {
        net: saleNetRepository,
        offlineFirst: saleOfflineFirstRepository
    },
    useCases: {
        getAll: getSalesCaseUse,
        updateVerified: updateSaleVerifiedCaseUse
    }
}
