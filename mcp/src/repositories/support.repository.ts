import type {
  SupportThread,
  SupportChatMessage,
  CreateSupportThreadInput,
  PostSupportMessageInput,
} from "../domain/support.js";

/**
 * Repository interface — Support
 *
 * Maps to the `support_threads` and `support_messages` Appwrite collections.
 * Implementations: AppwriteSupportRepository, MockSupportRepository (tests)
 *
 * IMPORTANT: All methods that return user-owned data receive userId and
 * enforce ownership at the repository level (via Appwrite queries).
 * Never return data from other users.
 */
export interface ISupportRepository {
  /** List all support threads belonging to a user, newest first */
  listThreadsByUser(userId: string): Promise<SupportThread[]>;

  /**
   * Get a single support thread.
   * Returns null if not found.
   * NOTE: Ownership check is done at the service layer.
   */
  getThreadById(threadId: string): Promise<SupportThread | null>;

  /** List all messages in a thread, oldest first */
  listMessagesByThread(threadId: string): Promise<SupportChatMessage[]>;

  /**
   * Create a new support thread + its first message atomically.
   * The userId, userName and userEmail come from the auth context.
   */
  createThread(
    userId: string,
    userName: string,
    userEmail: string,
    input: CreateSupportThreadInput
  ): Promise<SupportThread>;

  /** Post a new message in an existing thread as the customer */
  postMessage(
    userId: string,
    userName: string,
    input: PostSupportMessageInput
  ): Promise<SupportChatMessage>;

  /**
   * Mark a thread as read by the customer (reset unreadUser counter).
   * Called after the customer views a thread.
   */
  markThreadReadByUser(threadId: string): Promise<void>;
}
