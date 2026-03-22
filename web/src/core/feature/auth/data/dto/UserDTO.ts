import type {Models} from "appwrite";

export interface UserDTO extends Models.Document{
    id: string
    name: string
    email: string
    phone: string
    password: string
    photo_url: string
    sub: string
    verification: boolean
    role: string | null
}