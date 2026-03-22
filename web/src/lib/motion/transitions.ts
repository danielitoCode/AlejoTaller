import type {AnimationSpec} from "./AnimationsSpec";

const defaultEnter = 280;
const defaultExit = 220;

export function fadeIn(duration: number = defaultEnter): AnimationSpec {
    return {
        base: `transition-opacity duration-[${duration}ms] ease-out`,
        from: "opacity-0",
        to: "opacity-100",
        duration
    };
}

export function fadeOut(duration: number = defaultExit): AnimationSpec {
    return {
        base: `transition-opacity duration-[${duration}ms] ease-in`,
        from: "opacity-100",
        to: "opacity-0",
        duration
    };
}

export function scaleIn(duration: number = defaultEnter): AnimationSpec {
    return {
        base: `transition-all duration-[${duration}ms] ease-out`,
        from: "opacity-0 scale-95",
        to: "opacity-100 scale-100",
        duration
    };
}

export function scaleOut(duration: number = defaultExit): AnimationSpec {
    return {
        base: `transition-all duration-[${duration}ms] ease-in`,
        from: "opacity-100 scale-100",
        to: "opacity-0 scale-95",
        duration
    };
}

export function slideIn(
    duration: number = defaultEnter,
    direction: "left" | "right" | "up" | "down" = "right"
): AnimationSpec {
    const fromMap = {
        left: "-translate-x-4",
        right: "translate-x-4",
        up: "-translate-y-4",
        down: "translate-y-4"
    };

    return {
        base: `transition-all duration-[${duration}ms] ease-out`,
        from: `opacity-0 ${fromMap[direction]}`,
        to: "opacity-100 translate-x-0 translate-y-0",
        duration
    };
}

export function slideOut(
    duration: number = defaultExit,
    direction: "left" | "right" | "up" | "down" = "right"
): AnimationSpec {
    const toMap = {
        left: "-translate-x-4",
        right: "translate-x-4",
        up: "-translate-y-4",
        down: "translate-y-4"
    };

    return {
        base: `transition-all duration-[${duration}ms] ease-in`,
        from: "opacity-100 translate-x-0 translate-y-0",
        to: `opacity-0 ${toMap[direction]}`,
        duration
    };
}
