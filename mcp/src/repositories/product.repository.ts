import type { Product } from "../domain/product.js";

/**
 * Repository interface — Product
 *
 * Maps to the `product` Appwrite collection.
 * Soft-hold Core 1: reserved is mutated only via atomic increment/decrement
 * (same contract as web ProductNetRepository / ProductOfflineFirstRepository).
 */
export interface IProductRepository {
  /** Get all products (paginated internally) */
  listAll(): Promise<Product[]>;

  /** Get a single product by ID (remote) */
  getById(productId: string): Promise<Product | null>;

  /**
   * Forced remote read for critical stock decisions (create_order soft-hold).
   * Alias semantics of web `refreshFromRemote`.
   */
  refreshFromRemote(productId: string): Promise<Product | null>;

  /** Get products belonging to a category */
  listByCategory(categoryId: string): Promise<Product[]>;

  /**
   * Atomically increment `reserved` on Appwrite.
   * `maxReserved` is typically current `existence` so reserved cannot exceed physical stock.
   * Returns updated product, or null if the atomic op is rejected / fails.
   */
  incrementReserved(
    productId: string,
    quantity: number,
    maxReserved: number
  ): Promise<Product | null>;

  /**
   * Atomically decrement `reserved` (floor 0).
   * Returns updated product, or null on failure.
   */
  decrementReserved(productId: string, quantity: number): Promise<Product | null>;
}
