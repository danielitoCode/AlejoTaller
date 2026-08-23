import { type Databases, ID, Query } from "node-appwrite";
import type { ISupportRepository } from "../../../repositories/support.repository.js";
import type {
  SupportThread,
  SupportChatMessage,
  CreateSupportThreadInput,
  PostSupportMessageInput,
  SupportReason,
  SupportStatus,
  SupportSenderRole,
} from "../../../domain/support.js";
import { COLLECTIONS } from "../config.js";

/**
 * Appwrite implementation of ISupportRepository.
 */
export class AppwriteSupportRepository implements ISupportRepository {
  constructor(
    private readonly databases: Databases,
    private readonly databaseId: string
  ) {}

  async listThreadsByUser(userId: string): Promise<SupportThread[]> {
    const res = await this.databases.listDocuments(
      this.databaseId,
      COLLECTIONS.supportThreads,
      [
        Query.equal("userId", userId),
        Query.orderDesc("lastMessageAt"),
        Query.limit(100),
      ]
    );
    return res.documents.map((d) => this.toThread(d as unknown as AppwriteThreadDoc));
  }

  async getThreadById(threadId: string): Promise<SupportThread | null> {
    try {
      const doc = await this.databases.getDocument(
        this.databaseId,
        COLLECTIONS.supportThreads,
        threadId
      );
      return this.toThread(doc as unknown as AppwriteThreadDoc);
    } catch {
      return null;
    }
  }

  async listMessagesByThread(threadId: string): Promise<SupportChatMessage[]> {
    const res = await this.databases.listDocuments(
      this.databaseId,
      COLLECTIONS.supportMessages,
      [
        Query.equal("threadId", threadId),
        Query.orderAsc("createdAtIso"),
        Query.limit(200),
      ]
    );
    return res.documents.map((d) => this.toMessage(d as unknown as AppwriteMessageDoc));
  }

  async createThread(
    userId: string,
    userName: string,
    userEmail: string,
    input: CreateSupportThreadInput
  ): Promise<SupportThread> {
    const nowIso = new Date().toISOString();
    const threadId = ID.unique();

    const threadPayload: Record<string, unknown> = {
      userId,
      userName,
      userEmail,
      reason: input.reason,
      subject: input.subject,
      status: "nuevo",
      lastMessageAt: nowIso,
      lastPreview: input.body.substring(0, 100),
      lastSenderRole: "user",
      unreadStaff: 1,
      unreadUser: 0,
      createdAtIso: nowIso,
    };

    const threadDoc = await this.databases.createDocument(
      this.databaseId,
      COLLECTIONS.supportThreads,
      threadId,
      threadPayload
    );

    const messagePayload: Record<string, unknown> = {
      threadId,
      senderRole: "user",
      senderId: userId,
      senderName: userName,
      body: input.body,
      createdAtIso: nowIso,
    };

    await this.databases.createDocument(
      this.databaseId,
      COLLECTIONS.supportMessages,
      ID.unique(),
      messagePayload
    );

    return this.toThread(threadDoc as unknown as AppwriteThreadDoc);
  }

  async postMessage(
    userId: string,
    userName: string,
    input: PostSupportMessageInput
  ): Promise<SupportChatMessage> {
    const nowIso = new Date().toISOString();

    const messagePayload: Record<string, unknown> = {
      threadId: input.threadId,
      senderRole: "user",
      senderId: userId,
      senderName: userName,
      body: input.body,
      createdAtIso: nowIso,
    };

    const messageDoc = await this.databases.createDocument(
      this.databaseId,
      COLLECTIONS.supportMessages,
      ID.unique(),
      messagePayload
    );

    await this.databases.updateDocument(
      this.databaseId,
      COLLECTIONS.supportThreads,
      input.threadId,
      {
        lastMessageAt: nowIso,
        lastPreview: input.body.substring(0, 100),
        lastSenderRole: "user",
        status: "en_proceso",
      }
    );

    return this.toMessage(messageDoc as unknown as AppwriteMessageDoc);
  }

  async markThreadReadByUser(threadId: string): Promise<void> {
    try {
      await this.databases.updateDocument(
        this.databaseId,
        COLLECTIONS.supportThreads,
        threadId,
        { unreadUser: 0 }
      );
    } catch {
      // Ignore if thread non-existent or error
    }
  }

  private toThread(doc: AppwriteThreadDoc): SupportThread {
    return {
      id: doc.$id,
      userId: doc.userId ?? "",
      userName: doc.userName ?? "",
      userEmail: doc.userEmail ?? "",
      reason: (doc.reason as SupportReason) ?? "otro",
      subject: doc.subject ?? "",
      status: (doc.status as SupportStatus) ?? "nuevo",
      lastMessageAt: doc.lastMessageAt ?? doc.createdAtIso ?? "",
      lastPreview: doc.lastPreview ?? "",
      lastSenderRole: (doc.lastSenderRole as SupportSenderRole) ?? "user",
      unreadUser: Number(doc.unreadUser ?? 0),
      createdAt: doc.createdAtIso ?? doc.$createdAt ?? "",
    };
  }

  private toMessage(doc: AppwriteMessageDoc): SupportChatMessage {
    return {
      id: doc.$id,
      threadId: doc.threadId ?? "",
      senderRole: (doc.senderRole as SupportSenderRole) ?? "user",
      senderId: doc.senderId ?? "",
      senderName: doc.senderName ?? "",
      body: doc.body ?? "",
      createdAt: doc.createdAtIso ?? doc.$createdAt ?? "",
    };
  }
}

interface AppwriteThreadDoc {
  $id: string;
  $createdAt?: string;
  userId?: string;
  userName?: string;
  userEmail?: string;
  reason?: string;
  subject?: string;
  status?: string;
  lastMessageAt?: string;
  lastPreview?: string;
  lastSenderRole?: string;
  unreadUser?: number;
  createdAtIso?: string;
}

interface AppwriteMessageDoc {
  $id: string;
  $createdAt?: string;
  threadId?: string;
  senderRole?: string;
  senderId?: string;
  senderName?: string;
  body?: string;
  createdAtIso?: string;
}
