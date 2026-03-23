// =======================================
// Compose Svelted – Public API (Core V2)
// =======================================

import type { SvelteComponent } from "svelte";

export class NavHost extends SvelteComponent {}

// ---------- TS-only API (must resolve inside the installed package) ----------
// IMPORTANT: These paths must exist in the published tarball.
// If you are publishing `dist/core/...` etc. (as your npm pack log showed), keep these.
export * from "./motion";
export * from "./navigation";
export * from "./core/modifier/Modifier"
export * from "./components/Alignment";

export { default as Box } from "./component/layouts/Box.svelte";


/*// =========================
// COMPONENTS (Svelte)
// =========================
// LAYOUTS
export { default as ComposeTheme } from "./core/theme/ComposeTheme.svelte";
export { default as AppRoot } from "./component/AppRoot.svelte"
// COMPONENTS
export { default as Surface } from "./component/Surface.svelte";
export { default as Text } from "./component/Text.svelte";
export { default as Column } from "./component/layouts/Column.svelte";
export { default as Row } from "./component/layouts/Row.svelte";
export { default as Box } from "./component/layouts/Box.svelte";
export { default as Spacer } from "./component/Spacer.svelte"
export { default as Button } from "./component/buttons/Button.svelte"
export { default as IconButton } from "./component/buttons/IconButton.svelte"
export { default as ButtonWithIcon } from "./component/buttons/ButtonWithIcon.svelte"
export { default as OutlinedIconButton } from "./component/buttons/OutlinedButton.svelte"
export { default as OutlinedButtonWithIcon } from "./component/buttons/OutlinedButtonWithIcon.svelte"
export { default as CheckButton } from "./component/buttons/CheckButton.svelte"
export { default as TextButton } from "./component/buttons/TextButton.svelte"
export { default as OutlinedButton } from "./component/buttons/OutlinedButton.svelte"
export { default as Card } from "./component/cards/Card.svelte"
export { default as OutlinedCard } from "./component/cards/OutlinedCard.svelte"
export { default as TextField } from "./component/textFields/TextField.svelte"
export { default as OutlinedTextField } from "./component/textFields/OutlinedTextField.svelte"
export { default as TonalButton } from "./component/TonalButton.svelte"
export { default as LazyColumn } from "./component/layouts/LazyColumn.svelte"
export { default as LazyRow } from "./component/layouts/LazyRow.svelte"
export { default as Icon } from "./component/Icon.svelte"
export { default as Image } from "./component/Image.svelte"
export { default as Scaffold } from "./component/layouts/Scaffold.svelte"
// Motions
export { default as AnimatedVisibility } from "./component/motion/AnimatedVisibility.svelte"
export { default as AnimatedContent } from "./component/motion/AnimatedContent.svelte"
// Custom
export { default as CodeBlock } from "./component/custom/CodeBlock.svelte"
// Navigation
export { default as NavHost } from "./core/navigation/NavHost.svelte";

// =========================
// TOKENS / API (TS only)
// =========================
export * from "./core/shapes/RoundedCornerShape"
export * from "./core/theme/ColorScheme";
export * from "./core/theme/TextStyle";
export * from "./core/modifier/Modifier"
export * from "./component/layouts/Arrangement";
export * from "./component/layouts/Alignment";
export * from "./component/ContentScale"
export * from "./core/helpers/painterResource"
export * from "./component/ContentScale"
export * from "./core/theme/Density"
export * from "./core/motion/AnimationSpec"
export * from "./core/motion/transitions"
export * from "./core/motion/contentTransitions"
export * from "./core/navigation/Route";
export * from "./core/navigation/NavController";
export * from "./core/navigation/rememberNavController";
export * from "./core/navigation/composable";*/