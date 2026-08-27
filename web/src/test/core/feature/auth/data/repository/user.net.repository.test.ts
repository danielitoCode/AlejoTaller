import { describe, expect, it, vi } from "vitest";
import { UserNetRepositoryImpl } from "../../../../../../core/feature/auth/data/repository/user.net.repository";

describe("UserNetRepositoryImpl", () => {
    it("read a remote photo from shared keys and take the rol from labels", async () => {
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

    it("save a photo in all success keys on update her", async () => {
        const account = {
            get: vi.fn().mockResolvedValue({
                prefs: {
                    sub: "google-sub",
                    role: "viewer",
                    phone: "+5355000000"
                }
            }),
            updatePrefs: vi.fn()
        };

        const repository = new UserNetRepositoryImpl(account as any);
        await repository.updatePhotoUrl("https://cdn.example.com/new-photo.jpg");

        expect(account.get).toHaveBeenCalled();
        expect(account.updatePrefs).toHaveBeenCalledWith({
            sub: "google-sub",
            role: "viewer",
            phone: "+5355000000",
            photo_url: "https://cdn.example.com/new-photo.jpg",
            photoUrl: "https://cdn.example.com/new-photo.jpg",
            avatarUrl: "https://cdn.example.com/new-photo.jpg"
        });
    });

    it("just take a picture if the google account don't have a self photo", async () => {
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

        // mergePrefs preserves existing photo_url and does NOT overwrite with Google photo
        expect(account.updatePrefs).toHaveBeenCalledWith({
            photo_url: "https://cdn.example.com/custom-photo.jpg",
            sub: "google-sub",
            name: "Alejo",
            google_linked: true
        });
    });
});
