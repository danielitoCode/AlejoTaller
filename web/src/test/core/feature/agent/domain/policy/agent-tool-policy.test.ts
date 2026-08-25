import { describe, it, expect } from "vitest";
import {
  assertMcpToolAllowed,
  isMcpToolAllowedForGuest,
} from "../../../../../../core/feature/agent/domain/policy/agent-tool-policy";

describe("agent-tool-policy guest", () => {
  it("allows catalog tools for guest", () => {
    expect(isMcpToolAllowedForGuest("list_products")).toBe(true);
    expect(isMcpToolAllowedForGuest("get_product")).toBe(true);
  });

  it("denies auth tools for guest", () => {
    expect(isMcpToolAllowedForGuest("get_my_profile")).toBe(false);
    expect(isMcpToolAllowedForGuest("create_order")).toBe(false);
  });

  it("assert throws for guest on auth tool", () => {
    expect(() => assertMcpToolAllowed("get_my_orders", true)).toThrow(/invitados/i);
  });

  it("assert allows any tool when not guest", () => {
    expect(() => assertMcpToolAllowed("create_order", false)).not.toThrow();
  });
});
