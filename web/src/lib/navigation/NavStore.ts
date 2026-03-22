import { writable } from "svelte/store";
import type { NavBackStackEntry } from "./NavBackStackEntry";

export function createNavStore(startDestination: string) {
    const stack = writable<NavBackStackEntry[]>([
        { route: startDestination }
    ]);

    return {
        stack,

        navigate<T = unknown>(route: string, args?: T) {
            stack.update(s => [...s, { route, args }]);
        },

        pop() {
            stack.update(s => (s.length > 1 ? s.slice(0, -1) : s));
        }
    };
}

export type NavStore = ReturnType<typeof createNavStore>;