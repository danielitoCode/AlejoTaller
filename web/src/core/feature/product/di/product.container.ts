import {infrastructureContainer} from "../../../infrastructure/di/infrastructure.container";
import ProductNetRepository from "../data/repository/product.net.repository";
import {ProductOfflineFirstRepository} from "../data/repository/product.offline-first.repository";
import {GetAllProductCaseUse} from "../domain/caseuse/GetAllProductCaseUse";
import {GetProductByIdCaseUse} from "../domain/caseuse/GetProductByIdCaseUse";
import {SaveProductCaseUse} from "../domain/caseuse/SaveProductCaseUse";
import {DeletePromotionCaseUse} from "../../notification/domain/caseuse/DeletePromotionCaseUse";
import {DeleteProductCaseUse} from "../domain/caseuse/DeleteProductCaseUse";
import {UpdateProductPriceCaseUse} from "../domain/caseuse/UpdateProductPriceCaseUse";

// Database instance
const database = infrastructureContainer.appwrite.databases

// Data
const productNetRepository = new ProductNetRepository(database)
const productOfflineFirstRepository = new ProductOfflineFirstRepository(productNetRepository)

// Domain
const getAllProductsCaseUse = new GetAllProductCaseUse(productOfflineFirstRepository)
const deletedProductCaseUse = new DeleteProductCaseUse(productOfflineFirstRepository)
const getProductByIdCaseUse = new GetProductByIdCaseUse(productOfflineFirstRepository)
const modifyProductPriceCaseUse = new UpdateProductPriceCaseUse(productOfflineFirstRepository)
const saveProductCaseUse = new SaveProductCaseUse(productOfflineFirstRepository)

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
        delete: deletedProductCaseUse
    }
}