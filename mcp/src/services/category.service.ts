import type { ICategoryRepository } from "../repositories/category.repository.js";
import type { Category } from "../domain/category.js";

/**
 * Service — Category
 * Business logic for product categories.
 */
export class CategoryService {
  constructor(private readonly categoryRepo: ICategoryRepository) {}

  /**
   * List active categories for customers.
   */
  async listCategories(): Promise<Category[]> {
    const categories = await this.categoryRepo.listAll();
    return categories.filter((c) => c.status === "active");
  }

  /**
   * Get category by ID.
   */
  async getCategory(categoryId: string): Promise<Category | null> {
    return await this.categoryRepo.getById(categoryId);
  }
}
