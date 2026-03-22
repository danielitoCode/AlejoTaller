import type { SupportStatus } from "../entity/SupportMessage";
import type { SupportRepository } from "../repository/support.repository";

export class UpdateSupportStatusCaseUse {
    constructor(private readonly repo: SupportRepository) {}

    async execute(id: string, status: SupportStatus): Promise<void> {
        await this.repo.updateStatus(id, status);
    }
}

