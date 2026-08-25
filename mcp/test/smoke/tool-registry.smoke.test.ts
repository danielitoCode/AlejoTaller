import { describe, it, expect } from "vitest";
import { TOOL_POLICIES, getToolPolicy } from "../../src/policies/tool-policy.js";

/** Canonical tool list — must stay in sync with register*Tools */
const EXPECTED_TOOLS = [
  "ping_customer_mcp",
  "get_server_info",
  "get_my_profile",
  "update_my_profile",
  "get_my_orders",
  "get_order",
  "cancel_order",
  "create_order",
  "list_products",
  "get_product",
  "list_categories",
  "get_category",
  "list_active_promotions",
  "get_my_support_threads",
  "get_support_thread",
  "get_thread_messages",
  "create_support_thread",
  "post_support_message",
] as const;

describe("tool registry smoke", () => {
  it("TOOL_POLICIES covers all 18 customer tools", () => {
    const keys = Object.keys(TOOL_POLICIES).sort();
    expect(keys).toEqual([...EXPECTED_TOOLS].sort());
    expect(keys).toHaveLength(18);
  });

  it("create_order and cancel_order require confirmation + auth + WRITE", () => {
    for (const name of ["create_order", "cancel_order"] as const) {
      const p = getToolPolicy(name);
      expect(p.requiresConfirmation).toBe(true);
      expect(p.requiresAuth).toBe(true);
      expect(p.permission).toBe("WRITE");
    }
  });

  it("public catalog tools do not require auth", () => {
    for (const name of [
      "list_products",
      "get_product",
      "list_categories",
      "get_category",
      "list_active_promotions",
      "ping_customer_mcp",
      "get_server_info",
    ] as const) {
      expect(getToolPolicy(name).requiresAuth).toBe(false);
    }
  });

  it("unknown tool defaults to requiresAuth true", () => {
    const p = getToolPolicy("not_a_real_tool");
    expect(p.requiresAuth).toBe(true);
    expect(p.permission).toBe("READ");
  });
});
