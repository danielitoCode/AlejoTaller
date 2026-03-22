var __assign = (this && this.__assign) || function () {
    __assign = Object.assign || function(t) {
        for (var s, i = 1, n = arguments.length; i < n; i++) {
            s = arguments[i];
            for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p))
                t[p] = s[p];
        }
        return t;
    };
    return __assign.apply(this, arguments);
};
import { lightColors } from "./lightColors";
import { darkColors } from "./darkColors";
import { defaultTypography } from "./typography";
export var defaultLightTheme = {
    colorScheme: lightColors,
    typography: defaultTypography,
    shapes: {
        none: "0px",
        extraSmall: "4px",
        small: "8px",
        medium: "12px",
        large: "16px",
    },
    spacing: {
        xs: "4px",
        sm: "8px",
        md: "16px",
        lg: "24px",
        xl: "32px",
    },
    elevation: {
        level0: "none",
        level1: "0 1px 2px rgba(0,0,0,0.2)",
        level2: "0 2px 6px rgba(0,0,0,0.2)",
        level3: "0 6px 12px rgba(0,0,0,0.25)",
        level4: "0 12px 24px rgba(0,0,0,0.3)",
    },
};
export var defaultDarkTheme = __assign(__assign({}, defaultLightTheme), { colorScheme: darkColors });
