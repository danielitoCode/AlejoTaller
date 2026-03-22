import type {ProductRepository} from "../repository/product.repository";

export class DeleteProductCaseUse {
    constructor(private readonly productRepository: ProductRepository) {}
    async execute(id: string): Promise<void> {
        await this.productRepository.delete(id)
    }
}