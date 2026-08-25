/** Domain — Agent conversation message (Fase 1: texto; tools en Fase 2). */

export type AgentRole = "user" | "assistant" | "system" | "tool";

export type AgentConnectionStatus = "unknown" | "ok" | "error" | "unconfigured";

export interface AgentMessage {
  id: string;
  role: AgentRole;
  content: string;
  createdAt: string;
}

export interface AgentConnectionResult {
  status: AgentConnectionStatus;
  /** Model or agent id used for the probe */
  modelId: string | null;
  message: string;
}

export interface AgentReply {
  message: AgentMessage;
  /** Provider raw id if any */
  providerId: string | null;
}
