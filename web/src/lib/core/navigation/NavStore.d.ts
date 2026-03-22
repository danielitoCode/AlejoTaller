import type { NavBackStackEntry } from "./NavBackStackEntry";
export declare function createNavStore(startDestination: string): {
    stack: import("svelte/store").Writable<NavBackStackEntry[]>;
    navigate<T = unknown>(route: string, args?: T): void;
    pop(): void;
};
export type NavStore = ReturnType<typeof createNavStore>;
