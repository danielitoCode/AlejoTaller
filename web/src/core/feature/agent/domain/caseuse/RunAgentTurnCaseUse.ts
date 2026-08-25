import {
  agentTurnMessage,
  agentTurnNeedsConfirmation,
  agentTurnToolResult,
  type AgentTurnResult,
} from "../entity/AgentTurn";
import type { AgentMessage } from "../entity/AgentMessage";
import type { McpAuthContext } from "../entity/McpTypes";
import {
  isMcpWriteToolRequiringConfirmation,
} from "../policy/agent-tool-policy";
import type { AgentRepository } from "../repository/agent.repository";
import { CallMcpToolCaseUse } from "./CallMcpToolCaseUse";

export type RunAgentTurnHistory = ReadonlyArray<
  Pick<AgentMessage, "role" | "content">
>;

export interface RunAgentTurnInput {
  /** User chat text. Required when there is no pendingTool. */
  text?: string;
  history?: RunAgentTurnHistory;
  auth?: McpAuthContext | null;
  /** true when the user is not logged in */
  isGuest?: boolean;
  /**
   * Explicit MCP tool to run this turn (from UI after confirm, or orchestrator).
   * Write tools (create_order / cancel_order) need userConfirmed === true.
   */
  pendingTool?: {
    name: string;
    args?: Record<string, unknown>;
  };
  /** Explicit user confirmation for write tools */
  userConfirmed?: boolean;
  /** After a successful tool call, ask Mistral to summarize (default true). */
  summarizeToolResult?: boolean;
}

/**
 * Orchestrates one agent turn:
 * - chat-only → Mistral agents/completions
 * - tool path → policy (guest + write confirm) → MCP → optional summary
 *
 * Write tools never execute without userConfirmed; the UI must re-invoke
 * with the same pendingTool and userConfirmed: true.
 */
export class RunAgentTurnCaseUse {
  constructor(
    private readonly agent: AgentRepository,
    private readonly callMcpTool: CallMcpToolCaseUse
  ) {}

  async execute(input: RunAgentTurnInput): Promise<AgentTurnResult> {
    const pending = input.pendingTool;
    const toolName = pending?.name?.trim() ?? "";

    if (toolName) {
      return this.executeToolPath({
        name: toolName,
        args: pending?.args ?? {},
        auth: input.auth,
        isGuest: input.isGuest === true,
        userConfirmed: input.userConfirmed === true,
        history: input.history,
        summarize: input.summarizeToolResult !== false,
      });
    }

    const text = input.text?.trim() ?? "";
    if (!text) {
      throw new Error("El mensaje no puede estar vacío");
    }

    const reply = await this.agent.sendMessage(text, input.history);
    return agentTurnMessage(reply);
  }

  private async executeToolPath(opts: {
    name: string;
    args: Record<string, unknown>;
    auth?: McpAuthContext | null;
    isGuest: boolean;
    userConfirmed: boolean;
    history?: RunAgentTurnHistory;
    summarize: boolean;
  }): Promise<AgentTurnResult> {
    if (
      isMcpWriteToolRequiringConfirmation(opts.name) &&
      !opts.userConfirmed
    ) {
      return agentTurnNeedsConfirmation({
        name: opts.name,
        args: opts.args,
        reason: `La herramienta "${opts.name}" modifica datos. Confirma en la UI antes de continuar.`,
      });
    }

    // CallMcpToolCaseUse re-asserts guest + write policies (defense in depth).
    const toolResult = await this.callMcpTool.execute({
      name: opts.name,
      args: opts.args,
      auth: opts.auth,
      isGuest: opts.isGuest,
      userConfirmed: opts.userConfirmed,
    });

    let reply = null;
    if (opts.summarize) {
      const summaryPrompt = [
        `Se ejecutó la tool MCP "${opts.name}".`,
        toolResult.isError
          ? `Resultado con error:\n${toolResult.text}`
          : `Resultado:\n${toolResult.text}`,
        "Resume al usuario en español de forma breve y clara, sin inventar datos.",
      ].join("\n\n");

      try {
        reply = await this.agent.sendMessage(summaryPrompt, opts.history);
      } catch {
        // Tool already ran; summary is best-effort.
        reply = null;
      }
    }

    return agentTurnToolResult(toolResult, reply);
  }
}
