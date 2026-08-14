type ProductImagesPayload = {
    version: number | string;
    images: string[];
};

function isProductImagesPayload(value: unknown): value is ProductImagesPayload {
    if (!value || typeof value !== "object") {
        return false;
    }

    const payload = value as Partial<ProductImagesPayload>;

    return (
        (typeof payload.version === "number" || typeof payload.version === "string") &&
        Array.isArray(payload.images)
    );
}

function coerceToStringList(value: unknown): string[] {
    if (value == null) return [];

    if (Array.isArray(value)) {
        return value
            .filter((v): v is string => typeof v === "string")
            .map((v) => v.trim())
            .filter((v) => v.length > 0);
    }

    if (typeof value === "object") {
        const record = value as Record<string, unknown>;
        if ("photoUrl" in record) return coerceToStringList(record.photoUrl);
        if ("photo_url" in record) return coerceToStringList(record.photo_url);
        if (isProductImagesPayload(value)) {
            return value.images
                .filter((imageUrl): imageUrl is string => typeof imageUrl === "string")
                .map((imageUrl) => imageUrl.trim())
                .filter((imageUrl) => imageUrl.length > 0);
        }
        return [];
    }

    if (typeof value !== "string") {
        return [];
    }

    const normalized = value.trim();
    if (!normalized) return [];

    if (normalized.startsWith("http://") || normalized.startsWith("https://")) {
        return [normalized];
    }

    try {
        const parsed = JSON.parse(normalized) as unknown;
        return coerceToStringList(parsed);
    } catch {
        return [];
    }
}

/** Acepta string | JSON versionado | array | objeto producto (defensivo). */
export function parseProductImageUrls(photoUrl: unknown): string[] {
    return coerceToStringList(photoUrl);
}

export function getPrimaryProductImageUrl(photoUrl: unknown): string | null {
    return parseProductImageUrls(photoUrl)[0] ?? null;
}
