import { describe, expect, it } from "vitest";
import {
    classifySessionMode,
    hasClearAuthenticatedProfile,
    resolveUserId
} from "../../../../../../core/feature/auth/presentation/util/profile-classification";
import {
    isAnonymousAppwriteUser,
    isGuestProvider
} from "../../../../../../core/feature/auth/presentation/util/gest-session";

describe("AUTH_POLICY profile classification", () => {
    it("treats clear email profile as authenticated", () => {
        const user = { id: "u1", email: "a@b.com", name: "Ana" };
        expect(hasClearAuthenticatedProfile(user)).toBe(true);
        expect(classifySessionMode(user)).toBe("authenticated");
        expect(resolveUserId(user)).toBe("u1");
    });

    it("treats empty email as visitor (anonymous Appwrite)", () => {
        const user = { id: "anon", email: "", name: "" };
        expect(isAnonymousAppwriteUser(user)).toBe(true);
        expect(hasClearAuthenticatedProfile(user)).toBe(false);
        expect(classifySessionMode(user)).toBe("visitor");
    });

    it("treats null / missing id as visitor", () => {
        expect(hasClearAuthenticatedProfile(null)).toBe(false);
        expect(hasClearAuthenticatedProfile({ email: "a@b.com" })).toBe(false);
        expect(classifySessionMode(undefined)).toBe("visitor");
    });

    it("guest providers are always visitors even with email", () => {
        expect(isGuestProvider("guest")).toBe(true);
        expect(isGuestProvider("ANONYMOUS")).toBe(true);
        expect(
            hasClearAuthenticatedProfile({
                id: "1",
                email: "x@y.com",
                provider: "visitor"
            })
        ).toBe(false);
        expect(
            classifySessionMode({ id: "1", email: "x@y.com", provider: "guest" })
        ).toBe("visitor");
    });

    it("resolves $id as fallback user id", () => {
        expect(resolveUserId({ $id: "appwrite-id", email: "a@b.com" })).toBe("appwrite-id");
    });
});
