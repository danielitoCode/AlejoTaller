export const APPWRITE_COLLECTIONS = {
    product: "product",
    category: "category",
    promotion: "promotions"
} as const;

export const PUBLIC_VISITOR_COLLECTIONS = [
    APPWRITE_COLLECTIONS.product,
    APPWRITE_COLLECTIONS.category
] as const;

export type PublicVisitorCollection = (typeof PUBLIC_VISITOR_COLLECTIONS)[number];

export function isPublicVisitorCollection(collectionId: string): collectionId is PublicVisitorCollection {
    return (PUBLIC_VISITOR_COLLECTIONS as readonly string[]).includes(collectionId);
}

export function isAppwritePermissionError(error: unknown): boolean {
    const candidate = error as { code?: unknown; type?: unknown } | null;
    const code = typeof candidate?.code === "number" ? candidate.code : null;
    const type = typeof candidate?.type === "string" ? candidate.type : "";

    return code === 401 || code === 403 || type.includes("unauthorized") || type.includes("forbidden");
}