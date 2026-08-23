import { describe, it, expect } from "vitest";
import { TOOL_POLICIES, getToolPolicy } from "../../src/policies/tool-policy.js";

describe("Tool Policy & Authorization Rules", () => {
  it("Read-only tools should not require confirmation", () => {
    expect(TOOL_POLICIES["get_my_profile"]?.requiresConfirmation).toBe(false);
    expect(TOOL_POLICIES["get_my_orders"]?.requiresConfirmation).toBe(false);
    expect(TOOL_POLICIES["list_products"]?.requiresConfirmation).toBe(false);
  });

  it("Sensitive write tools MUST require confirmation", () => {
    expect(TOOL_POLICIES["cancel_order"]?.requiresConfirmation).toBe(true);
    expect(TOOL_POLICIES["cancel_order"]?.permission).toBe("WRITE");

    expect(TOOL_POLICIES["create_order"]?.requiresConfirmation).toBe(true);
    expect(TOOL_POLICIES["create_order"]?.permission).toBe("WRITE");
  });

  it("Public tools should not require authentication", () => {
    expect(TOOL_POLICIES["ping_customer_mcp"]?.requiresAuth).toBe(false);
    expect(TOOL_POLICIES["list_products"]?.requiresAuth).toBe(false);
    expect(TOOL_POLICIES["list_categories"]?.requiresAuth).toBe(false);
    expect(TOOL_POLICIES["list_active_promotions"]?.requiresAuth).toBe(false);
  });

  it("Customer-specific tools MUST require authentication", () => {
    expect(TOOL_POLICIES["get_my_profile"]?.requiresAuth).toBe(true);
    expect(TOOL_POLICIES["get_my_orders"]?.requiresAuth).toBe(true);
    expect(TOOL_POLICIES["cancel_order"]?.requiresAuth).toBe(true);
    expect(TOOL_POLICIES["get_my_support_threads"]?.requiresAuth).toBe(true);
  });

  it("Unknown tools fall back to safe default", () => {
    const unknown = getToolPolicy("non_existent_tool");
    expect(unknown.permission).toBe("READ");
    expect(unknown.requiresAuth).toBe(true);
    expect(unknown.requiresConfirmation).toBe(false);
  });
});
