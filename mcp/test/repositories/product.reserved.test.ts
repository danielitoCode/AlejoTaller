import { describe, it, expect, vi, beforeEach } from "vitest";
import { AppwriteProductRepository } from "../../src/infrastructure/appwrite/repositories/product.appwrite.repository.js";

describe("AppwriteProductRepository reserved atomic ops", () => {
  const getDocument = vi.fn();
  const listDocuments = vi.fn();
  const incrementDocumentAttribute = vi.fn();
  const decrementDocumentAttribute = vi.fn();

  const databases = {
    getDocument,
    listDocuments,
    incrementDocumentAttribute,
    decrementDocumentAttribute,
  } as never;

  const repo = new AppwriteProductRepository(databases, "db-test");

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("incrementReserved calls Appwrite with maxReserved and maps result", async () => {
    incrementDocumentAttribute.mockResolvedValue({
      $id: "prod-1",
      name: "Filtro",
      existence: 10,
      reserved: 4,
      price: 100,
      photo_url: "",
      category_id: "cat-1",
      rating: 0,
    });

    const result = await repo.incrementReserved("prod-1", 2, 10);

    expect(incrementDocumentAttribute).toHaveBeenCalledWith(
      "db-test",
      "product",
      "prod-1",
      "reserved",
      2,
      10
    );
    expect(result).not.toBeNull();
    expect(result!.reserved).toBe(4);
    expect(result!.existence).toBe(10);
  });

  it("incrementReserved returns null when atomic op fails", async () => {
    incrementDocumentAttribute.mockRejectedValue(new Error("max exceeded"));
    // object fallback also fails
    incrementDocumentAttribute.mockRejectedValue(new Error("max exceeded"));

    const result = await repo.incrementReserved("prod-1", 5, 3);
    expect(result).toBeNull();
  });

  it("decrementReserved uses min 0", async () => {
    decrementDocumentAttribute.mockResolvedValue({
      $id: "prod-1",
      name: "Filtro",
      existence: 10,
      reserved: 1,
      price: 100,
      photo_url: "",
      category_id: "cat-1",
    });

    const result = await repo.decrementReserved("prod-1", 3);

    expect(decrementDocumentAttribute).toHaveBeenCalledWith(
      "db-test",
      "product",
      "prod-1",
      "reserved",
      3,
      0
    );
    expect(result!.reserved).toBe(1);
  });

  it("qty <= 0 skips mutation and reloads product", async () => {
    getDocument.mockResolvedValue({
      $id: "prod-1",
      name: "X",
      existence: 5,
      reserved: 2,
      price: 1,
      category_id: "c",
    });

    const result = await repo.incrementReserved("prod-1", 0, 5);
    expect(incrementDocumentAttribute).not.toHaveBeenCalled();
    expect(result!.id).toBe("prod-1");
  });

  it("refreshFromRemote delegates to getById", async () => {
    getDocument.mockResolvedValue({
      $id: "prod-2",
      name: "Y",
      existence: 8,
      reserved: 1,
      price: 2,
      category_id: "c",
    });

    const result = await repo.refreshFromRemote("prod-2");
    expect(getDocument).toHaveBeenCalled();
    expect(result!.id).toBe("prod-2");
  });
});
