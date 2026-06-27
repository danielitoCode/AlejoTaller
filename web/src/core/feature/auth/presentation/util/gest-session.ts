const GUEST_PROVIDERS = new Set(["guest", "anonymous", "visitor", "invitado"]);

export function isGuestProvider(provider: unknown): boolean {
    return typeof provider === "string" && GUEST_PROVIDERS.has(provider.trim().toLowerCase());
}

export function isAnonymousAppwriteUser(user: unknown): boolean {
    const candidate = user as { email?: unknown; provider?: unknown } | null;
    if (!candidate) return false;
    if (isGuestProvider(candidate.provider)) return true;

    return typeof candidate.email === "string" && candidate.email.trim() === "";
}