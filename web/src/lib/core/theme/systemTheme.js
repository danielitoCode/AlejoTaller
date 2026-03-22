import { readable } from "svelte/store";
export var systemTheme = readable("light", function (set) {
    if (typeof window === "undefined")
        return;
    var media = window.matchMedia("(prefers-color-scheme: dark)");
    var update = function () {
        set(media.matches ? "dark" : "light");
    };
    update(); // estado inicial
    media.addEventListener("change", update);
    return function () {
        media.removeEventListener("change", update);
    };
});
