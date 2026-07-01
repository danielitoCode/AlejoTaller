import type {Models} from "appwrite";

export interface ProductDTO extends Models.Document {
    id: string
    name: string
    description: string
    existence: number
    price: number
    photo_url: string
    category_id: string
    rating?: number
}