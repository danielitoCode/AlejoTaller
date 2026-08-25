import type {
  AgentConnectionResult,
  AgentMessage,
  AgentReply,
} from "../entity/AgentMessage";

/**
 * Port — Agent gateway (Mistral). MCP tools llegan en Fase 2.
 */
export interface AgentRepository {
  /** Lightweight probe (models retrieve / list). */
  checkConnection(): Promise<AgentConnectionResult>;

  /**
   * Send a user turn to the configured Mistral Agent and return assistant text.
   * @param history optional prior messages (user/assistant only for Fase 1)
   */
  sendMessage(
    userText: string,
    history?: ReadonlyArray<Pick<AgentMessage, "role" | "content">>
  ): Promise<AgentReply>;
}
