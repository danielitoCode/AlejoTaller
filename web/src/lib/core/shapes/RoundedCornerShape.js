function toCss(value) {
    if (value === undefined)
        return "0px";
    return typeof value === "number" ? "".concat(value, "px") : value;
}
var RoundedCornerShapeImpl = /** @class */ (function () {
    function RoundedCornerShapeImpl(params) {
        this.params = params;
    }
    RoundedCornerShapeImpl.prototype.toCssBorderRadius = function () {
        // Caso: un solo valor → todas las esquinas
        if (typeof this.params === "number" || typeof this.params === "string") {
            var v = toCss(this.params);
            return "".concat(v, " ").concat(v, " ").concat(v, " ").concat(v);
        }
        var _a = this.params, _b = _a.topStart, topStart = _b === void 0 ? 0 : _b, _c = _a.topEnd, topEnd = _c === void 0 ? 0 : _c, _d = _a.bottomEnd, bottomEnd = _d === void 0 ? 0 : _d, _e = _a.bottomStart, bottomStart = _e === void 0 ? 0 : _e;
        // CSS: top-left, top-right, bottom-right, bottom-left
        return "\n            ".concat(toCss(topStart), "\n            ").concat(toCss(topEnd), "\n            ").concat(toCss(bottomEnd), "\n            ").concat(toCss(bottomStart), "\n        ").trim();
    };
    return RoundedCornerShapeImpl;
}());
/**
 * Compose-like factory
 */
export function RoundedCornerShape(params) {
    return new RoundedCornerShapeImpl(params);
}
