# Core 2 — Plan de implementación por fases (checklist canónico)

**Fuente de verdad del plan:** `dash_alejo_taller/.roadmap/Core2/CORE2_IMPLEMENTATION_PLAN.md`  
**Este archivo:** espejo **adaptado a AlejoTaller** (web cliente + `alejotallerscan` + schema compartido).  
**Última actualización:** 2026-08-16  
**Estado del plan:** publicado · ejecución ~5% (preparación soft-hold verificada) · finanzas incluidas  
**Repos:** AlejoTaller (schema / operador / web cliente) + dash (panel entradas, reportes, reservas)

Marca `[x]` al completar **en este repo** (o evidencia de schema).  
No avanzar fases críticas de stock sin regresión verde del soft-hold Core 1.

**Fórmula canónica (congelada):** `available = max(0, existence − reserved)`

**COGS (congelado):** `last_unit_cost × qty` al VERIFIED — **no** promedio.

---

## Competencias (qué marca este repo)

| Fase | AlejoTaller (este repo) | dash_alejo_taller |
|------|-------------------------|-------------------|
| 2.0 Políticas | Espejo + aceptar deltas | Origen del plan |
| 2.1 Schema Appwrite | Crear/validar collections; DTO/repo operador y web si aplica | Contrato + consumo |
| 2.2 Confirm + traza | **Primario:** operador VERIFIED → `salida_venta` + finance event | Secundario si confirma desde panel |
| 2.3 Factura entrada UI | Opcional atajo operador; no es el panel completo | **Primario** |
| 2.4 Reportes / cola | — | **Primario** |
| 2.5 Reservas taller | Cliente solicitar (si MVP web); schema | **Gobernar** panel |
| 2.6 Seguridad / CI | CI web + permisos writes producto/sale/movements | CI dash + roles panel |

Detalle de política: [`POLICY_DELTAS_CORE2.md`](./POLICY_DELTAS_CORE2.md) · Finanzas: [`FINANCE_MODEL_CORE2.md`](./FINANCE_MODEL_CORE2.md)

---

## Registro de avance

```text
Inicio espejo AlejoTaller: 2026-08-16
Fase actual: 2.0 (espejo) → siguiente 2.1 schema
Core 2 cerrado: NO
```

| Fecha | Ítem | Nota |
|-------|------|------|
| 2026-08-16 | Espejo plan + policy + finance | Alineado a dash |
| 2026-08-16 | Soft-hold Core 1 | 20 unit tests web en verde; smoke live Appwrite pendiente |

---

## Fase 2.0 — Alcance y políticas delta

**Objetivo:** congelar alcance MVP y deltas de política antes de schema.

- [x] Plan por fases publicado en dash
- [x] Espejo de este plan en `AlejoTaller/.roadmap/Core2/CORE2_IMPLEMENTATION_PLAN.md`
- [x] Espejo [`POLICY_DELTAS_CORE2.md`](./POLICY_DELTAS_CORE2.md)
- [x] Espejo [`FINANCE_MODEL_CORE2.md`](./FINANCE_MODEL_CORE2.md)
- [x] Modelo financiero aceptado (factura de entrada + ingreso/COGS al VERIFIED) — 2026-08-13
- [x] Reservas de taller **dentro** del MVP Core 2 — **SÍ**
- [x] COGS = **último costo** (`last_unit_cost`) — no promedio
- [x] Revisar y **aceptar** POLICY_DELTAS en este repo (firma / PR review del equipo)
- [x] Soft-hold Core 1 operativo en código + tests unitarios web (evidencia 2026-08-16)
- [x] Smoke live Appwrite: UNVERIFIED → reserved↑; VERIFIED → consume (manual)

**Criterio de salida 2.0:** alcance firmado + políticas delta aceptadas en ambos repos.

---

## Fase 2.1 — Schema stock + finanzas (Appwrite · este repo)

**Objetivo:** collections listas para operador y dash.

### Movimientos de stock

- [ ] Collection **`stock_movements`** creada (ID canónico: `stock_movements`; mismo database que Core 1)
- [ ] Campos: `product_id`, `type`, `quantity`, `balance_after`, `reason`, `user_id`, `sale_id?`, `created_at`
- [ ] Enum `type`: `entrada` | `salida_venta` | `ajuste` | `devolucion`
- [ ] Permisos: lectura/escritura staff/operador; **no** cliente
- [ ] Índices: `product_id`, `type`, `sale_id`, `$createdAt`
- [ ] Actualizar [`APPWRITE_STOCK_MOVEMENTS.md`](./APPWRITE_STOCK_MOVEMENTS.md) con IDs reales del proyecto

### Finanzas de entrada

- [ ] Collection `supplier` (name, contact?, notes?)
- [ ] Collection `purchase_entry` (supplier_id?, reference?, entry_date, total_cost, currency, user_id, notes?)
- [ ] Collection `purchase_entry_line` (entry_id, product_id, quantity, unit_cost, concept, line_cost)
- [ ] Enum concepto línea: `purchase` | `royalty` | `other`

### Finanzas de venta (al confirmar)

- [ ] Collection `sale_finance_event` **o** campos acordados (`sale_id`, `revenue`, `cogs`, `margin`, `user_id`, `at`)
- [ ] Campo producto **`last_unit_cost`** (base COGS)
- [ ] DTO + repo / contrato en operador (y web solo lectura si aplica)
- [ ] **Test:** mapper/DTO round-trip de movement + finance event

