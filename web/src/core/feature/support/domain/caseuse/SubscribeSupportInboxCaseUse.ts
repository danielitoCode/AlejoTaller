import type {
    SupportRealtimeEvent,
    SupportRealtimeUnsubscribe,
    SupportRepository
} from "../repository/support.repository";

export class SubscribeSupportInboxCaseUse {
    constructor(private readonly repo: SupportRepository) {}

    execute(handler: (event: SupportRealtimeEvent) => void): SupportRealtimeUnsubscribe {
        return this.repo.subscribe(handler);
    }
}

