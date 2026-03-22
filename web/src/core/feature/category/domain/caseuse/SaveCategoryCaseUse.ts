import type {CategoryRepository} from "../repository/CategoryRepository";
import type {Category} from "../entity/Category";

export class SaveCategoryCaseUse {
    constructor(private categoryRepository: CategoryRepository) {}
    async execute(data: Category): Promise<void> {
        await this.categoryRepository.create(data)
    }
}