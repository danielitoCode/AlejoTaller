import type { McpAuthContext, McpToolDescriptor } from "../entity/McpTypes";
import type { McpGateway } from "../repository/mcp.gateway";

export class ListMcpToolsCaseUse {
  constructor(private readonly mcp: McpGateway) {}

  execute(auth?: McpAuthContext | null): Promise<ReadonlyArray<McpToolDescriptor>> {
    return this.mcp.listTools(auth);
  }
}
