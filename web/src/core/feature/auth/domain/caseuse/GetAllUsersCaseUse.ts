import type {AdminNetRepository} from "../repository/admin.net.repository";
import type {User} from "../entity/User";

export class GetAllUsersCaseUse {
    constructor(private readonly adminRepo: AdminNetRepository) {}

    async execute(search?: string): Promise<{ total: number; users: User[] }> {
        return await this.adminRepo.listUsers({ search })
    }
}
