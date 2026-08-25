/**
 * Guest / anonymous: only catalog + system tools when MCP is wired (Fase 2).
 * Auth tools require a logged-in user (even if MCP is open for now).
 */

export const GUEST_ALLOWED_MCP_TOOLS = [
  "ping_customer_mcp",
  "get_server_info",
  "list_products",
  "get_product",
  "list_categories",
  "get_category",
  "list_active_promotions",
] as const;

export type GuestAllowedMcpTool = (typeof GUEST_ALLOWED_MCP_TOOLS)[number];

export function isMcpToolAllowedForGuest(toolName: string): boolean {
  return (GUEST_ALLOWED_MCP_TOOLS as readonly string[]).includes(toolName);
}

export function assertMcpToolAllowed(
  toolName: string,
  isGuest: boolean
): void {
  if (!isGuest) return;
  if (!isMcpToolAllowedForGuest(toolName)) {
    throw new Error(
      `Tool "${toolName}" no está disponible para invitados. Inicia sesión para usar esta función.`
    );
  }
}
