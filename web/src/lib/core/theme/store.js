import { writable, derived } from "svelte/store";
import { systemTheme } from "./systemTheme";
export var themeState = writable(null);
// 🔑 ESTE es el tema efectivo (como MaterialTheme)
export var resolvedTheme = derived([themeState, systemTheme], function (_a) {
    var $theme = _a[0], $system = _a[1];
    if (!$theme)
        return null;
    var mode = $theme.mode === "system"
        ? $system
        : $theme.mode;
    return mode === "dark"
        ? $theme.dark
        : $theme.light;
});
