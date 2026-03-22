export var resolveColor = function (c) {
    return "var(--md-sys-color-".concat(c, ")");
};
export function resolveTintColor(color) {
    if (color.startsWith("#") || color.startsWith("rgb") || color.startsWith("hsl")) {
        return color; // directo
    }
    // Para token del tema, resolvemos var a hex/RGB real usando getComputedStyle
    var dummy = document.createElement("div");
    dummy.style.color = resolveColor(color); // var(--...)
    document.body.appendChild(dummy);
    var computed = getComputedStyle(dummy).color; // rgb(...) o hex
    document.body.removeChild(dummy);
    return computed;
}
// Ahora resolvemos cada propiedad por separado
export var resolveTextStyle = function (t) { return "\n    font-family: var(--md-sys-typescale-".concat(t, "-font-family);\n    font-size: var(--md-sys-typescale-").concat(t, "-font-size);\n    line-height: var(--md-sys-typescale-").concat(t, "-line-height);\n    letter-spacing: var(--md-sys-typescale-").concat(t, "-letter-spacing);\n    font-weight: var(--md-sys-typescale-").concat(t, "-font-weight);\n"); };
