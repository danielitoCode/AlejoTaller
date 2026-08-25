import { describe, it, expect } from "vitest";
import {
  parseAuthMode,
  resolveAuthContext,
  resolveAuthContextFromMeta,
} from "../../src/auth/resolver.js";
import { extractBearerToken } from "../../src/auth/jwt.js";
import { AuthenticationError } from "../../src/auth/context.js";

describe("parseAuthMode", () => {
  it("defaults to jwt_or_header", () => {
    expect(parseAuthMode(undefined)).toBe("jwt_or_header");
    expect(parseAuthMode("weird")).toBe("jwt_or_header");
  });

  it("accepts known modes", () => {
    expect(parseAuthMode("jwt")).toBe("jwt");
    expect(parseAuthMode("header")).toBe("header");
  });
});

describe("extractBearerToken", () => {
  it("parses Bearer", () => {
    expect(extractBearerToken("Bearer abc.def")).toBe("abc.def");
    expect(extractBearerToken("bearer xyz")).toBe("xyz");
    expect(extractBearerToken("Basic x")).toBeNull();
    expect(extractBearerToken(null)).toBeNull();
  });
});

describe("resolveAuthContext header mode", () => {
  it("reads X-Customer-Id", async () => {
    const ctx = await resolveAuthContext(
      { "x-customer-id": "u1", "x-customer-name": "Ana" },
      { mode: "header" }
    );
    expect(ctx.userId).toBe("u1");
    expect(ctx.userName).toBe("Ana");
  });

  it("throws without id", async () => {
    await expect(
      resolveAuthContext({}, { mode: "header" })
    ).rejects.toBeInstanceOf(AuthenticationError);
  });

  it("jwt mode requires bearer", async () => {
    await expect(
      resolveAuthContext(
        { "x-customer-id": "u1" },
        { mode: "jwt" }
      )
    ).rejects.toThrow(/Bearer/);
  });
});

describe("resolveAuthContextFromMeta", () => {
  it("reads customerId", async () => {
    const ctx = await resolveAuthContextFromMeta(
      { customerId: "u2", customerName: "Bob" },
      { mode: "header" }
    );
    expect(ctx.userId).toBe("u2");
  });
});
