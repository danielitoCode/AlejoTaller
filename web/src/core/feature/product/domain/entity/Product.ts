export interface Product {
    id: string
    name: string
    description: string
    /** Unidades físicas en almacén (no descontadas hasta VERIFIED) */
    existence: number
    /** Unidades comprometidas en pedidos UNVERIFIED (soft-hold) */
    reserved: number
    price: number
    photoUrl: string
    categoryId: string
    rating?: number
    photoLocalResource?: number | null
    createdAtIso?: string
}

/** Stock vendible ahora = existence - reserved */
export function availableStock(product: Product): number {
    const existence = Math.max(0, Math.floor(product.existence ?? 0))
    const reserved = Math.max(0, Math.floor(product.reserved ?? 0))
    return Math.max(0, existence - reserved)
}

/**
 * Factory con validaciones (equivalente al init{})
 */
export function createProduct(product: Product): Product {
    if (!product.id || product.id.trim() === "") {
        throw new Error("The value of product identifier cannot be empty")
    }

    if (product.price < 0) {
        throw new Error("The price cannot be negative")
    }

    if (product.existence == null || Number.isNaN(product.existence) || product.existence < 0) {
        throw new Error("Product existence (stock) cannot be negative")
    }

    const reserved = product.reserved == null || Number.isNaN(product.reserved)
        ? 0
        : Math.floor(product.reserved)

    if (reserved < 0) {
        throw new Error("Product reserved stock cannot be negative")
    }

    return {
        rating: 0.0,
        ...product,
        existence: Math.floor(product.existence),
        reserved
    }
}
