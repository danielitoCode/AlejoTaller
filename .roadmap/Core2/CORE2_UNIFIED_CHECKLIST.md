# Core 2 — Checklist unificado (cliente + backoffice + operador)

**Rama de trabajo:** `Core2` en **AlejoTaller** y **dash_alejo_taller** (partir de `master` estable Core 1).  
**Merge a `master`:** incremental cuando un bloque esté verde (tests + política + smoke manual tuyo).  
**Regla de política:** no romper políticas de un core inferior salvo que un core superior las actualice de forma explícita y documentada.

**Última actualización:** 2026-08-19  
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
- [x] Soft-hold Core 1 en código + tests web (reserved atómico, available)
- [x] Operador: VERIFIED consume existence+reserved; DELETED libera reserved (sin traza movement/finance aún)
- [x] Dash: «Dar entrada» suma `existence` (sin movement formal aún)
- [x] Appwrite collections existentes con permisos staff/operador (no cliente):
  - [x] `stock_movements`
  - [x] `supplier`
  - [x] `purchase_entry`
  - [x] `purchase_entry_line`
  - [x] `sale_finance_event`
  - [x] producto con `last_unit_cost`
- [x] Políticas Core 2 documentadas (POLICY_DELTAS + FINANCE_MODEL) en ambos repos

**Check manual tuyo (opcional anotar):** confirmar en consola Appwrite IDs/atributos reales si difieren del canónico.

---

## Bloque 1 — Contrato dominio + DTO/repo (ambos repos, sin UI aún)

**Objetivo:** mismo lenguaje de dominio en operador, dash y (solo lectura si aplica) web.

### 1.1 Tipos / entidades

- [ ] Enum movement `type`: `entrada` | `salida_venta` | `ajuste` | `devolucion`
- [ ] Enum línea compra `concept`: `purchase` | `royalty` | `other`
- [ ] Entidad `StockMovement` (campos canónicos)
- [ ] Entidad `PurchaseEntry` + `PurchaseEntryLine`
- [ ] Entidad `SaleFinanceEvent`

### 1.2 Repos Appwrite

| Superficie | create movement | read movements | purchase_* | finance event |
|------------|-----------------|----------------|------------|---------------|
| Operador (`alejotallerscan`) | sí (salida_venta) | sí | no (salvo atajo) | sí al VERIFIED |
| Dash | sí (entrada/ajuste/…) | sí | sí | lectura + opcional confirm |
| Cliente web | **no** | **no** | **no** | **no** |

- [x] Repo/DTO stock_movements en **dash**
- [x] Repo/DTO stock_movements en **operador**
- [x] Repo/DTO purchase_entry (+ lines + supplier) en **dash**
- [x] Repo/DTO sale_finance_event en **operador** (+ dash lectura)
- [x] Constantes collection IDs alineadas (`stock_movements`, etc.)
- [x] **Test:** mapper/DTO round-trip movement + finance (+ purchase line)

**Criterio salida B1:** un test unitario crea/mapea documentos sin UI; permisos cliente no permiten write (check tuyo en Appwrite).

---

## Bloque 2 — Operador: traza al confirmar (2.2)

**Repo:** AlejoTaller / `alejotallerscan`  
**No tocar** soft-hold salvo regresión.

- [x] VERIFIED → `stock_movements` tipo `salida_venta` por línea (o agregado documentado)
- [x] `balance_after` = existence tras consume
- [x] `sale_id` + `user_id` operador
- [x] `sale_finance_event`: revenue, cogs (`last_unit_cost × qty`), margin
- [x] UNVERIFIED: **no** finance event
- [x] DELETED/reject: solo release reserved; **sin** salida_venta ni finance
- [x] Idempotencia: 2º confirm no duplica movement ni finance
- [x] **Test** unitario case-use
- [ ] **Check tuyo:** smoke en Appwrite tras confirm en dispositivo/emulador

**Criterio salida B2:** confirm deja qty + dinero; soft-hold sin regresión (tests web verdes).

