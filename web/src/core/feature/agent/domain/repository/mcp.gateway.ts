import type {
  McpAuthContext,
  McpHealthResult,
  McpToolCallResult,
  McpToolDescriptor,
} from "../entity/McpTypes";

/**
 * Port — remote MCP (Streamable HTTP) for AlejoTaller customer tools.
 * Identity is injected by the host; never taken from the LLM.
 */
export interface McpGateway {
  checkHealth(): Promise<McpHealthResult>;

  listTools(auth?: McpAuthContext | null): Promise<ReadonlyArray<McpToolDescriptor>>;

  callTool(
    name: string,
    args: Record<string, unknown>,
    auth?: McpAuthContext | null
  ): Promise<McpToolCallResult>;
}
