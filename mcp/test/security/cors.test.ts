import { describe, it, expect } from "vitest";
import {
  parseCorsOrigins,
  resolveAllowOrigin,
  isOriginAllowed,
  buildCorsHeaders,
} from "../../src/security/cors.js";

describe("parseCorsOrigins", () => {
  it("defaults to * when empty", () => {
    expect(parseCorsOrigins(undefined)).toEqual(["*"]);
    expect(parseCorsOrigins("")).toEqual(["*"]);
  });

  it("splits comma list", () => {
    expect(parseCorsOrigins("https://a.com, https://b.com")).toEqual([
      "https://a.com",
      "https://b.com",
    ]);
  });
});

describe("resolveAllowOrigin", () => {
  it("returns * when allowlist is open", () => {
    expect(resolveAllowOrigin("https://evil.com", { allowedOrigins: ["*"] })).toBe(
      "*"
    );
  });

  it("echoes only allowlisted origin", () => {
    const cfg = { allowedOrigins: ["https://agent.example.com"] };
    expect(resolveAllowOrigin("https://agent.example.com", cfg)).toBe(
      "https://agent.example.com"
    );
    expect(resolveAllowOrigin("https://evil.com", cfg)).toBeNull();
  });
});

describe("isOriginAllowed", () => {
  it("allows missing Origin (non-browser)", () => {
    expect(isOriginAllowed(null, { allowedOrigins: ["https://a.com"] })).toBe(
      true
    );
  });

  it("rejects unknown browser origin", () => {
    expect(
      isOriginAllowed("https://evil.com", {
        allowedOrigins: ["https://a.com"],
      })
    ).toBe(false);
  });
});

describe("buildCorsHeaders", () => {
  it("sets Vary when specific origin", () => {
    const h = buildCorsHeaders("https://a.com");
    expect(h["Access-Control-Allow-Origin"]).toBe("https://a.com");
    expect(h["Vary"]).toBe("Origin");
  });
});
