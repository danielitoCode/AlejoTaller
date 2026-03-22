import type { SupportMessage } from "../entity/SupportMessage";
import type { SupportRepository } from "../repository/support.repository";

export class GetAllSupportMessagesCaseUse {
    constructor(private readonly repo: SupportRepository) {}

    async execute(): Promise<SupportMessage[]> {
        return await this.repo.getAll();
    }
}

