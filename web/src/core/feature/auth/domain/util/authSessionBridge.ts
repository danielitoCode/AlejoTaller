import type { AuthSession } from "../entity/AuthSession";

/** Forma mínima compatible con splash / admin-choice / home. */
export function userLikeFromAuthSession(session: AuthSession): {
    id: string;
    name: string;
    email: string;
    role: string;
    photo_url: string;
    sub: string;
} {
    const email = session.email ?? "";
    const name =
        session.displayName?.trim() ||
        (email.includes("@") ? email.split("@")[0] : "") ||
        "cliente";
    const role = session.roles[0] ?? "viewer";
    return {
        id: session.subject,
        name,
        email,
        role,
        photo_url: session.pictureUrl ?? "",
        sub: session.subject,
    };
}
