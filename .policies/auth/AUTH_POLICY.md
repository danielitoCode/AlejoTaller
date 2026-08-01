# Política de autenticación (Auth)

Documento de validación del core `auth` (web + alineación Android).
Última actualización: 2026-08-01

## 1. Principio general

> **Si el perfil no es claro, el modo de sesión es visitante (guest).**

Un perfil “claro” (autenticado) requiere identidad verificable:
- `id` / `$id` no vacío
- `email` no vacío
- no es proveedor guest/anonymous/visitor/invitado
- no es usuario anónimo de Appwrite (email vacío)

Cualquier otro caso → **visitante**.

## 2. Modos de sesión

| Modo | Condición | Capacidades |
|------|-----------|-------------|
| **authenticated** | Perfil claro (email + id) | Catálogo, carrito, reservas, perfil, ajustes, sync de ventas/promos |
| **visitor** | Sin sesión, sesión anónima, email vacío, provider guest | Solo catálogo / detalle de producto. Overlay al tocar rutas protegidas |

## 3. Flujo de arranque (Splash)

1. Capturar deeplink si existe (nunca se pierde).
2. `getCurrentUser()`:
   - **Éxito + perfil claro** → `setAuthenticatedSession()` → home.
   - **Éxito + perfil no claro / anónimo** → `setGuestSession()` → home como visitante.
   - **Fallo (sin sesión)**:
     - Deeplink → crear sesión anónima → home (contenido del deeplink).
     - Visitante recurrente (`alejo_has_visited`) → auto guest → home (productos).
     - Primera visita → Welcome.
3. Admin solo se ofrece si el perfil es **authenticated** y el rol es admin-like.

## 4. Flags locales

| Clave | Uso |
|-------|-----|
| `alejo_has_visited` | Completó Welcome / ya entró a la app |
| `talleralejo.session.isGuest` | Flag local de visitante (sessionStore) |

El flag `isGuest` **debe** sincronizarse con la clasificación de perfil en cada arranque. No confiar solo en localStorage si Appwrite devuelve un usuario anónimo.

## 5. Restricciones de visitante (web)

- Navegación visible: solo **Productos**.
- Rutas protegidas (`buy`, `reservations`, `profile`, `settings`): mostrar `GuestAuthOverlay`.
- No sincronizar ventas ni promociones privadas.
- Deeplink a producto: permitido.

## 6. Implementación de referencia

- Clasificación: `web/src/core/feature/auth/presentation/util/profile-classification.ts`
- Detección anónimo: `web/src/core/feature/auth/presentation/util/gest-session.ts`
- Splash: `web/src/core/feature/auth/presentation/routes/Splash.svelte`
- Shell: `web/src/core/infrastructure/presentation/navigation/NestedNavigationWrapper.svelte`

## 7. Checklist de validación

- [ ] Primera visita sin sesión → Welcome
- [ ] Tras “Explorar como visitante” → home con badge Visitante y solo Productos
- [ ] Recarga con sesión anónima Appwrite → sigue siendo Visitante (no “Usuario” sin email)
- [ ] Login con email → authenticated, navegación completa
- [ ] Deeplink producto sin sesión → guest + detalle producto, sin Welcome
- [ ] Logout / salir visitante limpia flags de sesión
