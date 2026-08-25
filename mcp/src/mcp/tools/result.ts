import { ZodError } from "zod";
import {
  AuthenticationError,
  AuthorizationError,
} from "../../auth/context.js";

export type ToolTextContent = {
  type: "text";
  text: string;
};

export type ToolSuccess = {
  content: ToolTextContent[];
};

export type ToolFailure = {
  isError: true;
  content: ToolTextContent[];
};

export type ToolResult = ToolSuccess | ToolFailure;

/** Successful JSON payload for the agent */
export function okJson(data: unknown): ToolSuccess {
  return {
    content: [
      {
        type: "text",
        text: JSON.stringify(data, null, 2),
      },
    ],
  };
}

/** Error payload (isError=true) */
export function failText(message: string): ToolFailure {
  return {
    isError: true,
    content: [{ type: "text", text: message }],
  };
}

/**
 * Strip internals that must not reach the agent (stack traces, Appwrite
 * project ids, raw SDK dumps).
 */
export function sanitizeErrorMessage(raw: string): string {
  let msg = raw.trim();

  // Drop multi-line stacks
  const firstLine = msg.split("\n")[0] ?? msg;
  msg = firstLine;

  // Common Appwrite / HTTP noise
  msg = msg.replace(/\bAppwriteException:\s*/gi, "");
  msg = msg.replace(/\bError:\s*/g, "");

  // Truncate runaway messages
  if (msg.length > 400) {
    msg = `${msg.slice(0, 397)}...`;
  }

  return msg || "Error desconocido";
}

/**
 * Map any thrown value to a safe ToolFailure.
 * Domain messages (stock, ownership) are preserved after sanitize.
 */
export function mapToolError(err: unknown, context: string): ToolFailure {
  if (err instanceof AuthenticationError) {
    return failText(
      `${context}: autenticación requerida. El host MCP debe enviar identidad de cliente (X-Customer-Id o JWT).`
    );
  }

  if (err instanceof AuthorizationError) {
    return failText(
      `${context}: no tienes permiso para esta operación sobre el recurso solicitado.`
    );
  }

  if (err instanceof ZodError) {
    const detail = err.issues
      .slice(0, 5)
      .map((i) => `${i.path.join(".") || "(root)"}: ${i.message}`)
      .join("; ");
    return failText(`${context}: parámetros inválidos. ${detail}`);
  }

  const raw = err instanceof Error ? err.message : String(err);
  const safe = sanitizeErrorMessage(raw);
  return failText(`${context}: ${safe}`);
}

/** Tools that require user confirmation (from policy) — documented for agents */
export function confirmationHint(toolName: string): string {
  return (
    ` IMPORTANTE: antes de llamar a ${toolName}, confirma explícitamente con el usuario ` +
    `(resume qué harás y espera un sí inequívoco).`
  );
}
