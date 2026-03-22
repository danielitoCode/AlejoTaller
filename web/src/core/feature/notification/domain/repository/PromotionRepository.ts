import type {Promotion} from "../entity/Promotion";
import type {Product} from "../../../product/domain/entity/Product";

export interface PromotionRepository {
    create(promotion: Promotion): Promise<void>

    getAll(): Promise<Promotion[]>

    getActive(now: number): Promise<Promotion[]>

    delete(id: string): Promise<void>
}