import Dexie, {type Table} from "dexie";
import type {ProductDTO} from "../../feature/product/data/dto/ProductDTO";
import type {CategoryDTO} from "../../feature/category/data/dto/CategoryDTO";
import type {PromotionDTO} from "../../feature/notification/data/dto/PromotionDTO";
import type {SaleDTO} from "../../feature/sale/data/dto/SaleDTO";
import type { CupExchange } from "../../feature/exchange/domain/entity/CupExchange";

class AppDatabase extends Dexie {
    products!: Table<ProductDTO>
    categories!: Table<CategoryDTO>
    promotions!: Table<PromotionDTO>
    sales!: Table<SaleDTO>
    exchangeRates!: Table<CupExchange>

    constructor() {
        super("alejo-taller-business-db")

        this.version(1).stores({
            products: "$id, name, categoryId",
            categories: "$id, name",
            promotions: "$id, validUntilEpochMillis",
            sales: "$id, userId, verified"
        })

        this.version(2).stores({
            products: "$id, name, categoryId",
            categories: "$id, name",
            promotions: "$id, validUntilEpochMillis",
            sales: "$id, user_id, buy_state",
            exchanges: "$id, usd_reference",
        })

        this.version(3).stores({
            products: "$id, name, categoryId",
            categories: "$id, name",
            promotions: "$id, validUntilEpochMillis",
            sales: "$id, user_id, buy_state",
            exchangeRates: "id, updatedAt, source"
        })
    }
}

export const db = new AppDatabase()
