import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { z } from "zod";
import type { SupportService } from "../../services/support.service.js";
import type { McpAuthContext } from "../../auth/context.js";
import type { SupportReason } from "../../domain/support.js";

/**
 * Support tools — Manage customer tickets & chat conversations.
 */
export function registerSupportTools(
  server: McpServer,
  supportService: SupportService,
  getAuthContext: (extra: unknown) => McpAuthContext
): void {
  // ─── get_my_support_threads ─────────────────────────────────────────────
  server.tool(
    "get_my_support_threads",
    "Obtiene la lista de consultas o tickets de soporte creados por el cliente autenticado.",
    {},
    async (_args, extra) => {
      try {
        const auth = getAuthContext(extra);
        const threads = await supportService.getMyThreads(auth);
        return {
          content: [
            {
              type: "text",
              text: JSON.stringify(threads, null, 2),
            },
          ],
        };
      } catch (err: unknown) {
        const message = err instanceof Error ? err.message : String(err);
        return {
          isError: true,
          content: [{ type: "text", text: `Error al obtener hilos de soporte: ${message}` }],
        };
      }
    }
  );

  // ─── get_support_thread ──────────────────────────────────────────────────
  server.tool(
    "get_support_thread",
    "Obtiene la información y estado de un ticket o hilo de soporte específico.",
    {
      threadId: z.string().describe("ID del hilo de soporte"),
    },
    async (args, extra) => {
      try {
        const auth = getAuthContext(extra);
        const thread = await supportService.getThread(auth, args.threadId);
        return {
          content: [
            {
              type: "text",
              text: JSON.stringify(thread, null, 2),
            },
          ],
        };
      } catch (err: unknown) {
        const message = err instanceof Error ? err.message : String(err);
        return {
          isError: true,
          content: [{ type: "text", text: `Error al obtener el hilo de soporte: ${message}` }],
        };
      }
    }
  );

  // ─── get_thread_messages ────────────────────────────────────────────────
  server.tool(
    "get_thread_messages",
    "Obtiene el historial completo de mensajes dentro de un hilo de soporte del cliente.",
    {
      threadId: z.string().describe("ID del hilo de soporte"),
    },
    async (args, extra) => {
      try {
        const auth = getAuthContext(extra);
        const messages = await supportService.getThreadMessages(auth, args.threadId);
        return {
          content: [
            {
              type: "text",
              text: JSON.stringify(messages, null, 2),
            },
          ],
        };
      } catch (err: unknown) {
        const message = err instanceof Error ? err.message : String(err);
        return {
          isError: true,
          content: [{ type: "text", text: `Error al obtener mensajes: ${message}` }],
        };
      }
    }
  );

  // ─── create_support_thread ──────────────────────────────────────────────
  server.tool(
    "create_support_thread",
    "Abre una nueva consulta o ticket de soporte con el equipo de AlejoTaller.",
    {
      reason: z
        .enum(["soporte", "pregunta_tecnica", "facturacion", "otro"])
        .describe("Motivo de la consulta"),
      subject: z.string().describe("Asunto del ticket"),
      body: z.string().describe("Mensaje inicial describiendo el problema o duda"),
    },
    async (args, extra) => {
      try {
        const auth = getAuthContext(extra);
        const thread = await supportService.createThread(auth, {
          reason: args.reason as SupportReason,
          subject: args.subject,
          body: args.body,
        });

        return {
          content: [
            {
              type: "text",
              text: JSON.stringify(
                {
                  message: "Ticket de soporte creado con éxito.",
                  thread,
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
          content: [{ type: "text", text: `Error al crear el ticket: ${message}` }],
        };
      }
    }
  );

  // ─── post_support_message ───────────────────────────────────────────────
  server.tool(
    "post_support_message",
    "Envía una respuesta o mensaje adicional dentro de un ticket de soporte existente.",
    {
      threadId: z.string().describe("ID del hilo de soporte"),
      body: z.string().describe("Contenido del mensaje"),
    },
    async (args, extra) => {
      try {
        const auth = getAuthContext(extra);
        const message = await supportService.postMessage(auth, {
          threadId: args.threadId,
          body: args.body,
        });

        return {
          content: [
            {
              type: "text",
              text: JSON.stringify(
                {
                  message: "Mensaje enviado al soporte.",
                  chatMessage: message,
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
          content: [{ type: "text", text: `Error al enviar mensaje: ${message}` }],
        };
      }
    }
  );
}
