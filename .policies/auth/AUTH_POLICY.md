# Política de autenticación (Auth)

Documento de validación del core `auth` (web + Android).  
**alejotallerscan (operador): no admite visitantes** — solo usuarios autenticados de operación.

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
| **visitor** | Sin sesión, sesión anónima, email vacío, provider guest | Solo catálogo / detalle de producto. Overlay / bloqueo en rutas protegidas |

## 3. Flujo de arranque (Splash)

1. Capturar deeplink de producto si existe.
2. `getCurrentUser()`:
   - **Éxito + perfil claro** → authenticated → home.
   - **Éxito + perfil no claro / anónimo** → visitor → home.
   - **Fallo (sin sesión)**:
     - Deeplink producto → crear sesión anónima → visitor → contenido.
     - Visitante recurrente (`alejo_has_visited`) → auto guest → home (productos).
     - Primera visita → Welcome / Landing.
3. Admin solo se ofrece si el perfil es **authenticated** y el rol es admin-like (web).

## 4. Flags locales

| Plataforma | Clave | Uso |
|------------|-------|-----|
| Web | `alejo_has_visited` | Completó Welcome |
| Web | `talleralejo.session.isGuest` | Flag local visitante |
| Android | SharedPreferences `alejo_has_visited` | Completó Landing |
| Android | `MainRoutesKey.MainHome.isGuest` | Modo sesión en nav |

## 5. Restricciones de visitante

**Web**
- Navegación visible: solo Productos.
- Rutas protegidas: `GuestAuthOverlay`.
- No sync de ventas privadas.

**Android (cliente `app`)**
- Misma política de clasificación (`ProfileClassification` / `ResolveStartupSessionCaseUse`).
- `isGuest = true` en `MainHome`.
- FAB / destinos protegidos limitados a catálogo (productos + detalle).
- No sync de reservas si es visitante.

**Operador (`alejotallerscan`)**
- Sin modo visitante. Login obligatorio.

## 6. Implementación de referencia

### Web
- `web/src/core/feature/auth/presentation/util/profile-classification.ts`
- `web/src/core/feature/auth/presentation/util/gest-session.ts`
- `web/src/core/feature/auth/presentation/routes/Splash.svelte`
- `web/src/core/infrastructure/presentation/navigation/first-visit.ts`

### Android
- `app/.../auth/domain/util/ProfileClassification.kt`
- `app/.../auth/domain/caseuse/ResolveStartupSessionCaseUse.kt`
- `app/.../auth/data/FirstVisitPreferences.kt`
- `SessionManager.openAnonymousSession()`

## 7. Tests

| Suite | Qué valida |
|-------|------------|
| `web/.../profile-classification.test.ts` | Clasificación AUTH_POLICY |
| `web/.../first-visit.test.ts` | Flag onboarding |
| `app/.../ProfileClassificationTest.kt` | Paridad Android |
| `app/.../ResolveStartupSessionCaseUseTest.kt` | Arranque Splash |

## 8. Checklist de validación

- [ ] Primera visita sin sesión → Welcome/Landing
- [ ] Tras explorar como visitante → home Visitante, solo productos
- [ ] Recarga con sesión anónima → sigue visitante
- [ ] Login con email → authenticated, navegación completa
- [ ] Deeplink producto sin sesión → visitor + detalle, sin Welcome
- [ ] Operador no tiene camino guest
