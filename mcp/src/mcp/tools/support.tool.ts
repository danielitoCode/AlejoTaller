import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { z } from "zod";
import type { SupportService } from "../../services/support.service.js";
import type { SupportReason } from "../../domain/support.js";
import { okJson, runAuthedTool, type AuthResolver } from "./barrel.js";

/**
 * Support tools — tickets del cliente autenticado.
 */
export function registerSupportTools(
  server: McpServer,
  supportService: SupportService,
  getAuthContext: AuthResolver
): void {
  server.tool(
    "get_my_support_threads",
    [
      "Lista los tickets de soporte del cliente autenticado en AlejoTaller.",
      "Úsala cuando el usuario pregunte por: mis tickets, mi soporte, mis consultas, estado de mi reclamación.",
    ].join("\n"),
    {},
    async (_args, extra) =>
      runAuthedTool(
        "get_my_support_threads",
        "Listar tickets",
        extra,
        getAuthContext,
        async (auth) => {
          const threads = await supportService.getMyThreads(auth);
          return okJson(threads);
        }
      )
  );

  server.tool(
    "get_support_thread",
    "Obtiene un ticket de soporte de AlejoTaller por ID (solo propios).",
    {
      threadId: z.string().min(1).describe("ID del hilo de soporte"),
    },
    async (args, extra) =>
      runAuthedTool(
        "get_support_thread",
        "Obtener ticket",
        extra,
        getAuthContext,
        async (auth) => {
          const thread = await supportService.getThread(auth, args.threadId);
          return okJson(thread);
        }
      )
  );

  server.tool(
    "get_thread_messages",
    "Historial de mensajes de un ticket propio en AlejoTaller.",
    {
      threadId: z.string().min(1).describe("ID del hilo de soporte"),
    },
    async (args, extra) =>
      runAuthedTool(
        "get_thread_messages",
        "Obtener mensajes",
        extra,
        getAuthContext,
        async (auth) => {
          const messages = await supportService.getThreadMessages(auth, args.threadId);
          return okJson(messages);
        }
      )
  );

  server.tool(
    "create_support_thread",
    [
      "Abre un nuevo ticket de soporte con el equipo de AlejoTaller.",
      "Úsala cuando el usuario pida ayuda, reporte un problema, o quiera contactar soporte del taller.",
    ].join("\n"),
    {
      reason: z
        .enum(["soporte", "pregunta_tecnica", "facturacion", "otro"])
        .describe("Motivo de la consulta"),
      subject: z.string().min(1).describe("Asunto del ticket"),
      body: z.string().min(1).describe("Mensaje inicial"),
    },
    async (args, extra) =>
      runAuthedTool(
        "create_support_thread",
        "Crear ticket",
        extra,
        getAuthContext,
        async (auth) => {
          const thread = await supportService.createThread(auth, {
            reason: args.reason as SupportReason,
            subject: args.subject,
            body: args.body,
          });
          return okJson({
            message: "Ticket de soporte creado con éxito.",
            thread,
          });
        }
      )
  );

  server.tool(
    "post_support_message",
    "Envía un mensaje en un ticket de soporte existente (propio) de AlejoTaller.",
    {
      threadId: z.string().min(1).describe("ID del hilo de soporte"),
      body: z.string().min(1).describe("Contenido del mensaje"),
    },
    async (args, extra) =>
      runAuthedTool(
        "post_support_message",
        "Enviar mensaje",
        extra,
        getAuthContext,
        async (auth) => {
          const message = await supportService.postMessage(auth, {
            threadId: args.threadId,
            body: args.body,
          });
          return okJson({
            message: "Mensaje enviado al soporte.",
            chatMessage: message,
          });
        }
      )
  );
}
