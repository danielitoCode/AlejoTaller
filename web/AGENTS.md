# AGENTS — Guía rápida para agentes de IA

Objetivo: instrucciones concretas para ser productivo en este repo frontend (Svelte + TypeScript + Vite).

- Estructura clave:
  - feature-first: `src/core/feature/<feature>/presentation/...` contiene screens, viewmodels y componentes específicos.
  - infraestructura compartida: `src/core/infrastructure/` (routing, utilidades, componentes contenedores).
  - libs reutilizables: `src/lib/` y `src/demo/`.

- Reglas prácticas para cambios:
  - Prioridad 1: corregir imports rotos (errores de Vite). Busca rutas relativas exactas desde el archivo que importa.
  - Prioridad 2: evitar romper boundaries feature↔infrastructure. Features pueden usar infra utilities, no viceversa.
  - Prioridad 3: mantener stores tipadas; exportar métodos explícitos (p.ej. `saleAlertStore.addAlert`).

- Convenciones de código:
  - Stores: usar Svelte `writable/derived`, exponer funciones (subscribe + api). Ejemplo: `src/core/feature/sale/presentation/viewmodel/sale-alert.store.ts`.
  - Evitar `any` salvo justificado con comentario TODO.
  - Proteger acceso a `window` / `Notification` / `localStorage` con `if (typeof window !== 'undefined')`.

- Patterns específicos detectados:
  - Pusher/realtime: `saleStore` gestiona suscripción según usuario y ventas sin verificar (ver `sale.store.ts`).
  - Notificaciones: se usan `toastStore`, `Notification` del navegador y `saleAlertStore` para mostrar alertas y persistir estado en memoria.
  - Logging: hay un interceptor global en `src/core/infrastructure/presentation/util/console.interceptor.ts` y `log.store` para persistir logs.

- Qué arreglar primero (lista mínima de acciones):
  1. Corregir imports que den error de Vite (p.ej. `../../../feature/...` vs `../../../sale/...`).
 2. Adecuar tipos en stores que reciben valores opcionales (p.ej. `auth-flow.store.ts::setSuccess`).
 3. Mejorar interceptación de `console` para respetar tipos y evitar errores TSC.
 4. Añadir comprobaciones `event.target` en handlers de input (`InternalProfileScreen.svelte`).
 5. Asegurar padding inferior seguro en `Screen.svelte` para evitar contenido oculto por FAB/barras.

- Comandos útiles (PowerShell):
  pnpm install; pnpm dev
  pnpm tsc --noEmit

- Buenas prácticas para commits por agente:
  - Hacer commits pequeños y atómicos: `fix(store): ...`, `chore(ui): ...`.
  - Ejecutar typecheck y `pnpm dev` tras cambios que afecten imports o tipos.

Leer antes de cambiar: `src/core/feature/**/README.md` y `src/core/infrastructure/README.md` cuando existan.
