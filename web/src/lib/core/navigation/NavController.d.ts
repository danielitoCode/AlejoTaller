import { type Writable } from "svelte/store";
import type { NavBackStackEntry } from "./NavBackStackEntry";
export declare class NavController {
    private readonly stackStore;
    constructor(startDestination: string);
    _getStackStore(): Writable<NavBackStackEntry[]>;
    navigate<T = unknown>(route: string, args?: T): void;
    popBackStack(): void;
}
