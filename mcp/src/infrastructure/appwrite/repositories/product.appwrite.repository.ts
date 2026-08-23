import { type Databases, Query } from "node-appwrite";
import type { IProductRepository } from "../../../repositories/product.repository.js";
import type { Product } from "../../../domain/product.js";
import { COLLECTIONS } from "../config.js";

/**
 * Appwrite implementation of IProductRepository.
 */
export class AppwriteProductRepository implements IProductRepository {
  constructor(
    private readonly databases: Databases,
    private readonly databaseId: string
  ) {}

  async listAll(): Promise<Product[]> {
    const res = await this.databases.listDocuments(
      this.databaseId,
      COLLECTIONS.product,
      [Query.orderDesc("$createdAt"), Query.limit(100)]
    );
    return res.documents.map((d) => this.toProduct(d as unknown as AppwriteProductDoc));
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

  async listByCategory(categoryId: string): Promise<Product[]> {
    const res = await this.databases.listDocuments(
      this.databaseId,
      COLLECTIONS.product,
      [Query.equal("category_id", categoryId), Query.limit(100)]
    );
    return res.documents.map((d) => this.toProduct(d as unknown as AppwriteProductDoc));
  }

  private toProduct(doc: AppwriteProductDoc): Product {
    return {
      id: doc.$id,
      name: doc.name ?? "",
      description: doc.description ?? "",
      existence: Number(doc.existence ?? 0),
      reserved: Number(doc.reserved ?? 0),
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
  reserved?: number;
  price?: number;
  photoUrl?: string;
  photo_url?: string;
  category_id?: string;
  categoryId?: string;
  rating?: number;
}
