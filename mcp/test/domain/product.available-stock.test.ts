import { describe, it, expect } from "vitest";
import {
  availableStock,
  toPublicView,
  type Product,
} from "../../src/domain/product.js";

function product(partial: Partial<Product> & Pick<Product, "existence" | "reserved">): Product {
  return {
    id: "p1",
    name: "Test",
    description: "",
    price: 10,
    photoUrl: "",
    categoryId: "c1",
    rating: 0,
    createdAt: null,
    ...partial,
  };
}

describe("availableStock (Core 1)", () => {
  it("existence - reserved", () => {
    expect(availableStock(product({ existence: 10, reserved: 3 }))).toBe(7);
  });

  it("never negative", () => {
    expect(availableStock(product({ existence: 2, reserved: 5 }))).toBe(0);
  });

  it("floors fractional inputs", () => {
    expect(availableStock(product({ existence: 4.9, reserved: 1.2 }))).toBe(3);
  });

  it("toPublicView exposes availableUnits and inStock", () => {
    const view = toPublicView(product({ existence: 5, reserved: 5 }));
    expect(view.availableUnits).toBe(0);
    expect(view.inStock).toBe(false);

    const view2 = toPublicView(product({ existence: 5, reserved: 1 }));
    expect(view2.availableUnits).toBe(4);
    expect(view2.inStock).toBe(true);
  });
});
