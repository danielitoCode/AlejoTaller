export type GoogleIdTokenProfile = {
    email: string;
    sub: string;
    name: string;
    picture: string;
    credential: string;
};

function base64UrlDecode(input: string): string {
    const normalized = input.replace(/-/g, "+").replace(/_/g, "/");
    const padded = normalized.padEnd(normalized.length + ((4 - (normalized.length % 4)) % 4), "=");
    return decodeURIComponent(
        atob(padded)
            .split("")
            .map((c) => "%" + c.charCodeAt(0).toString(16).padStart(2, "0"))
            .join("")
    );
}

export function parseGoogleIdToken(credential: string): GoogleIdTokenProfile {
    const parts = credential.split(".");
    if (parts.length < 2) {
        throw new Error("Credencial inv?lida");
    }

    const payloadJson = base64UrlDecode(parts[1] || "");
    const payload = JSON.parse(payloadJson) as Record<string, unknown>;

    const email = typeof payload.email === "string" ? payload.email : "";
    const sub = typeof payload.sub === "string" ? payload.sub : "";
    const name = typeof payload.name === "string" ? payload.name : "";
    const picture = typeof payload.picture === "string" ? payload.picture : "";

    if (!email || !sub) {
        throw new Error("Credencial incompleta");
    }

    return { email, sub, name, picture, credential };
}


