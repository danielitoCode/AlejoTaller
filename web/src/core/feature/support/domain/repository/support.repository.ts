import type {
    SupportChatMessage,
    SupportMessage,
    SupportStatus,
    SupportThread
} from "../entity/SupportMessage";
import type { SupportChatMessageWritePayload, SupportThreadWritePayload } from "../../data/mapper/Mappers";

export type SupportRealtimeEvent = {
    events: string[];
    target?: "threads" | "messages" | "unknown";
};

export type SupportRealtimeUnsubscribe = () => void;

export interface SupportRepository {
    /** Hilos del usuario actual (filtrados por userId). */
    listMyThreads(userId: string): Promise<SupportMessage[]>;
    listMessages(threadId: string): Promise<SupportChatMessage[]>;
    createThread(payload: SupportThreadWritePayload, documentId?: string): Promise<SupportThread>;
    postMessage(payload: SupportChatMessageWritePayload, documentId?: string): Promise<SupportChatMessage>;
    touchThread(
        id: string,
        patch: Partial<{
            status: SupportStatus;
            lastMessageAt: string;
            lastPreview: string;
            lastSenderRole: "user" | "staff";
            unreadStaff: number;
            unreadUser: number;
        }>
    ): Promise<void>;
    subscribe(handler: (event: SupportRealtimeEvent) => void): SupportRealtimeUnsubscribe;
}
