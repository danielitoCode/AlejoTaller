import type {CategoryRepository} from "../repository/CategoryRepository";
import type {Category} from "../entity/Category";

export class DeleteCategoryCaseUse {
    constructor(private readonly categoryRepository: CategoryRepository) {}
    async execute(id: string): Promise<void> {
        await this.categoryRepository.delete(id);
    }
}