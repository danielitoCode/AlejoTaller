import { type Databases, Query } from "node-appwrite";
import type { ICategoryRepository } from "../../../repositories/category.repository.js";
import type { Category, CategoryStatus } from "../../../domain/category.js";
import { COLLECTIONS } from "../config.js";

/**
 * Appwrite implementation of ICategoryRepository.
 */
export class AppwriteCategoryRepository implements ICategoryRepository {
  constructor(
    private readonly databases: Databases,
    private readonly databaseId: string
  ) {}

  async listAll(): Promise<Category[]> {
    const res = await this.databases.listDocuments(
      this.databaseId,
      COLLECTIONS.category,
      [Query.limit(100)]
    );
    return res.documents.map((d) => this.toCategory(d as unknown as AppwriteCategoryDoc));
  }

  async getById(categoryId: string): Promise<Category | null> {
    try {
      const doc = await this.databases.getDocument(
        this.databaseId,
        COLLECTIONS.category,
        categoryId
      );
      return this.toCategory(doc as unknown as AppwriteCategoryDoc);
    } catch {
      return null;
    }
  }

  private toCategory(doc: AppwriteCategoryDoc): Category {
    return {
      id: doc.$id,
      name: doc.name ?? "",
      description: doc.description ?? "",
      photoUrl: doc.photoUrl ?? doc.photo_url ?? null,
      status: (doc.status as CategoryStatus) ?? "active",
    };
  }
}

interface AppwriteCategoryDoc {
  $id: string;
  name?: string;
  description?: string;
  photoUrl?: string;
  photo_url?: string;
  status?: string;
}
