export function composable(route, factory) {
    return {
        route: route,
        component: factory()
    };
}
