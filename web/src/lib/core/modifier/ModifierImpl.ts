import type { Shape } from "../shapes/Shape";
import type { ColorToken } from "../theme/ColorScheme";

// Local fallback type: the original layout alignment module was moved/removed.
export type BoxAlignment = unknown;
export type ModifierMeta = {
    align?: BoxAlignment;
};
export type ModifierEntry = {
    className?: string;
    style?: string;
    meta?: ModifierMeta;
};
export declare class ModifierImpl {
    private readonly entries;
    constructor(entries?: ModifierEntry[]);
    then(other: ModifierImpl): ModifierImpl;
    fillMaxWidth(): ModifierImpl;
    fillMaxHeight(): ModifierImpl;
    fillMaxSize(): ModifierImpl;
    weight(weight: number, fill?: boolean): ModifierImpl;
    padding(value: number): ModifierImpl;
    paddingHorizontal(value: number): ModifierImpl;
    paddingVertical(value: number): ModifierImpl;
    marginTop(value: number): ModifierImpl;
    background(color: ColorToken | string): ModifierImpl;
    border(width: number, color: string, shape?: Shape): ModifierImpl;
    clip(shape: Shape): ModifierImpl;
    align(alignment: BoxAlignment): ModifierImpl;
    clickable(): ModifierImpl;
    toStyle(): string;
    toClass(): string;
    getMeta(): ModifierMeta;
}
