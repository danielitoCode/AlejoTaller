import { describe, expect, it, vi } from "vitest";
import { CallMcpToolCaseUse } from "../../../../core/feature/agent/domain/caseuse/CallMcpToolCaseUse";
import { RunAgentTurnCaseUse } from "../../../../core/feature/agent/domain/caseuse/RunAgentTurnCaseUse";
import type { AgentReply } from "../../../../core/feature/agent/domain/entity/AgentMessage";
import type { McpToolCallResult } from "../../../../core/feature/agent/domain/entity/McpTypes";
import type { AgentRepository } from "../../../../core/feature/agent/domain/repository/agent.repository";
import type { McpGateway } from "../../../../core/feature/agent/domain/repository/mcp.gateway";

function makeReply(content: string): AgentReply {
  return {
    providerId: "test",
    message: {
      id: "m1",
      role: "assistant",
      content,
      createdAt: new Date().toISOString(),
    },
  };
}

function makeToolResult(
  partial: Partial<McpToolCallResult> & { toolName: string }
): McpToolCallResult {
  return {
    isError: false,
    text: partial.text ?? "ok",
    content: partial.content ?? [{ type: "text", text: partial.text ?? "ok" }],
    ...partial,
  };
}

describe("RunAgentTurnCaseUse", () => {
  it("chat-only delega en agent.sendMessage", async () => {
    const reply = makeReply("hola");
    const agent: AgentRepository = {
      checkConnection: vi.fn(),
      sendMessage: vi.fn().mockResolvedValue(reply),
    };
    const mcp: McpGateway = {
      checkHealth: vi.fn(),
      listTools: vi.fn(),
      callTool: vi.fn(),
    };
    const uc = new RunAgentTurnCaseUse(agent, new CallMcpToolCaseUse(mcp));

    const out = await uc.execute({ text: "  hola  " });

    expect(out.kind).toBe("message");
    expect(out.reply?.message.content).toBe("hola");
    expect(mcp.callTool).not.toHaveBeenCalled();
    expect(agent.sendMessage).toHaveBeenCalledWith("hola", undefined);
  });

  it("create_order sin confirmación → needs_confirmation (no llama MCP)", async () => {
    const agent: AgentRepository = {
      checkConnection: vi.fn(),
      sendMessage: vi.fn(),
    };
    const mcp: McpGateway = {
      checkHealth: vi.fn(),
      listTools: vi.fn(),
      callTool: vi.fn(),
    };
    const uc = new RunAgentTurnCaseUse(agent, new CallMcpToolCaseUse(mcp));

    const out = await uc.execute({
      pendingTool: { name: "create_order", args: { items: [] } },
      isGuest: false,
      userConfirmed: false,
    });

    expect(out.kind).toBe("needs_confirmation");
    expect(out.pendingTool?.name).toBe("create_order");
    expect(mcp.callTool).not.toHaveBeenCalled();
    expect(agent.sendMessage).not.toHaveBeenCalled();
  });

  it("create_order con userConfirmed ejecuta MCP y resume", async () => {
    const summary = makeReply("Pedido creado");
    const agent: AgentRepository = {
      checkConnection: vi.fn(),
      sendMessage: vi.fn().mockResolvedValue(summary),
    };
    const toolResult = makeToolResult({
      toolName: "create_order",
      text: '{"orderId":"o1"}',
    });
    const mcp: McpGateway = {
      checkHealth: vi.fn(),
      listTools: vi.fn(),
      callTool: vi.fn().mockResolvedValue(toolResult),
    };
    const uc = new RunAgentTurnCaseUse(agent, new CallMcpToolCaseUse(mcp));

    const out = await uc.execute({
      pendingTool: { name: "create_order", args: { productId: "p1" } },
      isGuest: false,
      userConfirmed: true,
    });

    expect(out.kind).toBe("tool_result");
    expect(out.toolResult?.toolName).toBe("create_order");
    expect(out.reply?.message.content).toBe("Pedido creado");
    expect(mcp.callTool).toHaveBeenCalledWith(
      "create_order",
      { productId: "p1" },
      undefined
    );
  });

  it("guest no puede create_order aunque confirme", async () => {
    const agent: AgentRepository = {
      checkConnection: vi.fn(),
      sendMessage: vi.fn(),
    };
    const mcp: McpGateway = {
      checkHealth: vi.fn(),
      listTools: vi.fn(),
      callTool: vi.fn(),
    };
    const uc = new RunAgentTurnCaseUse(agent, new CallMcpToolCaseUse(mcp));

    await expect(
      uc.execute({
        pendingTool: { name: "create_order", args: {} },
        isGuest: true,
        userConfirmed: true,
      })
    ).rejects.toThrow(/invitados/);

    expect(mcp.callTool).not.toHaveBeenCalled();
  });

  it("tool de lectura (list_products) no requiere confirmación", async () => {
    const agent: AgentRepository = {
      checkConnection: vi.fn(),
      sendMessage: vi.fn().mockResolvedValue(makeReply("hay 3 productos")),
    };
    const toolResult = makeToolResult({
      toolName: "list_products",
      text: "[]",
    });
    const mcp: McpGateway = {
      checkHealth: vi.fn(),
      listTools: vi.fn(),
      callTool: vi.fn().mockResolvedValue(toolResult),
    };
    const uc = new RunAgentTurnCaseUse(agent, new CallMcpToolCaseUse(mcp));

    const out = await uc.execute({
      pendingTool: { name: "list_products", args: {} },
      isGuest: true,
      userConfirmed: false,
    });

    expect(out.kind).toBe("tool_result");
    expect(mcp.callTool).toHaveBeenCalled();
  });
});
