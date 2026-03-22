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
var NavController = /** @class */ (function () {
    function NavController(startDestination) {
        this.stackStore = writable([
            { route: startDestination }
        ]);
    }
    // ✅ siempre devuelve EL MISMO store
    NavController.prototype._getStackStore = function () {
        return this.stackStore;
    };
    NavController.prototype.navigate = function (route, args) {
        this.stackStore.update(function (s) { return __spreadArray(__spreadArray([], s, true), [{ route: route, args: args }], false); });
    };
    NavController.prototype.popBackStack = function () {
        this.stackStore.update(function (s) {
            return s.length > 1 ? s.slice(0, -1) : s;
        });
    };
    return NavController;
}());
export { NavController };
