import type {Category} from "../entity/Category";
import type {Product} from "../../../product/domain/entity/Product";

export interface CategoryRepository {
    getAll(): Promise<Category[]>

    getById(id: string): Promise<Category | null>

    create(category: Category): Promise<Category>

    update(id: string, category: Partial<Category>): Promise<Category>

    delete(id: string): Promise<void>
}