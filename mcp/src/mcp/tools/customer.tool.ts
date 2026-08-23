import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { z } from "zod";
import type { CustomerService } from "../../services/customer.service.js";
import type { McpAuthContext } from "../../auth/context.js";

/**
 * Customer tools — Profile management.
 */
export function registerCustomerTools(
  server: McpServer,
  customerService: CustomerService,
  getAuthContext: (extra: unknown) => McpAuthContext
): void {
  // ─── get_my_profile ─────────────────────────────────────────────────────
  server.tool(
    "get_my_profile",
    "Obtiene la información del perfil del cliente autenticado actual (nombre, email, teléfono, etc.). No requiere parámetros.",
    {},
    async (_args, extra) => {
      try {
        const auth = getAuthContext(extra);
        const profile = await customerService.getMyProfile(auth);
        return {
          content: [
            {
              type: "text",
              text: JSON.stringify(profile, null, 2),
            },
          ],
        };
      } catch (err: unknown) {
        const message = err instanceof Error ? err.message : String(err);
        return {
          isError: true,
          content: [{ type: "text", text: `Error al obtener el perfil: ${message}` }],
        };
      }
    }
  );

  // ─── update_my_profile ──────────────────────────────────────────────────
  server.tool(
    "update_my_profile",
    "Actualiza la información permitida del perfil del cliente (nombre, teléfono, URL de foto).",
    {
      name: z.string().optional().describe("Nuevo nombre del cliente"),
      phone: z.string().optional().describe("Nuevo número de teléfono"),
      photoUrl: z.string().url().optional().describe("URL de la foto de perfil"),
    },
    async (args, extra) => {
      try {
        const auth = getAuthContext(extra);
        const updated = await customerService.updateMyProfile(auth, args);
        return {
          content: [
            {
              type: "text",
              text: JSON.stringify(
                {
                  message: "Perfil actualizado correctamente",
                  profile: updated,
                },
                null,
                2
              ),
            },
          ],
        };
      } catch (err: unknown) {
        const message = err instanceof Error ? err.message : String(err);
        return {
          isError: true,
          content: [
            { type: "text", text: `Error al actualizar el perfil: ${message}` },
          ],
        };
      }
    }
  );
}
