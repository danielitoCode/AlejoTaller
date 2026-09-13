# Migración de plataforma — por funcionalidad

**Repo:** `AlejoTaller` (web B2C + Android operador/cliente)  
**Actualizado:** 2026-09-13  
**Rama:** `Core6`

## Principio

```text
Misma política de aplicación · mismos case uses · misma UX
Solo cambian adapters / plataformas
```

No se reescriben reglas de soft-hold, checkout, confirm operador, finance snapshot (Core 4), ni fronteras MCP.  
Cada fase cierra con **smoke** alineado a `.policies/` y checklists ya cerrados.

Canónico panel: [dash PLATFORM_MIGRATION](https://github.com/danielitoCode/dash_alejo_taller/blob/Core6/.roadmap/PLATFORM_MIGRATION.md).

### Stack destino

| Funcionalidad | Plataforma |
|---------------|------------|
| Autenticación | **Auth0** |
| Database | **Turso** |
| Files | **Cloudflare R2** |
| Realtime | **Pusher** |

### Orden (igual que dash)

```text
1. Auth  →  2. Database  →  3. Files  →  4. Realtime (alinear)  →  5. Cortar Appwrite
```

---

## Puertos

```text
Web / Android + Case uses
    → AuthPort | *Repository | FileStoragePort | RealtimePort
         → Auth0 | Turso | R2 | Pusher
```

Dominio y UI **no** importan SDKs de plataforma.

---

## Fase 1 — Autenticación (Auth0)

**Objetivo:** sesión B2C y staff operador sin Appwrite Account.  
**Invariantes:** invitado vs logueado; operador staff; permisos de pedido; sin ampliar roles mágicamente.

### Implementación

- [ ] Apps Auth0: SPA web + nativo Android (+ paridad con panel)
- [ ] `Auth0AuthAdapter` / sesión shared
- [ ] `getSubject()` = `sub`; perfil local si hace falta
- [ ] Guest: política actual preservada (guest allowlist de rutas)
- [ ] Operador: mismo criterio de staff que dash

### Smoke

- [ ] Web: login / logout / sesión persistida
- [ ] Guest: catálogo según política actual
- [ ] Pedido autenticado exige sesión como hoy
- [ ] Operador: login staff OK; no-staff no opera confirm

**DoD:** auth 100% Auth0; data aún puede ser Appwrite.

---

## Fase 2 — Database (Turso)

**Objetivo:** lecturas/escrituras de negocio en Turso; case uses iguales.  
**Invariantes:** soft-hold atómico (Core 1); checkout no bloquea por Telegram; confirm operador → `salida_venta` + finance; DELETED sin finance; cliente no escribe finance.

### Implementación

- [ ] Schema compatible con dash
- [ ] Repos web + operador → Turso
- [ ] Migración datos + validación de conteos
- [ ] `user` / owner keys = Auth0 `sub` donde aplique

### Smoke

- [ ] Catálogo y stock visible (available = existence − reserved)
- [ ] Checkout crea Sale UNVERIFIED + soft-hold
- [ ] Operador confirm → VERIFIED + side effects Core 4
- [ ] Operador reject → DELETED + release según política
- [ ] Reintento confirm no duplica finance (idempotencia)

**DoD:** sin DB Appwrite en runtime de negocio.

---

## Fase 3 — Files (R2)

**Objetivo:** imágenes por URL pública; UI solo usa key + `CDN_BASE`.  
**Invariantes:** mismas fotos de producto en listado/detalle.

### Implementación

- [ ] Lectura catálogo desde R2/CDN
- [ ] Upload (si existe en AT) vía `FileStoragePort`
- [ ] Migración de blobs de prueba / producción acotada

### Smoke

- [ ] Web: imágenes cargan por HTTPS público
- [ ] Android: igual en listado/detalle
- [ ] Sin dependencia de fileId Appwrite en UI

**DoD:** storage Appwrite fuera del path de medios.

---

## Fase 4 — Realtime (alinear Pusher)

**Objetivo:** eventos tras persistencia Turso; mismos nombres de canal/evento que consumen web y scan.  
**Invariantes:** RT no es autoridad; sync recupera; publisher no bloquea el resultado de negocio si falla (aviso, no rollback de venta ya confirmada — política actual de resiliencia).

### Implementación

- [ ] Publish desde paths que escriben (operador confirm/reject, etc.)
- [ ] Web/scan subscribe: handlers idempotentes
- [ ] Quitar listeners/realtime Appwrite

### Smoke

- [ ] Confirm operador → web ve estado actualizado (o tras sync)
- [ ] Stock invalidation / refresh como hoy
- [ ] Soporte (si aplica) sigue en canales acordados
- [ ] Sin Pusher: refresh manual deja datos correctos

**DoD:** un solo bus de señales (Pusher) ligado a writes Turso.

---

## Fase 5 — Cortar Appwrite

- [ ] Sin SDK Appwrite en web/android prod
- [ ] Env: `AUTH0_*`, `TURSO_*`, `CDN_BASE`/`R2_*`, `PUSHER_*`
- [ ] CI verde
- [ ] Smoke E2E corto cruzado con panel si es posible

---

## Superficies

| Superficie | Fase 1 | Fase 2 | Fase 3 | Fase 4 |
|------------|--------|--------|--------|--------|
| Web B2C | sesión | pedidos/catálogo | imágenes | listen |
| Operador | staff | confirm/stock | — | publish + listen |
| MCP | JWT | tools acotadas | — | no relaja permisos |

---

## Core 6 (AT)

Frontera `Appointment ≠ Sale`. Request B2C opcional solo con Auth0 + Turso, sin soft-hold de producto.  
Agenda: dash.

---

## Aceptación global

- [ ] Políticas de venta/almacén/finance **sin** cambios de reglas
- [ ] Solo adapters intercambiados
- [ ] Cada fase tiene smoke marcado antes de la siguiente
