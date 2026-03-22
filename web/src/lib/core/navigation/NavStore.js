var __spreadArray = (this && this.__spreadArray) || function (to, from, pack) {
    if (pack || arguments.length === 2) for (var i = 0, l = from.length, ar; i < l; i++) {
        if (ar || !(i in from)) {
            if (!ar) ar = Array.prototype.slice.call(from, 0, i);
            ar[i] = from[i];
        }
    }
    return to.concat(ar || Array.prototype.slice.call(from));
};
import { writable } from "svelte/store";
export function createNavStore(startDestination) {
    var stack = writable([
        { route: startDestination }
    ]);
    return {
        stack: stack,
        navigate: function (route, args) {
            stack.update(function (s) { return __spreadArray(__spreadArray([], s, true), [{ route: route, args: args }], false); });
        },
        pop: function () {
            stack.update(function (s) { return (s.length > 1 ? s.slice(0, -1) : s); });
        }
    };
}
