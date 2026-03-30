import { writable, type Writable } from "svelte/store";
import type { NavBackStackEntry } from "./NavBackStackEntry";

export class NavController {
    // 🔥 store creado UNA sola vez
    private readonly stackStore: Writable<NavBackStackEntry[]>;

    constructor(startDestination: string) {
        this.stackStore = writable([
            { route: startDestination }
        ]);
    }

    // ✅ siempre devuelve EL MISMO store
    _getStackStore() {
        return this.stackStore;
    }

    navigate<T = unknown>(route: string, args?: T) {
        this.stackStore.update(s => [...s, { route, args }]);
    }

    resetTo<T = unknown>(route: string, args?: T) {
        this.stackStore.set([{ route, args }]);
    }

    replaceTop<T = unknown>(route: string, args?: T) {
        this.stackStore.update((stack) => {
            if (!stack.length) return [{ route, args }];
            return [...stack.slice(0, -1), { route, args }];
        });
    }

    popBackStack() {
        this.stackStore.update(s =>
            s.length > 1 ? s.slice(0, -1) : s
        );
    }
}
