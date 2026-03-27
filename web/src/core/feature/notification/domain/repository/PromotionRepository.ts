import type {Promotion} from "../entity/Promotion";
import type {Product} from "../../../product/domain/entity/Product";

export interface PromotionRepository {
    getAll(): Promise<Promotion[]>

    getActive(now: number): Promise<Promotion[]>
}