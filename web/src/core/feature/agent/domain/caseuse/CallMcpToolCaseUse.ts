import {
  assertMcpToolAllowed,
  assertMcpWriteConfirmed,
} from "../policy/agent-tool-policy";
import type { McpAuthContext, McpToolCallResult } from "../entity/McpTypes";
import type { McpGateway } from "../repository/mcp.gateway";

export class CallMcpToolCaseUse {
  constructor(private readonly mcp: McpGateway) {}

  async execute(input: {
    name: string;
    args?: Record<string, unknown>;
    auth?: McpAuthContext | null;
    /** true when user is not logged in */
    isGuest?: boolean;
    /** required for create_order / cancel_order */
    userConfirmed?: boolean;
  }): Promise<McpToolCallResult> {
    const name = input.name?.trim() ?? "";
    if (!name) {
      throw new Error("Nombre de tool MCP vacío");
    }
    assertMcpToolAllowed(name, input.isGuest === true);
    assertMcpWriteConfirmed(name, input.userConfirmed === true);
    return this.mcp.callTool(name, input.args ?? {}, input.auth);
  }
}
