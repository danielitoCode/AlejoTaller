import { describe, expect, it, beforeEach } from "vitest";
import {
    clearWelcomeCompleted,
    hasCompletedWelcome,
    markWelcomeCompleted
} from "../../../../../core/infrastructure/presentation/navigation/first-visit";

describe("first-visit flag (AUTH_POLICY onboarding)", () => {
    beforeEach(() => {
        clearWelcomeCompleted();
    });

    it("starts as first visit", () => {
        expect(hasCompletedWelcome()).toBe(false);
    });

    it("marks completed after welcome flow", () => {
        markWelcomeCompleted();
        expect(hasCompletedWelcome()).toBe(true);
    });

    it("can be cleared for tests / reset", () => {
        markWelcomeCompleted();
        clearWelcomeCompleted();
        expect(hasCompletedWelcome()).toBe(false);
    });
});
