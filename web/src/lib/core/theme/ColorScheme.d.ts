import type { ColorScheme as Def } from "./colors";
export type ColorToken = keyof Def;
export declare const ColorScheme: {
    readonly Primary: "primary";
    readonly OnPrimary: "onPrimary";
    readonly Secondary: "secondary";
    readonly OnSecondary: "onSecondary";
    readonly Background: "background";
    readonly OnBackground: "onBackground";
    readonly Surface: "surface";
    readonly OnSurface: "onSurface";
    readonly SurfaceVariant: "surfaceVariant";
    readonly OnSurfaceVariant: "onSurfaceVariant";
    readonly Outline: "outline";
    readonly Error: "error";
    readonly OnError: "onError";
};
