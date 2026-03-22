import { ModifierImpl } from "./ModifierImpl";
import type { BoxAlignment } from "./ModifierImpl";
import type { Shape } from "../shapes/Shape";
import type { ColorToken } from "../theme/ColorScheme";
/**
 * Modifier
 *
 * Modifier is an immutable, chainable object used to decorate or augment
 * a UI component.
 *
 * Inspired by Jetpack Compose Modifier.
 *
 * Usage:
 * ```
 * Modifier
 *   .padding(16)
 *   .fillMaxWidth()
 *   .background(ColorScheme.Surface)
 * ```
 */
export declare const Modifier: {
    /**
     * Returns an empty Modifier with no effects.
     *
     * Useful as a default value or starting point.
     */
    readonly empty: () => ModifierImpl;
    /**
     * Adds padding around the content.
     *
     * Can be used with:
     * - a single number (uniform padding)
     * - an object with directional values
     *
     * Examples:
     * ```
     * Modifier.padding(16)
     * Modifier.padding({ top: 8, bottom: 8 })
     * ```
     */
    readonly padding: (valueOrParams?: number | {
        top?: number;
        bottom?: number;
        start?: number;
        end?: number;
    }, unit?: string) => ModifierImpl;
    /**
     * Adds horizontal padding (left and right).
     */
    readonly paddingHorizontal: (value: number) => ModifierImpl;
    /**
     * Adds a border around the component.
     *
     * Optionally accepts a Shape to match rounded corners.
     *
     * Examples:
     * ```
     * Modifier.border(1, ColorScheme.Outline)
     * Modifier.border(2, "#FF0000", RoundedCornerShape(12))
     * ```
     */
    readonly border: (width: number, color: string, shape?: Shape) => ModifierImpl;
    /**
     * Marks the component as clickable.
     *
     * This modifier applies interaction semantics such as:
     * - pointer cursor
     * - user-select disabling
     *
     * Note:
     * The click handler must still be attached at the component level.
     *
     * Example:
     * ```
     * <Box
     *   on:click={onClick}
     *   modifier={Modifier.clickable(onClick)}
     * />
     * ```
     */
    readonly clickable: (onClick: () => void) => ModifierImpl;
    /**
     * Offsets the component visually without affecting its layout.
     *
     * Equivalent to Jetpack Compose Modifier.offset.
     *
     * Note:
     * This uses CSS transform and does not affect surrounding layout.
     */
    readonly offset: (x: number, y: number) => ModifierImpl;
    /**
     * Enables vertical scrolling for the component.
     *
     * Useful for Column or large content containers.
     */
    readonly verticalScroll: (enabled?: boolean) => ModifierImpl;
    /**
     * Enables horizontal scrolling for the component.
     */
    readonly horizontalScroll: (enabled?: boolean) => ModifierImpl;
    /**
     * Adds vertical padding (top and bottom).
     */
    readonly paddingVertical: (value: number) => ModifierImpl;
    /**
     * Aligns the component inside a Box.
     *
     * ⚠️ This modifier is intended to be used only inside Box layouts.
     *
     * Example:
     * ```
     * Modifier.align(Alignment.Center)
     * ```
     */
    readonly align: (alignment: BoxAlignment) => ModifierImpl;
    /**
     * Makes the component fill the maximum available width.
     */
    readonly fillMaxWidth: () => ModifierImpl;
    /**
     * Makes the component fill the maximum available height.
     */
    readonly fillMaxHeight: () => ModifierImpl;
    /**
     * Makes the component fill both width and height.
     */
    readonly fillMaxSize: () => ModifierImpl;
    /**
     * Sets a fixed height for the component.
     *
     * Accepts either a number (px by default) or a CSS size string.
     */
    readonly height: (value: number | string, unit?: string) => ModifierImpl;
    /**
     * Sets a fixed width for the component.
     *
     * Accepts either a number (px by default) or a CSS size string.
     */
    readonly width: (value: number | string, unit?: string) => ModifierImpl;
    /**
     * Applies a background color to the component.
     *
     * Accepts either:
     * - A CSS color string (e.g. "#2A2A2A", "transparent")
     * - A Compose color token (e.g. ColorScheme.Surface)
     *
     * Examples:
     * ```
     * Modifier.background(ColorScheme.Surface)
     * Modifier.background("#121212")
     * ```
     */
    readonly background: (color: ColorToken | string) => ModifierImpl;
    /**
     * Assigns a proportional weight to the component inside
     * a Row or Column.
     *
     * Similar to flex-grow.
     *
     * Example:
     * ```
     * Modifier.weight(1)
     * ```
     */
    readonly weight: (weight: number, fill?: boolean) => ModifierImpl;
    /**
     * Assigns weight without forcing fill behavior.
     */
    readonly weightNoFill: (weight: number) => ModifierImpl;
    /**
     * Adds top margin to the component.
     */
    readonly marginTop: (value: number, unit?: string) => ModifierImpl;
    /**
     * Clips the component using the provided Shape.
     *
     * Example:
     * ```
     * Modifier.clip(RoundedCornerShape(16))
     * ```
     */
    readonly clip: (shape: Shape) => ModifierImpl;
    /**
     * Sets both width and height to the same value.
     *
     * Useful for icons and square component.
     *
     * Example:
     * ```
     * Modifier.size(24)
     * ```
     */
    readonly size: (value: number | string, unit?: string) => ModifierImpl;
};
export type Modifier = ModifierImpl;
