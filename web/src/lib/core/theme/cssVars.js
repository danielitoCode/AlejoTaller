export function themeToCssVars(theme) {
    var vars = [];
    Object.entries(theme.colorScheme).forEach(function (_a) {
        var k = _a[0], v = _a[1];
        return vars.push("--md-sys-color-".concat(k, ": ").concat(v, ";"));
    });
    // === Tipografía: una variable por cada propiedad (¡esto soluciona el bug!) ===
    Object.entries(theme.typography).forEach(function (_a) {
        var styleKey = _a[0], style = _a[1];
        vars.push("--md-sys-typescale-".concat(styleKey, "-font-family: ").concat(style.fontFamily, ";"));
        vars.push("--md-sys-typescale-".concat(styleKey, "-font-size: ").concat(style.fontSize, ";"));
        vars.push("--md-sys-typescale-".concat(styleKey, "-line-height: ").concat(style.lineHeight, ";"));
        vars.push("--md-sys-typescale-".concat(styleKey, "-letter-spacing: ").concat(style.letterSpacing, ";"));
        vars.push("--md-sys-typescale-".concat(styleKey, "-font-weight: ").concat(style.fontWeight, ";"));
    });
    Object.entries(theme.shapes).forEach(function (_a) {
        var k = _a[0], v = _a[1];
        return vars.push("--md-sys-shape-".concat(k, ": ").concat(v, ";"));
    });
    Object.entries(theme.elevation).forEach(function (_a) {
        var k = _a[0], v = _a[1];
        return vars.push("--md-sys-elevation-".concat(k, ": ").concat(v, ";"));
    });
    Object.entries(theme.spacing).forEach(function (_a) {
        var k = _a[0], v = _a[1];
        return vars.push("--md-sys-spacing-".concat(k, ": ").concat(v, ";"));
    });
    return vars.join("\n");
}
