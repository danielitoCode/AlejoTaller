import type { Shape } from "./Shape";
type CornerSize = number | string;
type RoundedCornerParams = CornerSize | {
    topStart?: CornerSize;
    topEnd?: CornerSize;
    bottomEnd?: CornerSize;
    bottomStart?: CornerSize;
};
/**
 * Compose-like factory
 */
export declare function RoundedCornerShape(params: RoundedCornerParams): Shape;
export {};
