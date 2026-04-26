import { beforeEach, describe, expect, it, vi } from "vitest";
import { get } from "svelte/store";
import { profileStore } from "../../../../../../core/feature/auth/presentation/viewmodel/profile.store";

function createStorageMock() {
    const data = new Map<string, string>();
    return {
        getItem: vi.fn((key: string) => data.get(key) ?? null),
        setItem: vi.fn((key: string, value: string) => {
            data.set(key, value);
        }),
        removeItem: vi.fn((key: string) => {
            data.delete(key);
        }),
        clear: vi.fn(() => data.clear())
    };
}

describe("profileStore", () => {
    beforeEach(() => {
        const localStorage = createStorageMock();
        vi.stubGlobal("window", { localStorage });
        profileStore.reset({
            userId: "",
            email: "",
            name: "",
            phone: "",
            bio: "",
            avatarUrl: ""
        });
    });

    it("refresh the remote photo equal from photo_url when don't have local eraser", () => {
        profileStore.hydrateFromUser({
            $id: "user-1",
            email: "user@test.dev",
            name: "User",
            prefs: {
                phone: "+53",
                photo_url: "https://cdn.example.com/google-photo.jpg"
            }
        });

        expect(get(profileStore).avatarUrl).toBe("https://cdn.example.com/google-photo.jpg");
        expect(get(profileStore).phone).toBe("+53");
    });

    it("save the local avatar if the user have a desition into the remote photo", () => {
        window.localStorage.setItem(
            "alejo-taller-web-profile:user-1",
            JSON.stringify({
                avatarUrl: "https://cdn.example.com/custom-photo.jpg",
                bio: "Bio local"
            })
        );

        profileStore.hydrateFromUser({
            $id: "user-1",
            email: "user@test.dev",
            name: "User",
            prefs: {
                photoUrl: "https://cdn.example.com/google-photo.jpg"
            }
        });

        expect(get(profileStore).avatarUrl).toBe("https://cdn.example.com/custom-photo.jpg");
        expect(get(profileStore).bio).toBe("Bio local");
    });

    it("save a actual eraser photo to a localStorage when the user key", () => {
        profileStore.reset({
            userId: "user-9",
            email: "user9@test.dev",
            name: "User 9",
            phone: "+5355123456",
            bio: "Perfil de prueba",
            avatarUrl: "https://cdn.example.com/photo.jpg"
        });

        const saved = profileStore.save();

        expect(saved.userId).toBe("user-9");
        expect(window.localStorage.setItem).toHaveBeenCalledWith(
            "alejo-taller-web-profile:user-9",
            expect.stringContaining("\"avatarUrl\":\"https://cdn.example.com/photo.jpg\"")
        );
    });
});
