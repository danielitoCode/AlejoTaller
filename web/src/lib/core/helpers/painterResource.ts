/**
 * painterResource - como en Jetpack Compose
 * Carga un recurso local desde src/lib/assets/
 * Soporta SVG (?raw para string) e imágenes (?url para URL)
 */
export declare const Res: {
    readonly raw: (fileName: string) => string;
    readonly image: (fileName: string) => string;
    readonly values: (fileName: string) => string;
    readonly fonts: (fileName: string) => string;
};
export declare function painterResource(resourceName: string): string;
