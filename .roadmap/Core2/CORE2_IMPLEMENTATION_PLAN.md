# Core 2 — Plan de implementación por fases (checklist canónico AlejoTaller)

**Checklist operativo unificado (cliente + dash + operador):**  
→ [`CORE2_UNIFIED_CHECKLIST.md`](./CORE2_UNIFIED_CHECKLIST.md)

**Rama de trabajo:** `Core2` (no trabajar Core 2 sobre `master` salvo merge recomendado).  
**Última actualización:** 2026-08-18  
**Estado:** schema Appwrite **listo** · código runtime Core 2 formal **pendiente B1–B2** · finanzas incluidas  
**Repos:** AlejoTaller (schema / operador / web cliente) + dash (panel entradas, reportes, reservas)

Marca `[x]` al completar **en este repo** y sincroniza el checklist unificado.

**Fórmula canónica (congelada):** `available = max(0, existence − reserved)`  
**COGS (congelado):** `last_unit_cost × qty` al VERIFIED — **no** promedio.

---

## Competencias (qué marca este repo)

| Fase | AlejoTaller (este repo) | dash_alejo_taller |
|------|-------------------------|-------------------|
| 2.0 Políticas | Espejo + aceptar deltas | Origen del plan |
| 2.1 Schema Appwrite | Collections en cloud ✓; DTO/repo código = B1 | Contrato + consumo |
| 2.2 Confirm + traza | **Primario:** operador VERIFIED → `salida_venta` + finance | Secundario si confirma desde panel |
| 2.3 Factura entrada UI | Opcional atajo operador | **Primario** |
| 2.4 Reportes / cola | — | **Primario** |
| 2.5 Reservas taller | Cliente solicitar (opcional); schema | **Gobernar** panel |
| 2.6 Seguridad / CI | CI web + permisos | CI dash + roles panel |

---

## Registro de avance

```text
Rama: Core2
Fase actual: B1 contrato DTO/repo (tras schema cloud ✓)
Core 2 cerrado: NO
```

| Fecha | Ítem | Nota |
|-------|------|------|
| 2026-08-16 | Espejo plan + policy + finance | Alineado a dash |
| 2026-08-16 | Soft-hold Core 1 | tests web en verde |
| 2026-08-18 | Schema Appwrite | stock_movements, purchase_*, sale_finance_event, permisos — **hecho** |
| 2026-08-18 | Checklist unificado + rama Core2 | Trabajo organizado sin tocar master Core 1 |

---

## Fase 2.0 — Alcance y políticas delta

- [x] Plan por fases publicado en dash
- [x] Espejo de este plan en `AlejoTaller/.roadmap/Core2/`
- [x] Espejo POLICY_DELTAS + FINANCE_MODEL
- [x] Modelo financiero aceptado — 2026-08-13
- [x] Reservas de taller **dentro** del MVP Core 2 — **SÍ**
- [x] COGS = **último costo** (`last_unit_cost`)
- [x] Soft-hold Core 1 operativo en código + tests unitarios web
- [x] Smoke live Appwrite soft-hold (manual equipo)

**Criterio de salida 2.0:** ✓

---

## Fase 2.1 — Schema stock + finanzas (Appwrite)

### Movimientos de stock

- [x] Collection **`stock_movements`** creada (mismo database Core 1)
- [x] Campos: `product_id`, `type`, `quantity`, `balance_after`, `reason`, `user_id`, `sale_id?`, `entry_id?`, …
- [ ] Enum `type` **en código**: `entrada` | `salida_venta` | `ajuste` | `devolucion`
- [x] Permisos: lectura/escritura staff/operador; **no** cliente
- [x] Índices / atributos en consola (equipo)
- [x] [`APPWRITE_STOCK_MOVEMENTS.md`](./APPWRITE_STOCK_MOVEMENTS.md) actualizado (colección creada)

### Finanzas de entrada

- [x] Collection `supplier`
- [x] Collection `purchase_entry`
- [x] Collection `purchase_entry_line`
- [ ] Enum concepto línea **en código**: `purchase` | `royalty` | `other`

### Finanzas de venta (al confirmar)

- [x] Collection `sale_finance_event`
- [x] Campo producto **`last_unit_cost`**
- [ ] DTO + repo en operador (y dash lectura)
- [ ] **Test:** mapper/DTO round-trip movement + finance event

**Criterio de salida 2.1 schema cloud:** ✓ collections + permisos  
**Criterio de salida 2.1 código (B1):** pendiente DTO/repo + tests

---

## Fase 2.2 — Operador: traza stock + finanzas al confirmar

**Estado código hoy:** `ApplyOperatorStockDecisionCaseUse` muta existence/reserved; **no** escribe movement ni finance.

- [ ] VERIFIED → `stock_movements` `salida_venta`
- [ ] `balance_after`, `sale_id`, `user_id`
- [ ] Evento financiero revenue/cogs/margin
- [ ] UNVERIFIED sin finance; DELETED sin salida_venta
- [ ] Idempotencia 2º confirm
- [ ] Tests unitarios + smoke Appwrite (tú)

---

## Fase 2.3 — Panel factura (primario dash)

En este repo solo opcional atajo operador.

- [ ] Documentar «entrada solo desde dash» **o** atajo operador con mismos invariantes

---

## Fase 2.4 — Reportes (primario dash)

- [ ] Datos 2.2 consultables (permisos/índices)
- [x] Web cliente no muestra ingresos (solo available) — policy vigente

---

## Fase 2.5 — Reservas de taller

- [ ] Collection appointment/booking
- [ ] (Web opcional) create + list mine + test

---

## Fase 2.6 — Seguridad, CI y cierre

- [ ] Auditoría permisos cliente no escribe movements/finance/purchase
- [ ] CI web en PR `Core2`
- [ ] Smoke cruzado + STATUS cerrado

---

## Orden de trabajo

Ver **CORE2_UNIFIED_CHECKLIST.md** bloques B1 → B6.
