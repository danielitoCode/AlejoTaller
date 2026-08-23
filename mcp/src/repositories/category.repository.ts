import type { Category } from "../domain/category.js";

/**
 * Repository interface — Category
 *
 * Maps to the `category` Appwrite collection.
 * Implementations: AppwriteCategoryRepository, MockCategoryRepository (tests)
 */
export interface ICategoryRepository {
  /** List all categories */
  listAll(): Promise<Category[]>;

  /** Get a single category by ID */
  getById(categoryId: string): Promise<Category | null>;
}
