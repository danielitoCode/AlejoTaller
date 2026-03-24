import { writable } from "svelte/store";

export interface AppSettings {
    darkMode: boolean;
    notificationsEnabled: boolean;
    hapticFeedbackEnabled: boolean;
}

const STORAGE_KEY = "alejo-taller-web-settings";

const defaultState: AppSettings = {
    darkMode: false,
    notificationsEnabled: true,
    hapticFeedbackEnabled: true
};

function readInitialState(): AppSettings {
    if (typeof window === "undefined") return defaultState;

    const raw = window.localStorage.getItem(STORAGE_KEY);
    if (!raw) return defaultState;

    try {
        return { ...defaultState, ...JSON.parse(raw) };
    } catch {
        return defaultState;
    }
}

function persist(state: AppSettings): void {
    if (typeof window === "undefined") return;
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
}

function createSettingsStore() {
    const { subscribe, update } = writable<AppSettings>(readInitialState());

    function patch(partial: Partial<AppSettings>): void {
        update((state) => {
            const next = { ...state, ...partial };
            persist(next);
            return next;
        });
    }

    return {
        subscribe,
        updateDarkMode: (darkMode: boolean) => patch({ darkMode }),
        updateNotifications: (notificationsEnabled: boolean) => patch({ notificationsEnabled }),
        updateHapticFeedback: (hapticFeedbackEnabled: boolean) => patch({ hapticFeedbackEnabled })
    };
}

export const settingsStore = createSettingsStore();
