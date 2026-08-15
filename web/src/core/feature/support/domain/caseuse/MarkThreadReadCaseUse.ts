import type { SupportSenderRole } from "../entity/SupportMessage";
import type { SupportRepository } from "../repository/support.repository";

/** Marca el hilo como leído para staff o usuario. */
export class MarkThreadReadCaseUse {
    constructor(private readonly repo: SupportRepository) {}

    async execute(threadId: string, forRole: SupportSenderRole): Promise<void> {
        if (forRole === "staff") {
            await this.repo.touchThread(threadId, { unreadStaff: 0 });
        } else {
            await this.repo.touchThread(threadId, { unreadUser: 0 });
        }
    }
}