---

## Bloque 3 — Dash: entrada formal + movements (2.3)

**Repo:** dash_alejo_taller

### 3.1 Traza en entrada existente

- [ ] «Dar entrada» (o sucesor) escribe `stock_movements` tipo `entrada` + `balance_after`
- [ ] Opcional mínimo: `reason`, `user_id`
- [ ] Si hay unit_cost en UI rápida: actualizar `last_unit_cost` cuando concepto compra

### 3.2 Factura de entrada (UX principal)

- [ ] Registrar entrada multi-producto (cabecera + líneas)
- [ ] Proveedor (buscar/crear `supplier`)
- [ ] `purchase_entry` + `purchase_entry_line` + `existence +=` + movement `entrada`
- [ ] Totales coherentes (`line_cost`, `total_cost`)
- [ ] Roles: owner/admin (viewer no muta)

### 3.3 Ajuste / listados

- [ ] Ajuste auditado (`ajuste`) con motivo; post-ajuste `existence >= reserved`
- [ ] Listado `stock_movements` (filtros producto/tipo/fechas)
- [ ] Listado/detalle facturas de entrada

### 3.4 Tests

- [ ] Test case-use entrada → movement + existence
- [ ] Test invariante post-ajuste

**Check tuyo:** entrada multi-línea visible en Appwrite; stock coherente.

**Criterio salida B3:** factura o entrada formal deja traza stock + documento económico.

---

## Bloque 4 — Reportes y cola (2.4, dash primario)

- [ ] Cola ventas UNVERIFIED por antigüedad
- [ ] Resumen periodo: ingresos (solo VERIFIED), COGS, margen, costo entradas
- [ ] UNVERIFIED excluido de ingresos
- [ ] (Cliente) sin UI de ingresos — solo available

**Check tuyo:** números cuadran con 1 venta confirmada + 1 entrada.

---

## Bloque 5 — Reservas de taller (2.5)

- [ ] Collection `appointment`/`booking` en Appwrite (**check tuyo** crear si no existe)
- [ ] Estados: solicitada → confirmada → realizada | cancelada
- [ ] Dash: panel Reservas (no mezclar con Sale tienda)
- [ ] (Opcional cliente web) solicitar cita + list mine + test

---

## Bloque 6 — Seguridad, CI, cierre (2.6)

- [ ] Auditoría permisos: cliente no escribe movements/finance/purchase
- [ ] CI verde en PR de `Core2` (web + dash check/test/build)
- [ ] Smoke cruzado: entrada (dash) → pedido (cliente) → confirm (operador) → `salida_venta` + finance
- [ ] Regresión soft-hold + support web PASS
- [ ] STATUS ambos repos: Core 2 cerrado + fecha

---

## Orden de ejecución (coherente, sin huecos)

```text
B0 baseline          ✓
B1 DTO/repo contrato   ✓ (dash + operador net)
B2 Operador traza      ✓ código (smoke tuyo pendiente)
B3 Dash entrada/UI     ← siguiente
B4 Reportes
B5 Reservas
B6 Cierre + merges
```

### Política de merge a `master`

| Merge OK cuando | Ejemplo |
|-----------------|---------|
| B1 + tests mapper | contratos sin cambiar runtime crítico |
| B2 + tests + smoke tuyo | operador escribe movement/finance |
| B3 parcial (solo traza en Dar entrada) | mejora auditabilidad sin factura completa |
| B3 factura completa | panel listo |
| Nunca | half-finished write path sin test |

---

## Registro de avance

| Fecha | Bloque/ítem | Repo | Nota |
|-------|-------------|------|------|
| 2026-08-18 | B0 schema Appwrite + baseline código | ambos | Confirmado por equipo: collections + permisos en cloud |
| 2026-08-18 | Checklist unificado + rama `Core2` | ambos | Punto de partida organizado |
| 2026-08-19 | B2 operador salida_venta + finance + tests | AlejoTaller | ApplyOperatorStockDecisionCaseUse |
