import type { AdminNetRepository } from "../repository/admin.net.repository";
import type { User } from "../entity/User";

export class CreateManagedUserCaseUse {
    constructor(private readonly adminRepo: AdminNetRepository) {}

    async execute(params: { name: string; email: string; password: string; labels?: string[] }): Promise<User> {
        return await this.adminRepo.createUser({
            name: params.name,
            email: params.email,
            password: params.password,
            labels: params.labels
        });
    }
}

