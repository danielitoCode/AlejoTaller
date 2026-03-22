import type {ProductOfflineFirstRepository} from "../../data/repository/product.offline-first.repository";
import type {Product} from "../entity/Product";
import type {ProductRepository} from "../repository/product.repository";

export class UpdateProductPriceCaseUse {
    constructor(private readonly productRepository: ProductRepository) {}
    async execute(newPrice: number, product: Product): Promise<void>  {
        if(newPrice <= 0) throw new Error("Price must be greater than 0");
        product.price = newPrice
        await this.productRepository.update(product.id,product)
    }
}