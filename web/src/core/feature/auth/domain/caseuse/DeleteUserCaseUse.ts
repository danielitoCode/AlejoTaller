import type {UserDTO} from "../../data/dto/UserDTO";
import type {UserNetRepository} from "../repository/user.net.repository";

export class DeleteUserCaseUse {
    constructor(private readonly userRepository: UserNetRepository) {}

    async execute(user: Partial<UserDTO>): Promise<void> {
        await this.userRepository.deleteUser(user)
    }
}