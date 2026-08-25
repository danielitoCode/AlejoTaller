import { describe, it, expect, vi, beforeEach } from "vitest";
import { SendAgentMessageCaseUse } from "../../../../../../core/feature/agent/domain/caseuse/SendAgentMessageCaseUse";
import type { AgentRepository } from "../../../../../../core/feature/agent/domain/repository/agent.repository";

describe("SendAgentMessageCaseUse", () => {
  const sendMessage = vi.fn();
  const repo: AgentRepository = {
    checkConnection: vi.fn(),
    sendMessage,
  };
  const useCase = new SendAgentMessageCaseUse(repo);

  beforeEach(() => {
    vi.clearAllMocks();
    sendMessage.mockResolvedValue({
      providerId: "p1",
      message: {
        id: "m1",
        role: "assistant",
        content: "Hola",
        createdAt: new Date().toISOString(),
      },
    });
  });

  it("rejects empty text", async () => {
    await expect(useCase.execute({ text: "  " })).rejects.toThrow(/vacío/i);
    expect(sendMessage).not.toHaveBeenCalled();
  });

  it("delegates to repository", async () => {
    const reply = await useCase.execute({ text: "¿Qué productos hay?" });
    expect(sendMessage).toHaveBeenCalledWith("¿Qué productos hay?", undefined);
    expect(reply.message.content).toBe("Hola");
  });
});
