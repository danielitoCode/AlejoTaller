import {infrastructureContainer} from "../../../infrastructure/di/infrastructure.container";
import ProductNetRepository from "../data/repository/product.net.repository";
import {ProductOfflineFirstRepository} from "../data/repository/product.offline-first.repository";
import {GetAllProductCaseUse} from "../domain/caseuse/GetAllProductCaseUse";
import {GetProductByIdCaseUse} from "../domain/caseuse/GetProductByIdCaseUse";
import {SaveProductCaseUse} from "../domain/caseuse/SaveProductCaseUse";
import {DeleteProductCaseUse} from "../domain/caseuse/DeleteProductCaseUse";
import {UpdateProductPriceCaseUse} from "../domain/caseuse/UpdateProductPriceCaseUse";
import {CheckAProductExistenceCaseUse} from "../domain/caseuse/CheckAProductExistenceCaseUse";
import {ReleaseSoftHoldCaseUse} from "../domain/caseuse/ReleaseSoftHoldCaseUse";
import {RefreshProductsByIdsCaseUse} from "../domain/caseuse/RefreshProductsByIdsCaseUse";

const database = infrastructureContainer.appwrite.databases

const productNetRepository = new ProductNetRepository(database)
const productOfflineFirstRepository = new ProductOfflineFirstRepository(productNetRepository)

const getAllProductsCaseUse = new GetAllProductCaseUse(productOfflineFirstRepository)
const deletedProductCaseUse = new DeleteProductCaseUse(productOfflineFirstRepository)
const getProductByIdCaseUse = new GetProductByIdCaseUse(productOfflineFirstRepository)
const modifyProductPriceCaseUse = new UpdateProductPriceCaseUse(productOfflineFirstRepository)
const saveProductCaseUse = new SaveProductCaseUse(productOfflineFirstRepository)
const checkAProductExistenceCaseUse = new CheckAProductExistenceCaseUse(productOfflineFirstRepository)
const releaseSoftHoldCaseUse = new ReleaseSoftHoldCaseUse(productOfflineFirstRepository)
const refreshProductsByIdsCaseUse = new RefreshProductsByIdsCaseUse(productOfflineFirstRepository)

export const productContainer = {
    repositories: {
        net: productNetRepository,
        offlineFirst: productOfflineFirstRepository
    },
    useCases: {
        getAll: getAllProductsCaseUse,
        getById: getProductByIdCaseUse,
        create: saveProductCaseUse,
        updatePrice: modifyProductPriceCaseUse,
        delete: deletedProductCaseUse,
        checkAProductExistence: checkAProductExistenceCaseUse,
        releaseSoftHold: releaseSoftHoldCaseUse,
        refreshByIds: refreshProductsByIdsCaseUse
    }
}
