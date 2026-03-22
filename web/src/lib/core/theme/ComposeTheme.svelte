<script lang="ts">
    import { themeState, resolvedTheme } from "./store";
    import { themeToCssVars } from "./cssVars";
    import { defaultLightTheme, defaultDarkTheme } from "./defaults/defaultTheme";
    import type { ComposeTheme as Theme, ThemeMode } from "./theme";

    export let light: Theme | undefined = undefined;
    export let dark: Theme | undefined = undefined;
    export let mode: ThemeMode = "system";

    $: themeState.set({
        light: light ?? defaultLightTheme,
        dark: dark ?? defaultDarkTheme,
        mode,
    });

    $: css = $resolvedTheme ? themeToCssVars($resolvedTheme) : "";
</script>

<div style={css}>
    <slot />
</div>