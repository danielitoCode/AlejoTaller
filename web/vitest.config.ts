import { defineConfig } from "vitest/config";
import { svelte } from "@sveltejs/vite-plugin-svelte";

export default defineConfig({
    plugins: [svelte({ hot: false })], // Hot debe estar off para tests
    test: {
        environment: "jsdom",
        include: ["src/test/**/*.test.ts", "src/test/**/*.test.svelte"],
        setupFiles: ["./src/test/setup.ts"],
        clearMocks: true,
        coverage: {
            provider: "v8",
            reporter: ["text", "html", "lcov"],
            exclude: [
                "node_modules/",
                "src/test/",
                "**/*.d.ts",
                "**/*.config.ts",
                "**/di/**/*.ts", // Containers de DI
                "**/fixtures/**", // Datos de prueba
                "**/*.svelte" // Componentes Svelte (opcional, remover si se quiere cubrir)
            ],
            thresholds: {
                global: {
                    branches: 60,    // 60% cobertura de ramas
                    functions: 65,   // 65% cobertura de funciones
                    lines: 65,       // 65% cobertura de líneas
                    statements: 65   // 65% cobertura de statements
                }
            }
        }
    }
});
