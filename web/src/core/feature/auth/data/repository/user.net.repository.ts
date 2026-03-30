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
            photo_url:
                typeof current.prefs?.photo_url === "string"
                    ? current.prefs.photo_url
                    : (typeof (current.prefs as any)?.photoUrl === "string"
                        ? (current.prefs as any).photoUrl
                        : (typeof (current.prefs as any)?.avatarUrl === "string" ? (current.prefs as any).avatarUrl : "")),
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

        await this.account.createEmailPasswordSession(
            user.email as string,
            user.password as string,
        )

        await this.account.updatePrefs({
            photo_url: user.photo_url as string,
            photoUrl: user.photo_url as string,
            sub: user.sub as string,
            name: user.name as string,
            role: user.role as string,
            phone: user.phone as string
        });
    }

    async updateName(newName: string): Promise<void> {
        await this.account.updateName(newName);
    }

    async updatePassword(newPassword: string, oldPassword?: string): Promise<void> {
        await this.account.updatePassword(newPassword, oldPassword);
    }

    async updatePhotoUrl(newPhotoUrl: string): Promise<void> {
        await this.account.updatePrefs({
            photo_url: newPhotoUrl,
            photoUrl: newPhotoUrl,
            avatarUrl: newPhotoUrl
        })
    }

    async linkGoogle(sub: string, photoUrl: string, name: string): Promise<void> {
        const current = await this.account.get();
        const currentPhoto =
            typeof current.prefs?.photo_url === "string"
                ? current.prefs.photo_url
                : (typeof (current.prefs as any)?.photoUrl === "string"
                    ? (current.prefs as any).photoUrl
                    : "");
        const prefs: Record<string, any> = {
            sub,
            name,
            google_linked: true
        };
        if (!currentPhoto.trim() && photoUrl.trim()) {
            prefs.photo_url = photoUrl;
            prefs.photoUrl = photoUrl;
            prefs.avatarUrl = photoUrl;
        }
        await this.account.updatePrefs(prefs);
    }

    async updatePhone(newPhone: string): Promise<void> {
        await this.account.updatePrefs({
            phone: newPhone
        })
    }

    async updateRole(newRole: string): Promise<void> {
        await this.account.updatePrefs({
            role: newRole
        })
    }

    async deleteUser(user: Partial<UserDTO>) {
        await this.account.deleteIdentity(user.id as string)
    }
}
