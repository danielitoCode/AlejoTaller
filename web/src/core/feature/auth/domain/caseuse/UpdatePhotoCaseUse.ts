import type {UserNetRepository} from "../repository/user.net.repository";
import type {User} from "../entity/User";

export class UpdatePhotoCaseUse {
    constructor(private readonly userRepository: UserNetRepository) {}
    async execute(newPhoto: string): Promise<void> {
        await this.userRepository.updatePhotoUrl(newPhoto)
    }
}