# AGENTS — Guía rápida para agentes de IA

Objetivo: instrucciones concretas para ser productivo en este repo frontend Svelte+TypeScript+Vite orientado a un taller mecánico (compra, reservas, notificaciones realtime).

## Arquitectura de la Aplicación

**Big Picture**: Interfaz web para clientes de taller que pueden comprar productos/servicios y hacer reservas. Notificaciones realtime vía Pusher. Features aisladas con infraestructura compartida.

### Estructura de Directorios Clave

```
src/
├── core/
│   ��── feature/                          # Features aisladas (respetar limites)
│   │   ├── auth/                         # Google + Appwrite auth
│   │   ├── product/                      # Catálogo y búsqueda
│   │   ├── category/                     # Categorización
│   │   ├── sale/                         # Carrito, verificación, reservas
│   │   │   └── presentation/viewmodel/
│   │   │       ├── sale.store.ts         # Suscripción Pusher por usuario
│   │   │       ├── sale-alert.store.ts   # Alertas verificación en memoria
│   │   │       └── cart.store.ts         # Carrito temporal
│   │   ├── notification/                 # Promociones y alertas
│   │   ├── exchange/                     # Conversión de moneda
│   │   └── settigns/                     # (notar typo en carpeta)
│   └── infrastructure/                   # Compartido (NO importa features)
│       ├── presentation/
│       │   ├── components/
│       │   │   ├── Screen.svelte          # Layout base con padding seguro
│       │   │   └── SaleVerificationAlert.svelte
│       │   ├── navigation/
│       │   │   └── NestedNavigationWrapper.svelte  # Shell principal
│       │   ├── routes/                    # Pantallas especiales
│       │   ├── viewmodel/
│       │   ���   ├── toast.store.ts
│       │   │   └── log.store.ts
│       │   └── util/
│       │       ├── console.interceptor.ts
│       │       └── logger.service.ts
│       ├── data/                          # Appwrite, Pusher clients
│       └── di/                            # Inyección de dependencias
└── lib/                                   # M3 Svelte components + navStack
```

## Reglas Prácticas para Cambios

1. **Prioridad 1**: Corregir imports rotos (errores Vite). Busca rutas relativas exactas.
   - ✅ `../../../feature/sale/presentation/viewmodel/sale-alert.store`
   - ❌ `../../../sale/presentation/viewmodel/sale-alert.store` (falta `feature/`)

2. **Prioridad 2**: Respetar boundaries feature↔infrastructure. Features importan infra, no viceversa.

3. **Prioridad 3**: Mantener stores tipadas con métodos explícitos (p.ej. `saleAlertStore.addAlert`).

## Patrones Clave Específicos

### Stores (Svelte writable/derived)
```typescript
// sale-alert.store.ts
export interface SaleVerificationAlert {
    saleId: string;
    decision: 'confirmed' | 'rejected';
    timestamp: string;
    amount?: number;
    productCount?: number;
}

export function createSaleAlertStore() {
    const { subscribe, update } = writable(initialState);
    return {
        subscribe,
        addAlert: (alert) => update(...),
        removeAlert: (saleId) => update(...),
        clearAlerts: () => update(...)
    };
}

// Uso: import { saleAlertStore } from '...';
// saleAlertStore.addAlert({ saleId, decision, timestamp });
// let alerts = $saleAlertStore.alerts;  // reactive
```

### Notificaciones (Toast + Browser + Alerts)
- `toastStore`: mensajes breves UI
- `Notification` API: alertas nativas del navegador (con guard `typeof window`)
- `saleAlertStore`: persistencia en memoria (NO localStorage - Pusher tiene persistencia propia)

### Flujo Verificación de Ventas (Realtime)
1. Usuario abre reserva → emite `sale-verification-open` (CustomEvent)
2. NestedNavigationWrapper captura → navega a reservationDetail
3. Al confirmar/rechazar → Pusher publica `sale.verified` / `sale.rejected`
4. Cliente recibe → actualiza `saleStore` + `saleAlertStore`
5. SaleVerificationAlert.svelte renderiza confirmación + Toast + Browser Notification

### Responsividad Layouts
- **Desktop (>840px)**: Sidebar 320px + contenido
- **Mobile (≤840px)**: FAB flotante + menú desplegable
- **Screen.svelte**: Aplica `padding-bottom: calc(88px + env(safe-area-inset-bottom))`
- **ProductScreen.svelte**: `.products-grid` overflow-y auto con scroll suave

## Problemas Conocidos & Soluciones

| Problema | Ubicación | Solución |
|----------|-----------|----------|
| Import inválido `../../../sale/...` | SaleVerificationAlert.svelte | → `../../../feature/sale/presentation/viewmodel/sale-alert.store` |
| Contenido oculto lado derecho mobile | ProductScreen.svelte | Revisar `.products-grid` width + padding contenedor |
| FAB no flotante | NestedNavigationWrapper | FAB = `position: fixed` con z-index 60+ |
| Espacio innecesario user vs search | ProductScreen.svelte:mobile | Reducir gap `.top-row` para ≤480px |
| `console[level] = Function` error | console.interceptor.ts | Cast correcto: `const original = console[level] as Function` |
| Email undefined authFlow | auth-flow.store.ts | Hacer opcional o validar antes `setSuccess` |
| Input event sin type cast | InternalProfileScreen.svelte | Usar `event.currentTarget as HTMLInputElement` |

## Convenciones de Código

- **Tipos**: `type` para interfaces públicas, `interface` para herencias
- **Guards**: `if (typeof window !== 'undefined')` antes de browser APIs
- **Async**: `try/catch` en Pusher/Appwrite
- **CSS**: Variables `--md-sys-color-*` (Material Design 3)
- **Imports**: Rutas absolutas feature/infra, relativas solo dentro feature

## Comandos Útiles (PowerShell)

```powershell
pnpm install && pnpm dev       # Dev server
pnpm tsc --noEmit              # Typecheck solo
pnpm build                      # Build producción
```

## Commits por Agente

- Pequeños y atómicos: `fix(store):`, `chore(ui):`, `feat(notification):`
- Tras imports/tipos → `pnpm tsc --noEmit`
- Si navegación → verifica deeplinks en `browser.history.replaceState`

Leer antes de cambiar: `src/core/feature/**/README.md` y `src/core/infrastructure/README.md` cuando existan.
