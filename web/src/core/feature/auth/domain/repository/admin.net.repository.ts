import type {User} from "../entity/User";

export interface AdminNetRepository {
    listUsers(params?: { search?: string }): Promise<{ total: number; users: User[] }>;
    createUser(params: { name: string; email?: string; phone?: string; password: string; labels?: string[] }): Promise<User>;
    updateUser(params: { userId: string; patch: Record<string, unknown> }): Promise<User>;
    deleteUser(params: { userId: string }): Promise<void>;
}
