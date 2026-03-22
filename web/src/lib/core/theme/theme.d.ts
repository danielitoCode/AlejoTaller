import type { ColorScheme } from "./colors";
import type { Typography } from "./typography";
import type { Shapes } from "./shapes";
import type { Spacing } from "./spacing";
import type { Elevation } from "./elevation";
export interface ComposeTheme {
    colorScheme: ColorScheme;
    typography: Typography;
    shapes: Shapes;
    spacing: Spacing;
    elevation: Elevation;
}
export type ThemeMode = "light" | "dark" | "system";
