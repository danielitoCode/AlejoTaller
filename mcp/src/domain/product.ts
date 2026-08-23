/**
 * Domain — Product
 *
 * Maps to the `product` collection in Appwrite.
 * Stock model: existence = physical units; reserved = soft-hold units.
 * Sellable stock = existence - reserved.
 */
export interface Product {
  id: string;
  name: string;
  description: string;
  /** Physical units in warehouse (not decremented until VERIFIED) */
  existence: number;
  /** Units committed in UNVERIFIED orders (soft-hold) */
  reserved: number;
  price: number;
  photoUrl: string;
  categoryId: string;
  rating: number;
  createdAt: string | null;
}

/** Units actually available for purchase right now */
export function availableStock(product: Product): number {
  const existence = Math.max(0, Math.floor(product.existence));
  const reserved = Math.max(0, Math.floor(product.reserved));
  return Math.max(0, existence - reserved);
}

/** Public-facing product view — hides internal stock details */
export interface ProductPublicView {
  id: string;
  name: string;
  description: string;
  price: number;
  photoUrl: string;
  categoryId: string;
  rating: number;
  /** Whether the product is currently purchasable */
  inStock: boolean;
  availableUnits: number;
}

export function toPublicView(product: Product): ProductPublicView {
  const units = availableStock(product);
  return {
    id: product.id,
    name: product.name,
    description: product.description,
    price: product.price,
    photoUrl: product.photoUrl,
    categoryId: product.categoryId,
    rating: product.rating,
    inStock: units > 0,
    availableUnits: units,
  };
}
