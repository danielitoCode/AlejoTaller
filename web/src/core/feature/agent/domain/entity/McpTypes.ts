/** Domain — MCP client types (Fase 2). */

export type McpHealthStatus = "ok" | "degraded" | "error" | "unreachable";

export interface McpHealthResult {
  status: McpHealthStatus;
  worker: string | null;
  scope: string | null;
  appwriteConfigured: boolean | null;
  message: string;
  raw?: Record<string, unknown>;
}

export interface McpToolDescriptor {
  name: string;
  description: string;
  /** JSON Schema object from MCP (may be empty). */
  inputSchema: Record<string, unknown>;
}

export interface McpAuthContext {
  /** Appwrite user JWT — preferred in production. */
  jwt?: string | null;
  /** Fallback dev header; never pass as LLM tool argument. */
  customerId?: string | null;
  customerName?: string | null;
  customerEmail?: string | null;
}

export interface McpToolContentPart {
  type: string;
  text?: string;
}

export interface McpToolCallResult {
  toolName: string;
  isError: boolean;
  /** Flattened text from content parts. */
  text: string;
  content: McpToolContentPart[];
}
