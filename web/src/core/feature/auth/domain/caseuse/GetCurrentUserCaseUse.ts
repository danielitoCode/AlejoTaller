import type {UserNetRepository} from "../repository/user.net.repository";
import type {UserDTO} from "../../data/dto/UserDTO";

export class GetCurrentUserCaseUse {
    constructor(private readonly userRepository: UserNetRepository) {}

    async execute(): Promise<Partial<UserDTO>> {
        return await this.userRepository.getCurrentUser()
    }
}