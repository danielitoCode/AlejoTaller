import type {Models} from "appwrite";

export interface ProductDTO extends Models.Document {
    id: string
    name: string
    description: string
    price: number
    photo_url: string
    category_id: string
    rating?: number
}