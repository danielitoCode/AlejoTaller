import type {CategoryRepository} from "../repository/CategoryRepository";
import type {Category} from "../entity/Category";

export class ModifyCategoryCaseUse {
    constructor(private readonly categoryRepository: CategoryRepository) {}
    async execute(id: string, data: Category): Promise<void> {
        await this.categoryRepository.update(id, data)
    }
}