import type {ProductRepository} from "../repository/product.repository";
import type {Product} from "../entity/Product";

export class GetProductByIdCaseUse {
    constructor(private readonly productRepository: ProductRepository) {}
    async execute(id: string): Promise<Product | null> {
        return await this.productRepository.getById(id)
    }
}