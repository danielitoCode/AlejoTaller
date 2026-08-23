import type { IProductRepository } from "../repositories/product.repository.js";
import { type ProductPublicView, toPublicView } from "../domain/product.js";

/**
 * Service — Product
 * Business logic for customer-facing product queries.
 */
export class ProductService {
  constructor(private readonly productRepo: IProductRepository) {}

  /**
   * List all products in public view format.
   */
  async listProducts(): Promise<ProductPublicView[]> {
    const products = await this.productRepo.listAll();
    return products.map(toPublicView);
  }

  /**
   * Get single product in public view format.
   */
  async getProduct(productId: string): Promise<ProductPublicView | null> {
    const product = await this.productRepo.getById(productId);
    if (!product) return null;
    return toPublicView(product);
  }

  /**
   * List products by category in public view format.
   */
  async listByCategory(categoryId: string): Promise<ProductPublicView[]> {
    const products = await this.productRepo.listByCategory(categoryId);
    return products.map(toPublicView);
  }
}
