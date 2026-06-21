import { describe, expect, it } from "vitest";
import {
    getPrimaryProductImageUrl,
    parseProductImageUrls
} from "../../../../../../core/feature/product/presentation/utils/product.images";

describe("parseProductImageUrls", () => {
    it("returns a single direct http image URL", () => {
        expect(parseProductImageUrls("https://example.com/product.jpg")).toEqual([
            "https://example.com/product.jpg"
        ]);
    });

    it("parses the versioned payload format", () => {
        expect(parseProductImageUrls(JSON.stringify({ version: 1, images: [" https://a.test/1.jpg ", "", 42, "https://a.test/2.jpg"] }))).toEqual([
            "https://a.test/1.jpg",
            "https://a.test/2.jpg"
        ]);
    });

    it("requires the versioned payload shape for JSON values", () => {
        expect(parseProductImageUrls(JSON.stringify({ images: ["https://a.test/1.jpg"] }))).toEqual([]);
        expect(parseProductImageUrls(JSON.stringify(["https://a.test/1.jpg"]))).toEqual([]);
    });

    it("returns an empty list for invalid or empty values", () => {
        expect(parseProductImageUrls(null)).toEqual([]);
        expect(parseProductImageUrls("not-json-or-url")).toEqual([]);
        expect(parseProductImageUrls(JSON.stringify({ version: 1, images: "https://a.test/1.jpg" }))).toEqual([]);
    });
});

describe("getPrimaryProductImageUrl", () => {
    it("returns the direct URL or the first URL from a versioned payload", () => {
        expect(getPrimaryProductImageUrl("https://example.com/product.jpg")).toBe("https://example.com/product.jpg");
        expect(getPrimaryProductImageUrl(JSON.stringify({ version: "2", images: ["https://a.test/1.jpg", "https://a.test/2.jpg"] }))).toBe("https://a.test/1.jpg");
    });

    it("returns null when no displayable URL exists", () => {
        expect(getPrimaryProductImageUrl(null)).toBeNull();
        expect(getPrimaryProductImageUrl(JSON.stringify({ version: 1, images: [] }))).toBeNull();
    });
});