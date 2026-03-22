import type {UserNetRepository} from "../repository/user.net.repository";

export class UpdatePasswordCaseUse {
    constructor(private readonly userRepository: UserNetRepository) {}

    async execute(newPassword: string): Promise<void> {
        await this.userRepository.updatePassword(newPassword)
    }
}