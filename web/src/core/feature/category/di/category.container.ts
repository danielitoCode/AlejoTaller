import {infrastructureContainer} from "../../../infrastructure/di/infrastructure.container";
import {CategoryNetRepository} from "../data/repository/category.net.repository";
import {CategoryTursoRepository} from "../data/repository/category.turso.repository";
import {CategoryOfflineFirstRepository} from "../data/repository/category.offline-first.repository";
import {DeleteCategoryCaseUse} from "../domain/caseuse/DeleteCategoryCaseUse";
import {GetAllCategoriesCaseUse} from "../domain/caseuse/GetAllCategoriesCaseUse";
import {GetCategoryByIdCaseUse} from "../domain/caseuse/GetCategoryByIdCaseUse";
import {ModifyCategoryCaseUse} from "../domain/caseuse/ModifyCategoryCaseUse";
import {SaveCategoryCaseUse} from "../domain/caseuse/SaveCategoryCaseUse";
import {isTursoDataProvider} from "../../../infrastructure/turso/turso.client";

const database = infrastructureContainer.appwrite.databases

const categoryNetRepository = isTursoDataProvider()
    ? (new CategoryTursoRepository() as unknown as CategoryNetRepository)
    : new CategoryNetRepository(database)

const categoryOfflineFirstRepository = new CategoryOfflineFirstRepository(categoryNetRepository)

const deleteCategoryCaseUse = new DeleteCategoryCaseUse(categoryOfflineFirstRepository)
const getAllCategoriesCaseUse = new GetAllCategoriesCaseUse(categoryOfflineFirstRepository)
const getCategoryByIdCaseUse = new GetCategoryByIdCaseUse(categoryOfflineFirstRepository)
const modifyCategoryCaseUse = new ModifyCategoryCaseUse(categoryOfflineFirstRepository)
const saveCategoryCaseUse = new SaveCategoryCaseUse(categoryOfflineFirstRepository)

export const categoryContainer = {
    repositories: {
        net: categoryNetRepository,
        offlineFirst: categoryOfflineFirstRepository
    },
    useCases: {
        getAll: getAllCategoriesCaseUse,
        getById: getCategoryByIdCaseUse,
        create: saveCategoryCaseUse,
        update: modifyCategoryCaseUse,
        delete: deleteCategoryCaseUse
    }
}
