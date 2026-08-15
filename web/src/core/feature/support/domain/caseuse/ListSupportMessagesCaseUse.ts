import type { SupportChatMessage } from "../entity/SupportMessage";
import type { SupportRepository } from "../repository/support.repository";

export class ListSupportMessagesCaseUse {
    constructor(private readonly repo: SupportRepository) {}

    async execute(threadId: string): Promise<SupportChatMessage[]> {
        return this.repo.listMessages(threadId);
    }
}
