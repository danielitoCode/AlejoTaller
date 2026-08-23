import { describe, it, expect, vi } from "vitest";
import { SupportService } from "../../src/services/support.service.js";
import type { ISupportRepository } from "../../src/repositories/support.repository.js";
import type { SupportThread } from "../../src/domain/support.js";
import type { McpAuthContext } from "../../src/auth/context.js";
import { AuthorizationError } from "../../src/auth/context.js";

describe("SupportService Authorization", () => {
  const mockThreadUserA: SupportThread = {
    id: "thread-1",
    userId: "user-A",
    userName: "User A",
    userEmail: "a@test.com",
    reason: "soporte",
    subject: "Problema con pedido",
    status: "nuevo",
    lastMessageAt: new Date().toISOString(),
    lastPreview: "Hola tengo una duda",
    lastSenderRole: "user",
    unreadUser: 0,
    createdAt: new Date().toISOString(),
  };

  const mockSupportRepo: ISupportRepository = {
    listThreadsByUser: vi.fn().mockImplementation(async (userId) => {
      return userId === "user-A" ? [mockThreadUserA] : [];
    }),
    getThreadById: vi.fn().mockImplementation(async (threadId) => {
      return threadId === "thread-1" ? mockThreadUserA : null;
    }),
    listMessagesByThread: vi.fn().mockResolvedValue([]),
    createThread: vi.fn(),
    postMessage: vi.fn(),
    markThreadReadByUser: vi.fn().mockResolvedValue(undefined),
  };

  const service = new SupportService(mockSupportRepo);

  it("User A can view their own support thread", async () => {
    const authUserA: McpAuthContext = {
      userId: "user-A",
      userName: "User A",
      userEmail: "a@test.com",
    };

    const thread = await service.getThread(authUserA, "thread-1");
    expect(thread.id).toBe("thread-1");
  });

  it("User B CANNOT view User A's support thread", async () => {
    const authUserB: McpAuthContext = {
      userId: "user-B",
      userName: "User B",
      userEmail: "b@test.com",
    };

    await expect(service.getThread(authUserB, "thread-1")).rejects.toThrow(
      AuthorizationError
    );
  });
});
