import type {ProductWriteDTO} from "../../data/mapper/Mappers";
import type {ProductOfflineFirstRepository} from "../../data/repository/product.offline-first.repository";
import type {User} from "../../../auth/domain/entity/User";
import type {Product} from "../entity/Product";
import type {ProductRepository} from "../repository/product.repository";


export class SaveProductCaseUse {
    constructor(private readonly productRepository: ProductRepository) {}

    async execute(newProduct: Product): Promise<void> {
        if (newProduct.categoryId == "") return
        await this.productRepository.create(newProduct)
    }
}