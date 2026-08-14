export interface SaleItemDTO {
    productId: string;
    productName?: string | null;
    quantity: number;
    /** Precio unitario efectivo congelado al crear la venta (Policy B) */
    price: number;
    /** Precio de lista al momento (auditoría); opcional */
    listUnitPrice?: number | null;
}
