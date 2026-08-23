import type { Product } from "../domain/product.js";

/**
 * Repository interface — Product
 *
 * Maps to the `product` Appwrite collection.
 * Implementations: AppwriteProductRepository, MockProductRepository (tests)
 */
export interface IProductRepository {
  /** Get all products (paginated internally) */
  listAll(): Promise<Product[]>;

  /** Get a single product by ID */
  getById(productId: string): Promise<Product | null>;

  /** Get products belonging to a category */
  listByCategory(categoryId: string): Promise<Product[]>;
}
