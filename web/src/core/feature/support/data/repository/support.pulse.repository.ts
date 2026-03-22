import { ENV } from "../../../../infrastructure/env";
import { pulseFetchJson } from "../../../../infrastructure/data/alset-pulse/pulse.http";
import { subscribePulseRefresh } from "../../../../infrastructure/data/alset-pulse/pulse.realtime";
import type { SupportMessageDTO } from "../dto/SupportMessageDTO";
import { supportMessageFromDTO } from "../mapper/Mappers";
import type { SupportMessage, SupportStatus } from "../../domain/entity/SupportMessage";
import type { SupportRealtimeUnsubscribe, SupportRepository } from "../../domain/repository/support.repository";

type SupportListResponse = {
    items?: SupportMessageDTO[];
} | SupportMessageDTO[];

function getMessagesPath(): string {
    return ENV.pulseSupportMessagesPath || "/support/messages";
}

function normalizeListResponse(res: SupportListResponse): SupportMessageDTO[] {
    if (Array.isArray(res)) return res;
    return Array.isArray(res.items) ? res.items : [];
}

export class SupportPulseRepository implements SupportRepository {
    async getAll(): Promise<SupportMessage[]> {
        const res = await pulseFetchJson<SupportListResponse>({ path: getMessagesPath() });
        return normalizeListResponse(res).map(supportMessageFromDTO);
    }

    async updateStatus(id: string, status: SupportStatus): Promise<void> {
        await pulseFetchJson({
            method: "PATCH",
            path: `${getMessagesPath()}/${encodeURIComponent(id)}`,
            body: { status }
        });
    }

    subscribe(handler: (event: { events: string[] }) => void): SupportRealtimeUnsubscribe {
        return subscribePulseRefresh((eventName) => handler({ events: [eventName] }));
    }
}
