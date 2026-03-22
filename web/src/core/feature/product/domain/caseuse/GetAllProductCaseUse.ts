import type {ProductRepository} from "../repository/product.repository";
import type {Product} from "../entity/Product";

export class GetAllProductCaseUse {
    constructor(private readonly productRepository: ProductRepository) {}
    async execute(): Promise<Product[]> {
        return await this.productRepository.getAll()
    }
}