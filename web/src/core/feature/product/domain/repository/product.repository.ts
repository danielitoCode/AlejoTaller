import type { Product } from "../entity/Product";

export interface ProductRepository {
    getAll(): Promise<Product[]>
    /** Cache local inmediato (opcional; offline-first). */
    getLocalAll?(): Promise<Product[]>
    getById(id: string): Promise<Product | null>
    /** Fuente de verdad remota para operaciones críticas de stock. */
    refreshFromRemote?(id: string): Promise<Product | null>
    getByCategory(categoryId: string): Promise<Product[]>
    create(product: Product): Promise<Product>
    update(id: string, product: Partial<Product>): Promise<Product>
    delete(id: string): Promise<void>

    /**
     * Core 1: incrementa reserved atómicamente en Appwrite y persiste
     * únicamente la respuesta remota en Dexie.
     */
    incrementReserved(id: string, quantity: number): Promise<Product | null>

    /** Core 1: decrementa reserved atómicamente en Appwrite (mínimo 0). */
    decrementReserved(id: string, quantity: number): Promise<Product | null>

    sync(): Promise<void>
}
