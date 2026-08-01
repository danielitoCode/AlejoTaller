import { isAnonymousAppwriteUser, isGuestProvider } from "./gest-session";

/**
 * Classifies the current Appwrite account into a clear session mode.
 * Policy: if the profile is not clearly authenticated, treat as visitor (guest).
 *
 * Clear authenticated profile requires a non-empty email (or explicit non-guest provider).
 * Anonymous / empty-email / guest-provider accounts are always visitors.
 */
export type SessionMode = "authenticated" | "visitor";

export type ClassifiableUser = {
    id?: string | null;
    $id?: string | null;
    email?: string | null;
    name?: string | null;
    provider?: string | null;
    role?: string | null;
} | null | undefined;

export function hasClearAuthenticatedProfile(user: ClassifiableUser): boolean {
    if (!user) return false;

    const id = (user.id ?? user.$id ?? "").toString().trim();
    if (!id) return false;

    if (isGuestProvider(user.provider)) return false;
    if (isAnonymousAppwriteUser(user)) return false;

    const email = typeof user.email === "string" ? user.email.trim() : "";
    // Clear client/admin profile must have an email identity
    if (!email) return false;

    return true;
}

export function classifySessionMode(user: ClassifiableUser): SessionMode {
    return hasClearAuthenticatedProfile(user) ? "authenticated" : "visitor";
}

export function resolveUserId(user: ClassifiableUser): string | null {
    if (!user) return null;
    const id = (user.id ?? user.$id ?? "").toString().trim();
    return id || null;
}
