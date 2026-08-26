/**
 * Presentation store — agent chat + MCP write confirmation gate (Fase 3).
 * Domain orchestration lives in RunAgentTurnCaseUse.
 */

import { get, writable } from "svelte/store";
import { agentContainer } from "../../di/agent.container";
import type { AgentPendingTool } from "../../domain/entity/AgentTurn";
import type { McpAuthContext } from "../../domain/entity/McpTypes";
import { infrastructureContainer } from "../../../../infrastructure/di/infrastructure.container";
import { logger } from "../../../../infrastructure/presentation/util/logger.service";

export type AgentChatBubbleRole = "user" | "assistant" | "system";

export interface AgentChatBubble {
  id: string;
  role: AgentChatBubbleRole;
  content: string;
  createdAt: string;
  /** Optional: tool name when this bubble follows a tool execution */
  toolName?: string;
  isError?: boolean;
}

interface AgentUiState {
  bubbles: AgentChatBubble[];
  sending: boolean;
  error: string | null;
  pendingTool: AgentPendingTool | null;
  confirming: boolean;
  connectionMessage: string | null;
}

const initial: AgentUiState = {
  bubbles: [],
  sending: false,
  error: null,
  pendingTool: null,
  confirming: false,
  connectionMessage: null,
};

function normalizeError(error: unknown): string {
  return error instanceof Error ? error.message : "Error inesperado";
}

function newId(): string {
  return `b_${Date.now()}_${Math.random().toString(36).slice(2, 9)}`;
}

function historyFromBubbles(
  bubbles: AgentChatBubble[]
): ReadonlyArray<{ role: "user" | "assistant"; content: string }> {
  return bubbles
    .filter((b) => b.role === "user" || b.role === "assistant")
    .map((b) => ({
      role: b.role as "user" | "assistant",
      content: b.content,
    }));
}

async function resolveAuthContext(): Promise<{
  auth: McpAuthContext | null;
  isGuest: boolean;
}> {
  try {
    const account = infrastructureContainer.appwrite.account;
    const user = await account.get();
    const labels = (user as { labels?: string[] }).labels ?? [];
    const isAnon =
      labels.includes("anonymous") ||
      (user as { provider?: string }).provider === "anonymous";

    if (isAnon) {
      return { auth: null, isGuest: true };
    }

    let jwt: string | null = null;
    try {
      const jwtRes = await account.createJWT();
      jwt = jwtRes.jwt;
    } catch (e) {
      logger.warn(
        `[agent] createJWT failed: ${e instanceof Error ? e.message : String(e)}`
      );
    }

    return {
      auth: {
        jwt,
        customerId: user.$id,
        customerName: user.name || null,
        customerEmail: user.email || null,
      },
      isGuest: false,
    };
  } catch {
    return { auth: null, isGuest: true };
  }
}

function createAgentStore() {
  const { subscribe, update, set } = writable<AgentUiState>(initial);

  async function sendMessage(text: string): Promise<void> {
    const trimmed = text.trim();
    if (!trimmed) return;

    const userBubble: AgentChatBubble = {
      id: newId(),
      role: "user",
      content: trimmed,
      createdAt: new Date().toISOString(),
    };

    update((s) => ({
      ...s,
      bubbles: [...s.bubbles, userBubble],
      sending: true,
      error: null,
    }));

    try {
      const { auth, isGuest } = await resolveAuthContext();
      const state = get({ subscribe });
      const history = historyFromBubbles(state.bubbles.slice(0, -1));

      const turn = await agentContainer.useCases.runAgentTurn({
        text: trimmed,
        history,
        auth,
        isGuest,
      });

      if (turn.kind === "message" && turn.reply) {
        update((s) => ({
          ...s,
          bubbles: [
            ...s.bubbles,
            {
              id: turn.reply!.message.id || newId(),
              role: "assistant",
              content: turn.reply!.message.content,
              createdAt: turn.reply!.message.createdAt,
            },
          ],
        }));
      } else if (turn.kind === "needs_confirmation" && turn.pendingTool) {
        update((s) => ({ ...s, pendingTool: turn.pendingTool }));
      } else if (turn.kind === "tool_result") {
        appendToolTurn(turn);
      }
    } catch (e) {
      const msg = normalizeError(e);
      update((s) => ({ ...s, error: msg }));
      logger.error(msg);
    } finally {
      update((s) => ({ ...s, sending: false }));
    }
  }

  /**
   * UI (or future LLM orchestrator) requests an MCP tool.
   * Write tools return needs_confirmation until confirmPendingTool().
   */
  async function requestTool(
    name: string,
    args: Record<string, unknown> = {},
    userConfirmed = false
  ): Promise<void> {
    update((s) => ({
      ...s,
      sending: true,
      error: null,
      confirming: userConfirmed,
    }));

    try {
      const { auth, isGuest } = await resolveAuthContext();
      const state = get({ subscribe });
      const history = historyFromBubbles(state.bubbles);

      const turn = await agentContainer.useCases.runAgentTurn({
        pendingTool: { name, args },
        auth,
        isGuest,
        userConfirmed,
        history,
      });

      if (turn.kind === "needs_confirmation" && turn.pendingTool) {
        update((s) => ({
          ...s,
          pendingTool: turn.pendingTool,
          confirming: false,
        }));
        return;
      }

      if (turn.kind === "tool_result") {
        update((s) => ({ ...s, pendingTool: null }));
        appendToolTurn(turn);
      }
    } catch (e) {
      const msg = normalizeError(e);
      update((s) => ({ ...s, error: msg, pendingTool: null }));
      logger.error(msg);
    } finally {
      update((s) => ({ ...s, sending: false, confirming: false }));
    }
  }

  async function confirmPendingTool(): Promise<void> {
    const state = get({ subscribe });
    const pending = state.pendingTool;
    if (!pending) return;
    await requestTool(pending.name, pending.args, true);
  }

  function cancelPendingTool(): void {
    update((s) => ({ ...s, pendingTool: null, confirming: false }));
  }

  function appendToolTurn(turn: {
    reply: { message: { id?: string; content: string; createdAt: string } } | null;
    toolResult: {
      toolName: string;
      isError: boolean;
      text: string;
    } | null;
  }): void {
    update((s) => {
      const next = [...s.bubbles];
      if (turn.toolResult) {
        next.push({
          id: newId(),
          role: "system",
          content: turn.toolResult.text,
          createdAt: new Date().toISOString(),
          toolName: turn.toolResult.toolName,
          isError: turn.toolResult.isError,
        });
      }
      if (turn.reply) {
        next.push({
          id: turn.reply.message.id || newId(),
          role: "assistant",
          content: turn.reply.message.content,
          createdAt: turn.reply.message.createdAt,
        });
      }
      return { ...s, bubbles: next };
    });
  }

  function clearError(): void {
    update((s) => ({ ...s, error: null }));
  }

  function clearChat(): void {
    set({ ...initial });
  }

  return {
    subscribe,
    sendMessage,
    requestTool,
    confirmPendingTool,
    cancelPendingTool,
    clearError,
    clearChat,
  };
}

export const agentStore = createAgentStore();
