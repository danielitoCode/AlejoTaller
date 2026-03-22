export interface Category {
    id: string
    name: string
    description: string
    photoUrl?: string | null
    status: "active" | "inactive"
}
