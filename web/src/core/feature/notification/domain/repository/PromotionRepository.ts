import type {Promotion} from "../entity/Promotion";

export interface PromotionRepository {
    getAll(): Promise<Promotion[]>

    getActive(now: number): Promise<Promotion[]>
}