**Criterio de salida 2.1:** movimiento de prueba + cabecera/línea de entrada de prueba vía API con totales coherentes.

---

## Fase 2.2 — Operador: traza stock + finanzas al confirmar (**primario AlejoTaller**)

**Objetivo:** VERIFIED = soft-hold Core 1 + `salida_venta` + ingreso/COGS.

Superficie: `alejotallerscan` (`ApplyOperatorStockDecisionCaseUse` + repos Appwrite).

- [ ] En flujo VERIFIED: escribir `stock_movements` tipo `salida_venta` por línea (o agregado documentado)
- [ ] `balance_after` = `existence` tras el consume
- [ ] `sale_id` + `user_id` (operador) rellenados
- [ ] Evento financiero: `revenue`, `cogs` (= **last_unit_cost × qty**), `margin`
- [ ] UNVERIFIED / reserved: **no** crea evento financiero
- [ ] Idempotencia: segundo confirm no duplica movement, finance ni stock
- [ ] Reject/DELETED: sin `salida_venta` ni ingreso (solo release `reserved`)
- [ ] **Test:** unitario case-use operador — confirm → create movement + finance; segundo confirm no duplica
- [ ] **Test:** regresión soft-hold web (`RegisterNewSale` / `CheckAProductExistence` / release atomic) en verde
- [ ] Smoke: confirm en dispositivo/emulador → documentos visibles en Appwrite

**Criterio de salida 2.2:** confirm deja traza de qty y dinero; soft-hold sin regresión.

---

## Fase 2.3 — Panel factura / movimientos / ajuste

**Primario: dash.** En este repo solo:

- [ ] (Opcional) Atajo operador “entrada rápida” coherente con movement + costo, **o** documentar “solo desde dash”
- [ ] Si se expone entrada/ajuste en operador: mismos invariantes (`existence >= reserved`, movement obligatorio)
- [ ] **Test** de invariante post-ajuste si hay código aquí

UI factura multi-línea, listados y roles panel → checklist en **dash**.

**Criterio de salida 2.3 (ecosistema):** entrada multi-producto con costos; stock coherente; QA Core 1 stock PASS.

---

## Fase 2.4 — Reportes y cola UNVERIFIED

**Primario: dash.** En este repo:

- [ ] Garantizar que datos emitidos en 2.2 (movements + finance events) son consultables (permisos/índices)
- [ ] (Web cliente) no mostrar ingresos; solo `available` de catálogo — sin cambio de policy

---

## Fase 2.5 — Reservas de taller (MVP)

**Schema compartido; panel en dash; cliente web opcional.**

- [ ] Collection `appointment` / `booking` (nombre acordado) en Appwrite
- [ ] Estados: solicitada → confirmada → realizada | cancelada
- [ ] **No** mezclar con `Sale` de tienda en UI de reservas
- [ ] (Web) si se expone solicitud de cita: solo create + list mine — **Test** case-use
- [ ] (Fuera de MVP) descuento de piezas por cita

**Criterio de salida 2.5:** agenda usable en panel sin contaminar Ventas.

---

## Fase 2.6 — Seguridad, CI y cierre DoD (este repo)

### Seguridad

- [ ] Auditoría permisos Appwrite: cliente no escribe `stock_movements` / finance / purchase_*
- [ ] Secrets solo en env / CI (no en git)
- [ ] Operador no usa RMW inseguro donde ya hay path atómico (reserved)

### CI

- [ ] Workflows web: `npm ci` / test / build en verde en PR
- [ ] (Ideal) job operador compile + unit tests al tocar `alejotallerscan/`

### Cierre

- [ ] `MVP_CORE2_STATUS.md` (o este plan) → Core 2 cerrado + fecha **cuando** dash también cierre
- [ ] Smoke cruzado: entrada (dash) → pedido (web) → confirm (operador) → `salida_venta` + finance
- [ ] Regresión soft-hold + support web PASS

**DoD Core 2 (ecosistema, borrador):**

1. `stock_movements` registra al menos `entrada` y `salida_venta`.  
2. Factura de entrada multi-línea en dash; sin `existence < reserved`.  
3. VERIFIED reconoce ingreso/COGS/margen; UNVERIFIED no mueve dinero.  
4. Soft-hold Core 1 sin regresión.  
5. Reservas de taller separadas de ventas B2C.  
6. STATUS marca Core 2 cerrado en ambos repos.

---

## Fuera de este núcleo (Core 3+)

- Contabilidad formal / multi-almacén  
- Piezas de reparación descontando stock por cita  
- Function Appwrite atómica confirm+stock+movement+finance (puede adelantarse)  
- E2E automatizado completo tienda ↔ dash ↔ operador  
- Paridad Android support (sprint paralelo, no bloquea Core 2 stock)

---

## Orden de trabajo (AlejoTaller)

```text
2.0  aceptar policy espejo          (casi listo)
2.1  schema Appwrite + DTO/tests
2.2  operador salida_venta + finance + tests   ← valor principal aquí
2.3–2.5  coordinar con dash (UI panel)
2.6  CI + permisos + smoke cruzado
```

**Checklist anterior** `CHECKLIST_CORE2.md` queda **deprecado**; usar **este archivo** para marcar avance.
