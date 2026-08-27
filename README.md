# TallerAlejo

<p align="center">
  <img src="https://img.shields.io/badge/Status-MVP%20Activo-0A7C66?style=for-the-badge" alt="MVP Activo" />
  <img src="https://img.shields.io/badge/Core%201-Cerrado-0A7C66?style=for-the-badge" alt="Core 1" />
  <img src="https://img.shields.io/badge/Core%202-Cerrado-0A7C66?style=for-the-badge" alt="Core 2" />
  <img src="https://img.shields.io/badge/Monorepo-Android%20%7C%20Web%20%7C%20Functions%20%7C%20MCP-1B3A57?style=for-the-badge" alt="Monorepo" />
  <img src="https://img.shields.io/badge/Architecture-Offline--First-FF9F1C?style=for-the-badge" alt="Offline First" />
  <img src="https://img.shields.io/badge/Backend-Appwrite-E53935?style=for-the-badge" alt="Appwrite" />
</p>

<p align="center">
  <strong>Suite operativa para ventas, reservas y flujo de confirmacion de Taller Alejo.</strong><br/>
  Persistencia local, sincronizacion remota (Appwrite) y eventos en tiempo real.
</p>

---

## Estado de nucleos

| Nucleo | Estado | Contenido clave |
|--------|--------|-----------------|
| **Core 1** | **Cerrado** (2026-08-12) | Soft-hold, ventas UNVERIFIED→VERIFIED/DELETED, coherencia cliente/operador/dash |
| **Core 2** | **Cerrado** (2026-08-24) | Traza `salida_venta` + finance al VERIFIED (operador); factura entrada, movements `entrada`, COGS y reservas taller en **dash** |
| **MCP Cliente** | **Fase 0 cerrada** (2026-08-24) | Superficie agente B2C; soft-hold en Worker pendiente Fase 1+ — [mcp/](./mcp/) |

Contrato de stock:

```text
available = max(0, existence − reserved)
UNVERIFIED → reserved += qty
VERIFIED   → existence -= qty, reserved -= qty (+ salida_venta + sale_finance_event)
DELETED    → reserved -= qty
```

COGS al VERIFIED: `last_unit_cost × qty` (no promedio).

### Core 2 — qué incrementó

- Operador: al confirmar venta escribe `salida_venta` + evento financiero
- Dash: factura de entrada multi-línea, listados de movimientos/facturas, cola UNVERIFIED + KPIs, reservas taller, permisos
- **No incluido:** UI de **ajuste de inventario** (futura implementación); devolución formal UI; reserva taller desde cliente web

Roadmap Core 2: [`.roadmap/Core2/`](./.roadmap/Core2/) · Checklist: [`.roadmap/Core2/CORE2_UNIFIED_CHECKLIST.md`](./.roadmap/Core2/CORE2_UNIFIED_CHECKLIST.md)  
Back-office: [dash_alejo_taller](https://github.com/danielitoCode/dash_alejo_taller)

---

## Vision

**TallerAlejo** concentra:

- Android cliente final
- Android operador (escaneo / confirmacion)
- Web cliente
- Function de publicacion realtime
- **MCP cliente** (agente IA sobre las mismas reglas B2C que `web/`)

Objetivos: compras y reservaciones consistentes, operacion con red inestable, Appwrite como fuente remota, realtime entre actores.

---

## Navegacion

- [Android Cliente](./app/README.md)
- [Android Operador](./alejotallerscan/README.md)
- [Web Cliente](./web/README.md)
- [MCP Cliente (agente B2C)](./mcp/README.md) → [Fase 0](./mcp/docs/PHASE0.md) · [Matriz tools](./mcp/docs/TOOL_MATRIX.md)
- [Function Publisher](./function/alejo_publisher/README.md)
- Web: [https://alejotaller.onrender.com/](https://alejotaller.onrender.com/)
- MCP health: [https://alejotaller-mcp.daniel-imbert96.workers.dev/health](https://alejotaller-mcp.daniel-imbert96.workers.dev/health)
- Dashboard: [https://github.com/danielitoCode/dash_alejo_taller](https://github.com/danielitoCode/dash_alejo_taller)

---

## Panorama del monorepo

```text
TallerAlejo/
|- app/                 -> Android cliente
|- alejotallerscan/     -> Android operador
|- web/                 -> Cliente web (dominio B2C de referencia para MCP)
|- mcp/                 -> MCP cliente B2C (Workers + Appwrite net)
|- function/alejo_publisher/
|- shared-auth/ | shared-core/ | shared-data/ | shared-sale/
```

---

## Aplicaciones

### `app` — Cliente Android
Catalogo, compra/reserva, offline, realtime de verificacion.

### `alejotallerscan` — Operador
Escaneo QR, confirm/reject, historial local, sync pendientes; al VERIFIED escribe **salida_venta** + **sale_finance_event** (Core 2).

### `web` — Cliente web
Compra/reserva, Dexie offline, realtime. **Fuente canónica de case uses B2C** para el MCP.

### `mcp` — MCP Cliente
Tools para agente de atención al cliente. Mismas reglas que web (soft-hold, ownership); sin staff/operador.  
URL: `https://alejotaller-mcp.daniel-imbert96.workers.dev`

### `function/alejo_publisher`
Publica eventos a Pusher sin secretos en el cliente.

---

## Arquitectura

Feature por capas (`data` / `domain` / `presentation`), offline-first con reconciliacion, repository + use cases, modulos `shared-*`.  
MCP: `tool → service → repository → Appwrite` (solo net).

---

## Stack

**Android:** Kotlin, Compose, Koin, Room, Appwrite SDK  
**Web:** Svelte, Vite, TypeScript, Dexie, Appwrite  
**MCP:** Cloudflare Workers, MCP SDK, node-appwrite  
**Infra:** Appwrite, Pusher, Render, GitHub Releases

---

## Roadmap por cores (resumen)

| Core | Foco | Estado |
|------|------|--------|
| MVP / Core 1 | Compra, reserva, verificacion, soft-hold | Cerrado |
| Core 2 | Traza stock/finance, factura (dash), COGS, reservas taller (dash) | Cerrado |
| MCP Cliente | Superficie agente B2C | Fase 0 ✅ · Fase 1 soft-hold en curso |
| Posterior | Ajuste inventario UI, reserva taller web, seguridad functions, analitica, E2E | Abierto |

Detalle: [`.roadmap/`](./.roadmap/) · MCP: [`mcp/docs/IMPLEMENTATION_ROADMAP.md`](./mcp/docs/IMPLEMENTATION_ROADMAP.md)

---

## Puesta en marcha

```bash
./gradlew assembleDebug
./gradlew :alejotallerscan:compileDebugKotlin
cd web && pnpm install && pnpm dev
cd mcp && npm install && npm run dev
```

---

## Criterio de calidad

Compilacion estable, dominio compartido, sync verificable, realtime alineado, soft-hold y Core 2 (salida_venta + finance) coherentes entre operador y dash. MCP debe respetar el mismo contrato de stock y ownership B2C.

---

## Licencia y uso

Repositorio privado del ecosistema **TallerAlejo**.
