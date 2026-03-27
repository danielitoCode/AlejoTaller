// Database instance
import { infrastructureContainer } from "../../../infrastructure/di/infrastructure.container";
import { PromotionNetRepository } from "../data/repository/promotion.net.repository";
import { PromotionOfflineFirstRepository } from "../data/repository/promotion.offline-first.repository";
import { GetActivePromosCaseUse } from "../domain/caseuse/GetActivePromosCaseUse";
import { GetAllPromosCaseUse } from "../domain/caseuse/GetAllPromosCaseUse";

// Infrastructure instances
const database = infrastructureContainer.appwrite.databases;

// Data
const promoNetRepository = new PromotionNetRepository(database);
const promoOfflineFirstRepository = new PromotionOfflineFirstRepository(promoNetRepository);

// Domain
const getActivePromoCaseUse = new GetActivePromosCaseUse(promoOfflineFirstRepository);
const getAllPromoCaseUse = new GetAllPromosCaseUse(promoOfflineFirstRepository);

export const notificationContainer = {
    repositories: {
        promo: promoOfflineFirstRepository
    },
    useCases: {
        promo: {
            getActive: getActivePromoCaseUse.execute.bind(getActivePromoCaseUse),
            getAll: getAllPromoCaseUse.execute.bind(getAllPromoCaseUse)
        }
    }
};
