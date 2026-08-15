import { ID, Query, type Databases, type Models } from "appwrite";
import { client } from "../../../../infrastructure/di/appwrite.config";
import { ENV } from "../../../../infrastructure/env";
import type { SupportChatMessageDTO, SupportThreadDTO } from "../dto/SupportMessageDTO";
import {
    supportChatMessageFromDTO,
    supportThreadFromDTO,
    threadDtoToInboxRow,
    type SupportChatMessageWritePayload,
    type SupportThreadWritePayload
} from "../mapper/Mappers";
import type {
    SupportChatMessage,
    SupportMessage,
    SupportStatus,
    SupportThread
} from "../../domain/entity/SupportMessage";
import type {
    SupportRealtimeEvent,
    SupportRealtimeUnsubscribe,
    SupportRepository
} from "../../domain/repository/support.repository";

const THREADS_COLLECTION = "support_threads";
const MESSAGES_COLLECTION = "support_messages";
const LOG = "[support:appwrite:client]";

function databaseId(): string {
    const id = ENV.databaseId;
    if (!id) throw new Error("Falta configurar VITE_APPWRITE_DATABASE_ID");
    return id;
}

export class SupportAppwriteRepository implements SupportRepository {
    constructor(private readonly databases: Databases) {}

    async listMyThreads(userId: string): Promise<SupportMessage[]> {
        const res = await this.databases.listDocuments<SupportThreadDTO & Models.Document>(
            databaseId(),
            THREADS_COLLECTION,
            [
                Query.equal("userId", userId),
                Query.orderDesc("lastMessageAt"),
                Query.limit(100)
            ]
        );
        return res.documents
            .map((d) => threadDtoToInboxRow(d))
            .sort((a, b) => b.createdAtIso.localeCompare(a.createdAtIso));
    }

    async listMessages(threadId: string): Promise<SupportChatMessage[]> {
        const res = await this.databases.listDocuments<SupportChatMessageDTO & Models.Document>(
            databaseId(),
            MESSAGES_COLLECTION,
            [
                Query.equal("threadId", threadId),
                Query.orderAsc("createdAtIso"),
                Query.limit(200)
            ]
        );
        return res.documents.map((d) => supportChatMessageFromDTO(d));
    }

    async createThread(payload: SupportThreadWritePayload, documentId?: string): Promise<SupportThread> {
        const doc = await this.databases.createDocument<SupportThreadDTO & Models.Document>(
            databaseId(),
            THREADS_COLLECTION,
            documentId?.trim() || ID.unique(),
            payload as unknown as SupportThreadDTO
        );
        return supportThreadFromDTO(doc);
    }

    async postMessage(
        payload: SupportChatMessageWritePayload,
        documentId?: string
    ): Promise<SupportChatMessage> {
        const doc = await this.databases.createDocument<SupportChatMessageDTO & Models.Document>(
            databaseId(),
            MESSAGES_COLLECTION,
            documentId?.trim() || ID.unique(),
            payload as unknown as SupportChatMessageDTO
        );
        return supportChatMessageFromDTO(doc);
    }

    async touchThread(
        id: string,
        patch: Partial<{
            status: SupportStatus;
            lastMessageAt: string;
            lastPreview: string;
            lastSenderRole: "user" | "staff";
            unreadStaff: number;
            unreadUser: number;
        }>
    ): Promise<void> {
        const threadId = id?.trim();
        if (!threadId) {
            throw new Error("threadId requerido para actualizar el hilo");
        }
        try {
            await this.databases.updateDocument(databaseId(), THREADS_COLLECTION, threadId, patch);
        } catch (e) {
            console.warn(`${LOG} touchThread failed id=${threadId}`, e);
            throw e;
        }
    }

    subscribe(handler: (event: SupportRealtimeEvent) => void): SupportRealtimeUnsubscribe {
        if (!ENV.appwriteEndpoint || !ENV.appwriteProjectId || !ENV.databaseId) {
            console.warn(`${LOG} RT omitido`);
            return () => {};
        }
        const db = databaseId();
        const channels = [
            `databases.${db}.collections.${THREADS_COLLECTION}.documents`,
            `databases.${db}.collections.${MESSAGES_COLLECTION}.documents`
        ];
        let unsub: (() => void) | null = null;
        try {
            unsub = (client as unknown as {
                subscribe: (
                    channels: string | string[],
                    cb: (res: { events: string[]; payload?: unknown }) => void
                ) => () => void;
            }).subscribe(channels, (res) => {
                const events = Array.isArray(res?.events) ? res.events : [];
                const joined = events.join(" ");
                let target: SupportRealtimeEvent["target"] = "unknown";
                if (joined.includes(THREADS_COLLECTION)) target = "threads";
                else if (joined.includes(MESSAGES_COLLECTION)) target = "messages";
                handler({ events, target });
            });
        } catch (e) {
            console.warn(`${LOG} RT failed`, e);
            return () => {};
        }
        return () => {
            try {
                unsub?.();
            } catch {
                /* ignore */
            }
        };
    }
}
