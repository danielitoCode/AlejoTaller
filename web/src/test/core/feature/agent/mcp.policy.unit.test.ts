import { describe, expect, it } from "vitest";
import {
  assertMcpToolAllowed,
  assertMcpWriteConfirmed,
  isMcpToolAllowedForGuest,
  isMcpWriteToolRequiringConfirmation,
} from "../../../../core/feature/agent/domain/policy/agent-tool-policy";

describe("agent MCP policy", () => {
  it("guest puede list_products", () => {
    expect(isMcpToolAllowedForGuest("list_products")).toBe(true);
    expect(() => assertMcpToolAllowed("list_products", true)).not.toThrow();
  });

  it("guest no puede create_order", () => {
    expect(isMcpToolAllowedForGuest("create_order")).toBe(false);
    expect(() => assertMcpToolAllowed("create_order", true)).toThrow(/invitados/);
  });

  it("create_order requiere confirmación", () => {
    expect(isMcpWriteToolRequiringConfirmation("create_order")).toBe(true);
    expect(() => assertMcpWriteConfirmed("create_order", false)).toThrow(/confirmación/);
    expect(() => assertMcpWriteConfirmed("create_order", true)).not.toThrow();
  });

  it("list_products no requiere confirmación", () => {
    expect(isMcpWriteToolRequiringConfirmation("list_products")).toBe(false);
    expect(() => assertMcpWriteConfirmed("list_products", false)).not.toThrow();
  });
});
