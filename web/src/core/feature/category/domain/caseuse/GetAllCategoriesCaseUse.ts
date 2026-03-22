import type {CategoryRepository} from "../repository/CategoryRepository";
import type {Category} from "../entity/Category";

export class GetAllCategoriesCaseUse {
    constructor(private readonly categoryRepository: CategoryRepository) {}
    async execute(): Promise<Category[]> {
        return await this.categoryRepository.getAll()
    }
}