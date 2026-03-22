import type { Route } from "./Route";
export declare function composable<T>(route: Route<T>, factory: () => any): {
    route: Route<T>;
    component: any;
};
