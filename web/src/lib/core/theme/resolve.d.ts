import type { ColorToken } from "./ColorScheme";
import type { TextStyleToken } from "./TextStyle";
export declare const resolveColor: (c: ColorToken) => string;
export declare function resolveTintColor(color: ColorToken | string): string;
export declare const resolveTextStyle: (t: TextStyleToken) => string;
