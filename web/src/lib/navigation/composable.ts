import type { Route } from "./Route";

export function composable<T>(
    route: Route<T>,
    factory: () => any
) {
    return {
        route,
        component: factory()
    };
}