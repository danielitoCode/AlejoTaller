import type { SupportMessage } from "../entity/SupportMessage";
import type { SupportRepository } from "../repository/support.repository";

export class ListMySupportThreadsCaseUse {
    constructor(private readonly repo: SupportRepository) {}

    async execute(userId: string): Promise<SupportMessage[]> {
        if (!userId) return [];
        return this.repo.listMyThreads(userId);
    }
}
