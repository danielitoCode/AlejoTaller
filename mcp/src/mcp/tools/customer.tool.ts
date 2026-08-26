import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { z } from "zod";
import type { CustomerService } from "../../services/customer.service.js";
import { okJson, runAuthedTool, type AuthResolver } from "./barrel.js";

/**
 * Customer tools — perfil B2C.
 */
export function registerCustomerTools(
  server: McpServer,
  customerService: CustomerService,
  getAuthContext: AuthResolver
): void {
  server.tool(
    "get_my_profile",
    [
      "Obtiene el perfil del cliente autenticado en AlejoTaller (nombre, email, teléfono, foto, etc.).",
      "Úsala cuando el usuario diga: mi perfil, mis datos, quién soy, mi cuenta, mi teléfono, mi email.",
    ].join("\n"),
    {},
    async (_args, extra) =>
      runAuthedTool("get_my_profile", "Obtener perfil", extra, getAuthContext, async (auth) => {
        const profile = await customerService.getMyProfile(auth);
        return okJson(profile);
      })
  );

  server.tool(
    "update_my_profile",
    [
      "Actualiza datos permitidos del perfil en AlejoTaller (nombre, teléfono, URL de foto).",
      "Úsala cuando el usuario pida cambiar su nombre, teléfono o foto de perfil.",
    ].join("\n"),
    {
      name: z.string().min(1).optional().describe("Nuevo nombre del cliente"),
      phone: z.string().min(1).optional().describe("Nuevo número de teléfono"),
      photoUrl: z
        .string()
        .url()
        .optional()
        .describe("URL de la foto de perfil"),
    },
    async (args, extra) =>
      runAuthedTool(
        "update_my_profile",
        "Actualizar perfil",
        extra,
        getAuthContext,
        async (auth) => {
          const updated = await customerService.updateMyProfile(auth, {
            name: args.name,
            phone: args.phone,
            photoUrl: args.photoUrl,
          });
          return okJson({
            message: "Perfil actualizado correctamente",
            profile: updated,
          });
        }
      )
  );
}
