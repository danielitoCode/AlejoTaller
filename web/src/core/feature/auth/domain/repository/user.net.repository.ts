import {infrastructureContainer} from "../../../../infrastructure/di/infrastructure.container";
import type {UserDTO} from "../../data/dto/UserDTO";
import {ID} from "appwrite";

export interface UserNetRepository {
    getCurrentUser(): Promise<Partial<UserDTO>>

    createAccount(user: Partial<UserDTO>): Promise<void>

    updateName(newName: string): Promise<void>

    updatePassword(newPassword: string, oldPassword?: string): Promise<void>

    updatePhotoUrl(newPhotoUrl: string): Promise<void>

    linkGoogle(sub: string, photoUrl: string, name: string): Promise<void>

    updatePhone(newPhone: string): Promise<void>

    updateRole(newRole: string): Promise<void>

    deleteUser(user: Partial<UserDTO>): Promise<void>
}
