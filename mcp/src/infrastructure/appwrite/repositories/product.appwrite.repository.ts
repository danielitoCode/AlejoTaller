import { type Databases, Query } from "node-appwrite";
import type { IProductRepository } from "../../../repositories/product.repository.js";
import type { Product } from "../../../domain/product.js";
import { COLLECTIONS } from "../config.js";

/**
 * Appwrite implementation of IProductRepository.
 * Soft-hold mutations mirror web `ProductNetRepository`:
 *   incrementDocumentAttribute(..., "reserved", qty, maxReserved)
 *   decrementDocumentAttribute(..., "reserved", qty, 0)
 */
export class AppwriteProductRepository implements IProductRepository {
  constructor(
    private readonly databases: Databases,
    private readonly databaseId: string
  ) {}

  async listAll(): Promise<Product[]> {
    const documents: Product[] = [];
    let cursor: string | null = null;
    const pageSize = 100;

    while (true) {
      const res = await this.databases.listDocuments(
        this.databaseId,
        COLLECTIONS.product,
        [
          Query.orderDesc("$createdAt"),
          Query.limit(pageSize),
          ...(cursor ? [Query.cursorAfter(cursor)] : []),
        ]
      );
      documents.push(
        ...res.documents.map((d) =>
          this.toProduct(d as unknown as AppwriteProductDoc)
        )
      );
      if (res.documents.length < pageSize) break;
      cursor = res.documents.at(-1)?.$id ?? null;
      if (!cursor) break;
    }

    return documents;
  }

  async getById(productId: string): Promise<Product | null> {
    try {
      const doc = await this.databases.getDocument(
        this.databaseId,
        COLLECTIONS.product,
        productId
      );
      return this.toProduct(doc as unknown as AppwriteProductDoc);
    } catch {
      return null;
    }
  }

  async refreshFromRemote(productId: string): Promise<Product | null> {
    return this.getById(productId);
  }

  async listByCategory(categoryId: string): Promise<Product[]> {
    const res = await this.databases.listDocuments(
      this.databaseId,
      COLLECTIONS.product,
      [Query.equal("category_id", categoryId), Query.limit(100)]
    );
    return res.documents.map((d) =>
      this.toProduct(d as unknown as AppwriteProductDoc)
    );
  }

  async incrementReserved(
    productId: string,
    quantity: number,
    maxReserved: number
  ): Promise<Product | null> {
    const qty = Math.floor(quantity);
    if (qty <= 0) {
      return this.getById(productId);
    }
    if (maxReserved < 0) {
      throw new Error("maxReserved debe ser >= 0");
    }

    try {
      const doc = await this.callIncrementReserved(productId, qty, maxReserved);
      return this.toProduct(doc);
    } catch (err) {
      console.error(
        `[AppwriteProductRepository] incrementReserved failed id=${productId} qty=${qty}:`,
        err instanceof Error ? err.message : String(err)
      );
      return null;
    }
  }

  async decrementReserved(
    productId: string,
    quantity: number
  ): Promise<Product | null> {
    const qty = Math.floor(quantity);
    if (qty <= 0) {
      return this.getById(productId);
    }

    try {
      const doc = await this.callDecrementReserved(productId, qty);
      return this.toProduct(doc);
    } catch (err) {
      console.error(
        `[AppwriteProductRepository] decrementReserved failed id=${productId} qty=${qty}:`,
        err instanceof Error ? err.message : String(err)
      );
      return null;
    }
  }

  /**
   * node-appwrite may expose positional (legacy) or object-style args.
   * Prefer positional to match web client SDK used in AlejoTaller.
   */
  private async callIncrementReserved(
    productId: string,
    quantity: number,
    maxReserved: number
  ): Promise<AppwriteProductDoc> {
    const db = this.databases as Databases & {
      incrementDocumentAttribute: (
        ...args: unknown[]
      ) => Promise<AppwriteProductDoc>;
    };

    try {
      return await db.incrementDocumentAttribute(
        this.databaseId,
        COLLECTIONS.product,
        productId,
        "reserved",
        quantity,
        maxReserved
      );
    } catch (positionalErr) {
      // Fallback: object params (newer SDKs)
      return await db.incrementDocumentAttribute({
        databaseId: this.databaseId,
        collectionId: COLLECTIONS.product,
        documentId: productId,
        attribute: "reserved",
        value: quantity,
        max: maxReserved,
      });
    }
  }

  private async callDecrementReserved(
    productId: string,
    quantity: number
  ): Promise<AppwriteProductDoc> {
    const db = this.databases as Databases & {
      decrementDocumentAttribute: (
        ...args: unknown[]
      ) => Promise<AppwriteProductDoc>;
    };

    try {
      return await db.decrementDocumentAttribute(
        this.databaseId,
        COLLECTIONS.product,
        productId,
        "reserved",
        quantity,
        0
      );
    } catch {
      return await db.decrementDocumentAttribute({
        databaseId: this.databaseId,
        collectionId: COLLECTIONS.product,
        documentId: productId,
        attribute: "reserved",
        value: quantity,
        min: 0,
      });
    }
  }

  private toProduct(doc: AppwriteProductDoc): Product {
    const existence = Math.max(
      0,
      Math.floor(Number(doc.existence ?? doc.status ?? 0))
    );
    const reserved = Math.max(0, Math.floor(Number(doc.reserved ?? 0)));

    return {
      id: doc.$id,
      name: doc.name ?? "",
      description: doc.description ?? "",
      existence,
      reserved,
      price: Number(doc.price ?? 0),
      photoUrl: doc.photoUrl ?? doc.photo_url ?? "",
      categoryId: doc.category_id ?? doc.categoryId ?? "",
      rating: Number(doc.rating ?? 0),
      createdAt: doc.$createdAt ?? null,
    };
  }
}

interface AppwriteProductDoc {
  $id: string;
  $createdAt?: string;
  name?: string;
  description?: string;
  existence?: number;
  /** Legacy alias some docs may still carry */
  status?: number;
  reserved?: number;
  price?: number;
  photoUrl?: string;
  photo_url?: string;
  category_id?: string;
  categoryId?: string;
  rating?: number;
}
