/**
 * @param {any} route
 * @param {() => any} factory
 */
export function composable(route, factory) {
    return {
        route: route,
        component: factory()
    };
}
