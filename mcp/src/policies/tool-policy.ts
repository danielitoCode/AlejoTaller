/**
 * Policies — Tool Policy
 *
 * Defines the permission level and behavior of each MCP tool.
 *
 * The AI agent reads tool annotations to understand which tools are
 * read-only vs. write, and which require explicit confirmation before
 * executing.
 *
 * Example: The AI should NOT cancel an order just because the user said
 * "I think I want to cancel". It should present the order details and
 * ask "¿Quieres que cancele el pedido #X?" before calling cancel_order.
 */

export type ToolPermission = "READ" | "WRITE";

export interface ToolPolicy {
  /** Whether this tool reads or modifies data */
  permission: ToolPermission;
  /**
   * If true, the AI MUST ask the user for explicit confirmation
   * before executing this tool. The AI should present a summary of
   * what will happen and wait for an unambiguous "yes".
   */
  requiresConfirmation: boolean;
  /** Whether an authenticated user is required to call this tool */
  requiresAuth: boolean;
}

/**
 * Central policy registry.
 * If a tool is not listed here, it defaults to READ + auth required.
 */
export const TOOL_POLICIES: Record<string, ToolPolicy> = {
  // ─── System ────────────────────────────────────────────────────────────
  ping_customer_mcp: {
    permission: "READ",
    requiresConfirmation: false,
    requiresAuth: false,
  },
  get_server_info: {
    permission: "READ",
    requiresConfirmation: false,
    requiresAuth: false,
  },

  // ─── Customer ──────────────────────────────────────────────────────────
  get_my_profile: {
    permission: "READ",
    requiresConfirmation: false,
    requiresAuth: true,
  },
  update_my_profile: {
    permission: "WRITE",
    requiresConfirmation: false, // low-risk: name/phone/photo only
    requiresAuth: true,
  },

  // ─── Orders ────────────────────────────────────────────────────────────
  get_my_orders: {
    permission: "READ",
    requiresConfirmation: false,
    requiresAuth: true,
  },
  get_order: {
    permission: "READ",
    requiresConfirmation: false,
    requiresAuth: true,
  },
  cancel_order: {
    permission: "WRITE",
    requiresConfirmation: true, // MUST confirm before cancelling
    requiresAuth: true,
  },
  create_order: {
    permission: "WRITE",
    requiresConfirmation: true, // MUST confirm before placing
    requiresAuth: true,
  },

  // ─── Products & Categories ─────────────────────────────────────────────
  list_products: {
    permission: "READ",
    requiresConfirmation: false,
    requiresAuth: false,
  },
  get_product: {
    permission: "READ",
    requiresConfirmation: false,
    requiresAuth: false,
  },
  list_categories: {
    permission: "READ",
    requiresConfirmation: false,
    requiresAuth: false,
  },
  get_category: {
    permission: "READ",
    requiresConfirmation: false,
    requiresAuth: false,
  },
  list_active_promotions: {
    permission: "READ",
    requiresConfirmation: false,
    requiresAuth: false,
  },

  // ─── Support ───────────────────────────────────────────────────────────
  get_my_support_threads: {
    permission: "READ",
    requiresConfirmation: false,
    requiresAuth: true,
  },
  get_support_thread: {
    permission: "READ",
    requiresConfirmation: false,
    requiresAuth: true,
  },
  get_thread_messages: {
    permission: "READ",
    requiresConfirmation: false,
    requiresAuth: true,
  },
  create_support_thread: {
    permission: "WRITE",
    requiresConfirmation: false,
    requiresAuth: true,
  },
  post_support_message: {
    permission: "WRITE",
    requiresConfirmation: false,
    requiresAuth: true,
  },
} as const;

/** Get the policy for a tool, with a safe default for unknown tools */
export function getToolPolicy(toolName: string): ToolPolicy {
  return (
    TOOL_POLICIES[toolName] ?? {
      permission: "READ",
      requiresConfirmation: false,
      requiresAuth: true,
    }
  );
}
