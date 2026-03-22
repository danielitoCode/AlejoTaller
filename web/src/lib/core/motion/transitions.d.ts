import type { AnimationSpec } from "./AnimationSpec";
export declare function fadeIn(duration?: number): AnimationSpec;
export declare function fadeOut(duration?: number): AnimationSpec;
export declare function scaleIn(duration?: number): AnimationSpec;
export declare function scaleOut(duration?: number): AnimationSpec;
export declare function slideIn(duration?: number, direction?: "left" | "right" | "up" | "down"): AnimationSpec;
export declare function slideOut(duration?: number, direction?: "left" | "right" | "up" | "down"): AnimationSpec;
