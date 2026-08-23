import type { ISupportRepository } from "../repositories/support.repository.js";
import type {
  SupportThread,
  SupportChatMessage,
  CreateSupportThreadInput,
  PostSupportMessageInput,
} from "../domain/support.js";
import type { McpAuthContext } from "../auth/context.js";
import { AuthenticationError, AuthorizationError } from "../auth/context.js";

/**
 * Service — Support
 * Business logic for customer support tickets/threads and messages.
 */
export class SupportService {
  constructor(private readonly supportRepo: ISupportRepository) {}

  /**
   * List support threads for authenticated user.
   */
  async getMyThreads(auth: McpAuthContext): Promise<SupportThread[]> {
    if (!auth.userId) {
      throw new AuthenticationError("User is not authenticated");
    }
    return await this.supportRepo.listThreadsByUser(auth.userId);
  }

  /**
   * Get single support thread by ID, enforcing user ownership.
   */
  async getThread(auth: McpAuthContext, threadId: string): Promise<SupportThread> {
    if (!auth.userId) {
      throw new AuthenticationError("User is not authenticated");
    }
    const thread = await this.supportRepo.getThreadById(threadId);
    if (!thread) {
      throw new Error(`Support thread not found: ${threadId}`);
    }
    if (thread.userId !== auth.userId) {
      throw new AuthorizationError("You are not authorized to view this support thread.");
    }
    return thread;
  }

  /**
   * List messages in a thread after verifying ownership.
   */
  async getThreadMessages(
    auth: McpAuthContext,
    threadId: string
  ): Promise<SupportChatMessage[]> {
    // Verify thread ownership first
    await this.getThread(auth, threadId);
    // Mark as read when customer views messages
    await this.supportRepo.markThreadReadByUser(threadId);
    return await this.supportRepo.listMessagesByThread(threadId);
  }

  /**
   * Create a new support ticket / thread.
   */
  async createThread(
    auth: McpAuthContext,
    input: CreateSupportThreadInput
  ): Promise<SupportThread> {
    if (!auth.userId) {
      throw new AuthenticationError("User is not authenticated");
    }
    if (!input.subject || !input.body) {
      throw new Error("Subject and body are required to create a support thread.");
    }
    return await this.supportRepo.createThread(
      auth.userId,
      auth.userName,
      auth.userEmail,
      input
    );
  }

  /**
   * Post a message in an existing support thread.
   */
  async postMessage(
    auth: McpAuthContext,
    input: PostSupportMessageInput
  ): Promise<SupportChatMessage> {
    if (!auth.userId) {
      throw new AuthenticationError("User is not authenticated");
    }
    // Verify ownership first
    await this.getThread(auth, input.threadId);

    if (!input.body || input.body.trim() === "") {
      throw new Error("Message body cannot be empty.");
    }

    return await this.supportRepo.postMessage(auth.userId, auth.userName, input);
  }
}
