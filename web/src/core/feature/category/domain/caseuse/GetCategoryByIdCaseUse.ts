import type {Sale} from "../../../sale/domain/entity/Sale";
import type {CategoryRepository} from "../repository/CategoryRepository";
import type {Category} from "../entity/Category";

export class GetCategoryByIdCaseUse {
    constructor(private readonly categoryRepository: CategoryRepository) {}
    async execute(id: string): Promise<Category | null> {
        return await this.categoryRepository.getById(id)
    }
}