import type { McpHealthResult } from "../entity/McpTypes";
import type { McpGateway } from "../repository/mcp.gateway";

export class CheckMcpHealthCaseUse {
  constructor(private readonly mcp: McpGateway) {}

  execute(): Promise<McpHealthResult> {
    return this.mcp.checkHealth();
  }
}
