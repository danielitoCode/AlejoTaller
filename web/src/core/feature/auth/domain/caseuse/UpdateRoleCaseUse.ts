import type {UserNetRepository} from "../repository/user.net.repository";

export class UpdateRoleCaseUse {
    constructor(private readonly userRepository: UserNetRepository) {}

    async execute(newRole: string): Promise<void> {
        await this.userRepository.updateRole(newRole)
    }
}