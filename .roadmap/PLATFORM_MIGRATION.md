# Migración de plataforma — stack desacoplado

**Repo:** `AlejoTaller` (web cliente + Android operador/cliente)  
**Actualizado:** 2026-09-13  
**Rama de trabajo sugerida:** `Core6` (o `infra/platform-migration`)

## Objetivo

Desacoplar capacidades que hoy concentramos en Appwrite, para migrar **por capa** cuando un free tier caduque o un vendor falle.

| Capa | Destino | Notas AT |
|------|---------|----------|
| Auth | **Auth0** | Mismo tenant/apps que el panel; `sub` estable |
| Files | **Cloudflare R2** | Imágenes catálogo por URL pública |
| DB | **Turso** | Lectura/escritura según superficie (web B2C / scan operador) |
| Realtime | **Pusher** | Señal; no fuente de verdad |

**Turso no emite realtime usable en free hacia el cliente.** Tras write → Pusher; si se pierde el evento → sync desde Turso.

Canónico panel: [dash PLATFORM_MIGRATION](https://github.com/danielitoCode/dash_alejo_taller/blob/Core6/.roadmap/PLATFORM_MIGRATION.md).

---

## Puertos (dominio y UI estables)

```text
UI (Svelte web / Android) + Case uses
        │
        ▼
   Ports
        ├── AuthPort
        ├── SaleRepository / ProductRepository / …
        ├── FileStoragePort
        └── RealtimePort (subscribe en client; publish en quien escribe)
                │
                ▼
        Adapters
                ├── Auth0AuthAdapter
                ├── Turso*Repository
                ├── R2FileStorageAdapter
                └── PusherRealtimeAdapter
```

### Contratos mínimos

**`AuthPort`**

- Sesión + token para APIs
- `getSubject(): string` (`sub` Auth0) — **nunca** mezclar con ids internos de Appwrite a largo plazo
- Web + Android comparten el mismo issuer / audience acordado

**`FileStoragePort`**

- Upload solo si la superficie escribe medios (operador/panel; B2C suele solo leer)
- En modelo de producto: `image_key`; UI resuelve `CDN_BASE + key`

**`RealtimePort` / client Pusher**

- Canales ya usados (ventas, stock, soporte…): mismos nombres de evento si es posible, payload estable
- Tras migración DB: el **publisher** es quien persistió en Turso (panel u operador), no Appwrite hooks

**Repositorios**

- Dominio sin tipos Appwrite
- Web y `shared-*` / scan: una implementación Turso por plataforma o shared Kotlin/TS según módulo

---

## Orden de migración (igual que dash)

```text
1. Auth0  →  2. R2  →  3. Turso  →  4. Cortar Appwrite
```

### Fase 1 — Auth0

- [ ] Aplicaciones Auth0: SPA web, nativo Android (y panel en dash)
- [ ] Adapter auth en web + session/token en scan
- [ ] Usuario invitado / B2C: flujos Auth0 (o guest explícito sin Appwrite Anonymous si se redefine)
- [ ] Operador: login staff con mismos roles que panel (claims o tabla local por `sub`)
- [ ] Smoke: login web + operador; pedido autenticado

### Fase 2 — R2

- [ ] Lectura de imágenes de catálogo desde `CDN_BASE` (R2)
- [ ] Cualquier upload desde AT (si existe) vía `FileStoragePort`
- [ ] No depender de file IDs Appwrite en UI
- [ ] Smoke: catálogo muestra fotos públicas

### Fase 3 — Turso

- [ ] Schema compartido / compatible con dash (mismas tablas de sales, products, stock…)
- [ ] Web: repos offline-first apuntando a Turso (o API propia que hable Turso)
- [ ] Operador: confirm/reject + finance snapshot siguen case uses; persistencia Turso
- [ ] Tras mutación OK → evento Pusher (paridad con hoy)
- [ ] Migración datos + smoke checkout, soft-hold, confirm operador

### Fase 4 — Cortar Appwrite

- [ ] Sin SDK Appwrite en web/android de producción
- [ ] CI sin dependencia runtime Appwrite
- [ ] Documentar env: `AUTH0_*`, `TURSO_*`, `R2_*` / `CDN_BASE`, `PUSHER_*`

---

## Superficies AT — responsabilidad

| Superficie | Auth0 | Turso | R2 | Pusher |
|------------|-------|-------|-----|--------|
| **Web B2C** | login/sesión | pedidos, catálogo | leer imágenes | sale/stock/support |
| **Operador (scan)** | staff | confirm/reject, stock | poco/nada | publish + listen |
| **MCP / agente** | JWT usuario | tools de lectura/escritura acotadas | — | no sustituye permisos |

Cliente **no** escribe `sale_finance_event` salvo flujos operador ya definidos (Core 4).

---

## Core 6 en AT

- Frontera: `Appointment ≠ Sale` ([Core6/AT_IMPLEMENTATION_CHECKLIST.md](./Core6/AT_IMPLEMENTATION_CHECKLIST.md))
- Si hay “pedir cita” B2C: repo Turso + Auth0 `sub`, **sin** soft-hold de producto ni finance
- Agenda staff: solo dash

---

## Checklist rápido de aceptación

- [ ] Cambiar solo el adapter de auth no rompe case uses de venta
- [ ] Cambiar `CDN_BASE` no exige migrar filas de producto (solo keys)
- [ ] Matar Pusher degrada a “hay que refrescar”, no corrompe datos
- [ ] Matar Appwrite no impide login ni listar productos tras fases 1–3
