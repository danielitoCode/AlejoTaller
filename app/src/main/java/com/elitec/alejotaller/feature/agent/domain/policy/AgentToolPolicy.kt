package com.elitec.alejotaller.feature.agent.domain.policy

/**
 * Guest / anonymous: only catalog + system MCP tools (Fase 2).
 */
object AgentToolPolicy {
    val guestAllowedMcpTools: Set<String> = setOf(
        "ping_customer_mcp",
        "get_server_info",
        "list_products",
        "get_product",
        "list_categories",
        "get_category",
        "list_active_promotions",
    )

    fun isAllowedForGuest(toolName: String): Boolean =
        toolName in guestAllowedMcpTools

    fun assertAllowed(toolName: String, isGuest: Boolean) {
        if (!isGuest) return
        require(isAllowedForGuest(toolName)) {
            "Tool \"$toolName\" no está disponible para invitados. Inicia sesión para usar esta función."
        }
    }
}
