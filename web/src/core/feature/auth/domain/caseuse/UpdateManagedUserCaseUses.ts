import type { AdminNetRepository } from "../repository/admin.net.repository";
import type { User } from "../entity/User";

export class UpdateManagedUserLabelsCaseUse {
    constructor(private readonly adminRepo: AdminNetRepository) {}
    async execute(userId: string, labels: string[]): Promise<User> {
        return await this.adminRepo.updateUser({ userId, patch: { labels } });
    }
}

export class UpdateManagedUserStatusCaseUse {
    constructor(private readonly adminRepo: AdminNetRepository) {}
    async execute(userId: string, status: boolean): Promise<User> {
        return await this.adminRepo.updateUser({ userId, patch: { status } });
    }
}

export class UpdateManagedUserPasswordCaseUse {
    constructor(private readonly adminRepo: AdminNetRepository) {}
    async execute(userId: string, password: string): Promise<User> {
        return await this.adminRepo.updateUser({ userId, patch: { password } });
    }
}

