import { writable } from "svelte/store";

export interface ProfileDraft {
    userId: string;
    email: string;
    name: string;
    phone: string;
    bio: string;
    avatarUrl: string;
}

const STORAGE_PREFIX = "alejo-taller-web-profile";

function storageKey(userId: string): string {
    return `${STORAGE_PREFIX}:${userId}`;
}

function persistProfile(profile: ProfileDraft): void {
    if (typeof window === "undefined" || !profile.userId) return;
    window.localStorage.setItem(storageKey(profile.userId), JSON.stringify(profile));
}

function createEmptyProfile(): ProfileDraft {
    return {
        userId: "",
        email: "",
        name: "",
        phone: "",
        bio: "",
        avatarUrl: ""
    };
}

function createProfileStore() {
    const { subscribe, set, update } = writable<ProfileDraft>(createEmptyProfile());

    function hydrateFromUser(user: { $id?: string; email?: string; name?: string; prefs?: Record<string, unknown> }) {
        const userId = user.$id ?? "";
        const remoteAvatar =
            typeof user.prefs?.avatarUrl === "string"
                ? user.prefs.avatarUrl
                : (typeof user.prefs?.photo_url === "string"
                    ? user.prefs.photo_url
                    : (typeof user.prefs?.photoUrl === "string" ? user.prefs.photoUrl : ""));
        const base: ProfileDraft = {
            userId,
            email: user.email ?? "",
            name: user.name ?? "",
            phone: typeof user.prefs?.phone === "string" ? user.prefs.phone : "",
            bio: typeof user.prefs?.bio === "string" ? user.prefs.bio : "",
            avatarUrl: remoteAvatar
        };

        if (typeof window === "undefined" || !userId) {
            set(base);
            return;
        }

        try {
            const raw = window.localStorage.getItem(storageKey(userId));
            if (!raw) {
                set(base);
                return;
            }

            const local = JSON.parse(raw) as Partial<ProfileDraft>;
            set({
                ...base,
                ...local,
                avatarUrl: (local.avatarUrl?.trim() || base.avatarUrl || "")
            });
        } catch {
            set(base);
        }
    }

    function patch(partial: Partial<ProfileDraft>) {
        update((state) => ({ ...state, ...partial }));
    }

    function reset(profile: ProfileDraft) {
        set(profile);
    }

    function save() {
        let snapshot = createEmptyProfile();
        update((state) => {
            snapshot = state;
            return state;
        });
        persistProfile(snapshot);
        return snapshot;
    }

    return {
        subscribe,
        hydrateFromUser,
        patch,
        reset,
        save
    };
}

export const profileStore = createProfileStore();
