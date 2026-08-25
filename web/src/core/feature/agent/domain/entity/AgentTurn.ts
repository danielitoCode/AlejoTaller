/** Domain — one agent turn outcome (chat + optional MCP tool). */

import type { AgentReply } from "./AgentMessage";
import type { McpToolCallResult } from "./McpTypes";

export type AgentTurnKind = "message" | "needs_confirmation" | "tool_result";

export interface AgentPendingTool {
  name: string;
  args: Record<string, unknown>;
  reason: string;
}

export interface AgentTurnResult {
  kind: AgentTurnKind;
  /** Assistant reply (message or post-tool summary). */
  reply: AgentReply | null;
  /** Write tool blocked until UI sets userConfirmed. */
  pendingTool: AgentPendingTool | null;
  /** Result of an executed MCP tool this turn. */
  toolResult: McpToolCallResult | null;
}

export function agentTurnMessage(reply: AgentReply): AgentTurnResult {
  return {
    kind: "message",
    reply,
    pendingTool: null,
    toolResult: null,
  };
}

export function agentTurnNeedsConfirmation(
  pending: AgentPendingTool
): AgentTurnResult {
  return {
    kind: "needs_confirmation",
    reply: null,
    pendingTool: pending,
    toolResult: null,
  };
}

export function agentTurnToolResult(
  toolResult: McpToolCallResult,
  reply: AgentReply | null
): AgentTurnResult {
  return {
    kind: "tool_result",
    reply,
    pendingTool: null,
    toolResult,
  };
}
