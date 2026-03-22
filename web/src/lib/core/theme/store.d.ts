import type { ComposeTheme, ThemeMode } from "./theme";
export declare const themeState: import("svelte/store").Writable<{
    light: ComposeTheme;
    dark: ComposeTheme;
    mode: ThemeMode;
}>;
export declare const resolvedTheme: import("svelte/store").Readable<ComposeTheme>;
