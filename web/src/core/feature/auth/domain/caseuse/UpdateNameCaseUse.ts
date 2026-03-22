import type {UserNetRepository} from "../repository/user.net.repository";

export class UpdateNameCaseUse {
    constructor(private readonly userRepository: UserNetRepository) {}

    async execute(newName: string): Promise<void> {
        await this.userRepository.updateName(newName)
    }
}