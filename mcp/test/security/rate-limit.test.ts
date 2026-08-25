import { describe, it, expect, beforeEach } from "vitest";
import {
  checkRateLimit,
  resetRateLimitState,
} from "../../src/security/rate-limit.js";

describe("checkRateLimit", () => {
  beforeEach(() => {
    resetRateLimitState();
  });

  it("allows under limit", () => {
    const cfg = { maxRequests: 3, windowMs: 60_000 };
    expect(checkRateLimit("k1", cfg).allowed).toBe(true);
    expect(checkRateLimit("k1", cfg).allowed).toBe(true);
    expect(checkRateLimit("k1", cfg).remaining).toBe(1);
  });

  it("blocks when exceeded", () => {
    const cfg = { maxRequests: 2, windowMs: 60_000 };
    checkRateLimit("k2", cfg);
    checkRateLimit("k2", cfg);
    const blocked = checkRateLimit("k2", cfg);
    expect(blocked.allowed).toBe(false);
    expect(blocked.remaining).toBe(0);
  });

  it("isolates keys", () => {
    const cfg = { maxRequests: 1, windowMs: 60_000 };
    checkRateLimit("a", cfg);
    expect(checkRateLimit("b", cfg).allowed).toBe(true);
    expect(checkRateLimit("a", cfg).allowed).toBe(false);
  });
});
