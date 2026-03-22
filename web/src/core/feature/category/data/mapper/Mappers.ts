import type {CategoryDTO} from "../dto/CategoryDTO";
import type {Category} from "../../domain/entity/Category";

export type CategoryWriteDTO = Pick<CategoryDTO, "$id" | "name" | "photo_url" | "description" | "status">;

export function categoryFromDTO(dto: CategoryDTO): Category {
    return {
        id: dto.$id,
        description: dto.description,
        name: dto.name,
        photoUrl: dto.photo_url ?? null,
        status: dto.status === "inactive" ? "inactive" : "active"
    };
}

/**
 * Domain → DTO (create/update payload)
 * El id de dominio se serializa en $id de Appwrite.
 */
export function categoryToDTO(category: Category): CategoryWriteDTO {
    return {
        $id: category.id,
        name: category.name,
        description: category.description,
        photo_url: category.photoUrl ?? null,
        status: category.status
    };
}
