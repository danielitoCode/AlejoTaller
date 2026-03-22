var defaultEnter = 280;
var defaultExit = 220;
export function fadeIn(duration) {
    if (duration === void 0) { duration = defaultEnter; }
    return {
        base: "transition-opacity duration-[".concat(duration, "ms] ease-out"),
        from: "opacity-0",
        to: "opacity-100",
        duration: duration
    };
}
export function fadeOut(duration) {
    if (duration === void 0) { duration = defaultExit; }
    return {
        base: "transition-opacity duration-[".concat(duration, "ms] ease-in"),
        from: "opacity-100",
        to: "opacity-0",
        duration: duration
    };
}
export function scaleIn(duration) {
    if (duration === void 0) { duration = defaultEnter; }
    return {
        base: "transition-all duration-[".concat(duration, "ms] ease-out"),
        from: "opacity-0 scale-95",
        to: "opacity-100 scale-100",
        duration: duration
    };
}
export function scaleOut(duration) {
    if (duration === void 0) { duration = defaultExit; }
    return {
        base: "transition-all duration-[".concat(duration, "ms] ease-in"),
        from: "opacity-100 scale-100",
        to: "opacity-0 scale-95",
        duration: duration
    };
}
export function slideIn(duration, direction) {
    if (duration === void 0) { duration = defaultEnter; }
    if (direction === void 0) { direction = "right"; }
    var fromMap = {
        left: "-translate-x-4",
        right: "translate-x-4",
        up: "-translate-y-4",
        down: "translate-y-4"
    };
    return {
        base: "transition-all duration-[".concat(duration, "ms] ease-out"),
        from: "opacity-0 ".concat(fromMap[direction]),
        to: "opacity-100 translate-x-0 translate-y-0",
        duration: duration
    };
}
export function slideOut(duration, direction) {
    if (duration === void 0) { duration = defaultExit; }
    if (direction === void 0) { direction = "right"; }
    var toMap = {
        left: "-translate-x-4",
        right: "translate-x-4",
        up: "-translate-y-4",
        down: "translate-y-4"
    };
    return {
        base: "transition-all duration-[".concat(duration, "ms] ease-in"),
        from: "opacity-100 translate-x-0 translate-y-0",
        to: "opacity-0 ".concat(toMap[direction]),
        duration: duration
    };
}
