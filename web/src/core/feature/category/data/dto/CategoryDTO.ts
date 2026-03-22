import type {Models} from "appwrite";

export interface CategoryDTO extends Models.Document {
    description: string
    name: string
    photo_url?: string | null
    status: string
}
