import type {UserNetRepository} from "../repository/user.net.repository";

export class UpdatePhoneCaseUse {
    constructor(private readonly userRepository: UserNetRepository) {}
    async execute(newPhone: string): Promise<void>  {
        await this.userRepository.updatePhone(newPhone)
    }
}