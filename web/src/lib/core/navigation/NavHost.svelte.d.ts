import { Modifier } from "../modifier/Modifier";
import type { NavController } from "./NavController";
import type { ContentTransition } from "../motion/ContentTransition";
interface $$__sveltets_2_IsomorphicComponent<Props extends Record<string, any> = any, Events extends Record<string, any> = any, Slots extends Record<string, any> = any, Exports = {}, Bindings = string> {
    new (options: import('svelte').ComponentConstructorOptions<Props>): import('svelte').SvelteComponent<Props, Events, Slots> & {
        $$bindings?: Bindings;
    } & Exports;
    (internal: unknown, props: Props & {
        $$events?: Events;
        $$slots?: Slots;
    }): Exports & {
        $set?: any;
        $on?: any;
    };
    z_$$bindings?: Bindings;
}
declare const NavHost: $$__sveltets_2_IsomorphicComponent<{
    navController: NavController;
    routes: {
        route: {
            path: string;
        };
        component: any;
    }[];
    transition?: ContentTransition;
    modifier?: Modifier;
}, {
    [evt: string]: CustomEvent<any>;
}, {}, {}, string>;
type NavHost = InstanceType<typeof NavHost>;
export default NavHost;
