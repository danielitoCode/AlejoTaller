import type { SupportMessage, SupportStatus } from "../entity/SupportMessage";

export type SupportRealtimeEvent = {
    events: string[];
};

export type SupportRealtimeUnsubscribe = () => void;

export interface SupportRepository {
    getAll(): Promise<SupportMessage[]>;
    updateStatus(id: string, status: SupportStatus): Promise<void>;
    subscribe(handler: (event: SupportRealtimeEvent) => void): SupportRealtimeUnsubscribe;
}

