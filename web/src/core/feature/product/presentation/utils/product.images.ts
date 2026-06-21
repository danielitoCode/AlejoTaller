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

export function parseProductImageUrls(photoUrl: string | null | undefined): string[] {
    const normalizedPhotoUrl = photoUrl?.trim();

    if (!normalizedPhotoUrl) {
        return [];
    }

    if (normalizedPhotoUrl.startsWith("http://") || normalizedPhotoUrl.startsWith("https://")) {
        return [normalizedPhotoUrl];
    }

    try {
        const parsed = JSON.parse(normalizedPhotoUrl) as unknown;

        if (!isProductImagesPayload(parsed)) {
            return [];
        }

        return parsed.images
            .filter((imageUrl): imageUrl is string => typeof imageUrl === "string")
            .map((imageUrl) => imageUrl.trim())
            .filter((imageUrl) => imageUrl.length > 0);
    } catch {
        return [];
    }
}

export function getPrimaryProductImageUrl(photoUrl: string | null | undefined): string | null {
    return parseProductImageUrls(photoUrl)[0] ?? null;
}