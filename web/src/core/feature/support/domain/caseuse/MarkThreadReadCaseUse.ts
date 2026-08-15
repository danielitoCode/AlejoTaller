import type { SupportSenderRole } from "../entity/SupportMessage";
import type { SupportRepository } from "../repository/support.repository";

/** Marca el hilo como leído para staff o usuario. */
export class MarkThreadReadCaseUse {
    constructor(private readonly repo: SupportRepository) {}

    async execute(threadId: string, forRole: SupportSenderRole): Promise<void> {
        const id = threadId?.trim();
        if (!id) {
            throw new Error("threadId requerido para marcar como leído");
        }
        if (forRole === "staff") {
            await this.repo.touchThread(id, { unreadStaff: 0 });
        } else {
            await this.repo.touchThread(id, { unreadUser: 0 });
        }
    }
}
