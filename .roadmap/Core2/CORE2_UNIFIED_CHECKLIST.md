# Core 2 — Checklist unificado (cliente + backoffice + operador)

**Rama de trabajo:** `Core2` en **AlejoTaller** y **dash_alejo_taller**.  
**Merge a `master`:** incremental cuando un bloque esté verde (tests + política + smoke manual tuyo).  
**Regla de política:** no romper políticas de un core inferior salvo que un core superior las actualice de forma explícita y documentada.

**Última actualización:** 2026-08-21 (merge PRs + B3.1)  
**Core 2 cerrado:** NO

### Cómo usamos este archivo

1. Trabajamos **solo en `Core2`** (ambos repos).
2. Cada tarea cerrada → marcar `[x]` aquí **y** el ítem equivalente en `CORE2_IMPLEMENTATION_PLAN.md` de cada repo tocado.
3. **Checks manuales Appwrite / dispositivo** → los haces tú; el agente marca lo implementado en código.
4. No mezclar support/Android-support en este checklist salvo que bloquee stock/finanzas.

### Fórmulas congeladas

- `available = max(0, existence − reserved)`
- COGS = `last_unit_cost × qty` al **VERIFIED** (no promedio)
- Cliente **no** escribe `stock_movements` / `purchase_*` / `sale_finance_event`

---

## Bloque 0 — Baseline (hecho)

- [x] Rama `Core2` creada desde `master` (AlejoTaller + dash)
- [x] Soft-hold Core 1 en código + tests
- [x] Operador: VERIFIED consume existence+reserved; DELETED libera reserved
- [x] Dash: «Dar entrada» suma `existence` (formalizado con movement en B3.1)
- [x] Appwrite collections + permisos staff/operador (no cliente)
- [x] Políticas Core 2 documentadas (POLICY_DELTAS + FINANCE_MODEL)

---

## Bloque 1 — Contrato dominio + DTO/repo

- [x] Enums / entidades movement, purchase, finance (dash + operador)
- [x] Net repos stock_movements + finance en **operador**
- [x] Net repos movements / purchase_* / finance en **dash**
- [x] Constantes collection IDs alineadas
- [x] Tests mapper/DTO round-trip

---

## Bloque 2 — Operador: traza al confirmar

- [x] VERIFIED → `salida_venta` + balance_after + sale_id + user_id
- [x] `sale_finance_event`: revenue, cogs, margin
- [x] UNVERIFIED / DELETED: sin finance ni salida_venta
- [x] Idempotencia + tests unitarios
- [ ] **Check tuyo:** smoke Appwrite tras confirm en dispositivo/emulador

---

## Bloque 3 — Dash: entrada formal + movements

### 3.1 Traza en entrada existente

- [x] «Dar entrada» escribe `stock_movements` tipo `entrada` + `balance_after`
- [x] `reason` + `user_id`
- [x] Test case-use RegisterStockEntry + movement
- [ ] **Check tuyo:** doc `entrada` visible en Appwrite

### 3.2 Factura de entrada (UX principal) — **siguiente código**

- [ ] Registrar entrada multi-producto (cabecera + líneas)
- [ ] Proveedor (buscar/crear `supplier`)
- [ ] `purchase_entry` + `purchase_entry_line` + `existence +=` + movement `entrada`
- [ ] Totales coherentes; actualizar `last_unit_cost` si compra
- [ ] Roles: owner/admin (viewer no muta)

### 3.3 Ajuste / listados

- [ ] Ajuste auditado; listado movements; listado/detalle facturas

---

## Bloque 4 — Reportes y cola

- [ ] Cola UNVERIFIED por antigüedad
- [ ] Resumen ingresos/COGS/margen (solo VERIFIED)

---

## Bloque 5 — Reservas de taller

- [ ] Collection + panel dash (no mezclar Sale tienda)
- [ ] (Opcional) solicitud cliente web

---

## Bloque 6 — Seguridad, CI, cierre

- [ ] Auditoría permisos cliente no escribe movements/finance/purchase
- [x] CI verde en Core2 (operador unit + dash quality)
- [x] PRs Core2 → master: [AlejoTaller #11](https://github.com/danielitoCode/AlejoTaller/pull/11) · [dash #1](https://github.com/danielitoCode/dash_alejo_taller/pull/1)
- [ ] Merge a master (tú)
- [ ] Smoke cruzado entrada → pedido → confirm → salida_venta + finance
- [ ] STATUS ambos repos: Core 2 cerrado + fecha

---

## Orden de ejecución

```text
B0 ✓ → B1 ✓ → B2 ✓ código → B3.1 ✓ → merge PRs + smoke (tú)
  → B3.2 factura multi-línea ← siguiente código
  → B4 → B5 → B6
```

### Política de merge a `master`

| Merge OK cuando | Ejemplo |
|-----------------|---------|
| B1 + tests mapper | contratos |
| B2 + tests (+ smoke tuyo) | operador movement/finance |
| B3.1 traza Dar entrada | auditabilidad sin factura completa |
| B3.2 factura completa | panel listo |
| Nunca | half-finished write path sin test |

---

## Registro de avance

| Fecha | Bloque/ítem | Repo | Nota |
|-------|-------------|------|------|
| 2026-08-18 | B0 schema + baseline | ambos | Collections + permisos cloud |
| 2026-08-18 | Checklist unificado + Core2 | ambos | |
| 2026-08-19 | B1 net repos + tests | ambos | |
| 2026-08-19 | B2 operador salida_venta + finance | AlejoTaller | ApplyOperatorStockDecisionCaseUse |
| 2026-08-21 | B3.1 Dar entrada → entrada | dash | RegisterStockEntryCaseUse |
| 2026-08-21 | CI Core2 verde | ambos | operador 9ef3378 · dash a78b7c4 |
| 2026-08-21 | PRs merge | ambos | #11 AlejoTaller · #1 dash |
