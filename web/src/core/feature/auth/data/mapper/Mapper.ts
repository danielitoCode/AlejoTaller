import type {ProductDTO} from "../../../product/data/dto/ProductDTO";
import type {UserDTO} from "../dto/UserDTO";
import type {User} from "../../domain/entity/User";
import {Role} from "../../domain/entity/Role";

type UserWriteDTO = Pick<
    UserDTO,
    "$id" | "name" | "email" | "phone" | "password" | "photo_url" | "sub" | "verification" | "role"
>;

/**
 * DTO → Domain (create/update entity)
 * Se recupera el $id del proporcionado por AppWrite.
 */
export function userFromDTO(dto: UserDTO): User {
    return {
        id: dto.id,
        name: dto.name,
        sub: dto.sub,
        email: dto.email,
        password: dto.password,
        phone: dto.phone,
        photo_url: dto.photo_url,
        verification: dto.verification,
        role: dto.role ?? Role.CUSTOM.toString()
    }
}

/**
 * Domain → DTO (create/update payload)
 * El id de dominio se serializa en $id de Appwrite.
 */
export function userToDTO(user: User): UserWriteDTO {
    return {
        $id: user.id,
        name: user.name,
        email: user.email,
        password: user.password,
        sub: user.sub,
        phone: user.phone,
        photo_url: user.photo_url,
        verification: user.verification,
        role: user.role ?? Role.CUSTOM.toString()
    };
}