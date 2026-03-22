import type {Product} from "../entity/Product";

export interface ProductRepository {
    getAll(): Promise<Product[]>

    getById(id: string): Promise<Product | null>

    getByCategory(categoryId: string): Promise<Product[]>

    create(product: Product): Promise<Product>

    update(id: string, product: Partial<Product>): Promise<Product>

    delete(id: string): Promise<void>
}