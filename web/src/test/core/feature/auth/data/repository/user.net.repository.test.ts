import { describe, expect, it, vi } from "vitest";
import { UserNetRepositoryImpl } from "../../../../../../core/feature/auth/data/repository/user.net.repository";

describe("UserNetRepositoryImpl", () => {
    it("lee la foto remota desde claves compatibles y resuelve el rol desde labels", async () => {
        const account = {
            get: vi.fn().mockResolvedValue({
                $id: "user-1",
                name: "Alejo",
                email: "alejo@test.dev",
                phone: "+5355000000",
                emailVerification: true,
                labels: ["viewer"],
                prefs: {
                    sub: "google-sub",
                    avatarUrl: "https://cdn.example.com/avatar.jpg"
                }
            })
        };

        const repository = new UserNetRepositoryImpl(account as any);
        const currentUser = await repository.getCurrentUser();

        expect(currentUser.photo_url).toBe("https://cdn.example.com/avatar.jpg");
        expect(currentUser.role).toBe("viewer");
        expect(currentUser.sub).toBe("google-sub");
    });

    it("persiste la foto en todas las claves compatibles al actualizarla", async () => {
        const account = {
            updatePrefs: vi.fn()
        };

        const repository = new UserNetRepositoryImpl(account as any);
        await repository.updatePhotoUrl("https://cdn.example.com/new-photo.jpg");

        expect(account.updatePrefs).toHaveBeenCalledWith({
            photo_url: "https://cdn.example.com/new-photo.jpg",
            photoUrl: "https://cdn.example.com/new-photo.jpg",
            avatarUrl: "https://cdn.example.com/new-photo.jpg"
        });
    });

    it("solo rellena la foto de Google si la cuenta actual no tenia una foto propia", async () => {
        const account = {
            get: vi.fn().mockResolvedValue({
                prefs: {
                    photo_url: "https://cdn.example.com/custom-photo.jpg"
                }
            }),
            updatePrefs: vi.fn()
        };

        const repository = new UserNetRepositoryImpl(account as any);
        await repository.linkGoogle("google-sub", "https://cdn.example.com/google-photo.jpg", "Alejo");

        expect(account.updatePrefs).toHaveBeenCalledWith({
            sub: "google-sub",
            name: "Alejo",
            google_linked: true
        });
    });
});
