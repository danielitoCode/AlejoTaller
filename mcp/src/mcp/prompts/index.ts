import type { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { z } from "zod";

/**
 * Register MCP Prompts — Guided workflows for AI agents.
 */
export function registerPrompts(server: McpServer): void {
  // System prompt for Customer Assistant role
  server.prompt(
    "customer-assistant",
    "Configura el comportamiento del agente de IA como asistente de atención al cliente de AlejoTaller.",
    {
      customerName: z.string().optional().describe("Nombre del cliente para personalizar el trato"),
    },
    async (args) => {
      const nameGreeting = args.customerName ? ` ${args.customerName}` : "";
      return {
        messages: [
          {
            role: "user",
            content: {
              type: "text",
              text: `Eres el asistente virtual oficial de atención al cliente de AlejoTaller.
Tu objetivo es ayudar al cliente${nameGreeting} a:
1. Consultar el estado de sus pedidos (ventas/reservas) y perfil.
2. Explorar el catálogo de productos, categorías y promociones.
3. Crear nuevos tickets de soporte o enviar mensajes en sus conversaciones existentes.
4. Ayudarlo a cancelar o crear pedidos cuando lo solicite.

REGLAS DE SEGURIDAD IMPORTANTES:
- Antes de ejecutar cualquier herramienta de ESCRITURA con confirmación (como cancel_order o create_order), DEBES resumir la acción al cliente y pedir su confirmación explícita.
- NUNCA inventes IDs de productos, pedidos o clientes. Usa siempre la información obtenida a través de las herramientas MCP.
- Trata siempre al cliente con amabilidad y respeto.`,
            },
          },
        ],
      };
    }
  );
}
