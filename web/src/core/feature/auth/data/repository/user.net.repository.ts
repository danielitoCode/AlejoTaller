import {infrastructureContainer} from "../../../../infrastructure/di/infrastructure.container";
import type {UserDTO} from "../dto/UserDTO";
import type {UserNetRepository} from "../../domain/repository/user.net.repository";
import {account} from "../../../../infrastructure/di/appwrite.config";
import {type Account, ID} from "appwrite";

export class UserNetRepositoryImpl implements UserNetRepository {
    constructor(private readonly account: Account) {}

    async getCurrentUser(): Promise<Partial<UserDTO>> {
        const current = await this.account.get();
        const labels = (current as any)?.labels;

        const roleFromLabels =
            Array.isArray(labels) && labels.length
                ? labels.includes("admin")
                    ? "admin"
                    : (typeof labels[0] === "string" ? labels[0] : null)
                : null;

        return {
            id: current.$id,
            name: current.name,
            email: current.email,
            phone: current.phone ?? "",
            photo_url: typeof current.prefs?.photo_url === "string" ? current.prefs.photo_url : "",
            role: roleFromLabels ?? (typeof current.prefs?.role === "string" ? current.prefs.role : null),
            sub: typeof current.prefs?.sub === "string" ? current.prefs.sub : "",
            verification: current.emailVerification,
        };
    }

    async createAccount(user: Partial<UserDTO>) {
        await this.account.create(
            ID.unique(),
            user.email as string,
            user.password as string,
            user.name as string
        )

        let response = await this.account.createEmailPasswordSession(
            user.email as string,
            user.password as string,
        )

        let preferences = new Map<string, any>();
        preferences.set("photo_url", user.photo_url as string);
        preferences.set("sub", user.sub as string);
        preferences.set("name", user.name as string);
        preferences.set("role", user.role as string);
        preferences.set("phone", user.phone as string);

        await this.account.updatePrefs(preferences);
    }

    async updateName(newName: string): Promise<void> {
        await this.account.updateName(newName);
    }

    async updatePassword(newPassword: string, oldPassword?: string): Promise<void> {
        await this.account.updatePassword(newPassword, oldPassword);
    }

    async updatePhotoUrl(newPhotoUrl: string): Promise<void> {
        let photoPreference = new Map<string,string>()
        photoPreference.set("photo_url", newPhotoUrl);
        await this.account.updatePrefs(photoPreference)
    }

    async linkGoogle(sub: string, photoUrl: string, name: string): Promise<void> {
        const prefs = new Map<string, any>();
        prefs.set("sub", sub);
        prefs.set("photo_url", photoUrl);
        prefs.set("name", name);
        prefs.set("google_linked", true);
        await this.account.updatePrefs(prefs);
    }

    async updatePhone(newPhone: string): Promise<void> {
        let photoPreference = new Map<string,string>()
        photoPreference.set("phone", newPhone);
        await this.account.updatePrefs(photoPreference)
    }

    async updateRole(newRole: string): Promise<void> {
        let photoPreference = new Map<string,string>()
        photoPreference.set("role", newRole);
        await this.account.updatePrefs(photoPreference)
    }

    async deleteUser(user: Partial<UserDTO>) {
        await this.account.deleteIdentity(user.id as string)
    }
}
