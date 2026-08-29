# Core 3 — Checklist (AlejoTaller · espejo)

**Última actualización:** 2026-08-29  
**Rama:** `Core3`  
**Orden canónico:** dash `CORE3_UNIFIED_CHECKLIST`.

Solo ítems **AT** / **BOTH**. Los **DASH** se marcan en el panel.

---

## B0 — Baseline y política

- [x] **BOTH** Core 2 en `master` verificado (operador salida_venta + finance)
- [x] **BOTH** Política de compras Core 3 leída/aceptada
- [x] **AT** Carpeta `.roadmap/Core3/` completa
- [x] **AT** Entradas solo desde el panel

**B0 AT cerrado.**

## B1 — Proveedores

- [x] **AT** Cliente sin create/update en `supplier`
- [x] **AT** Sin UI ni case use de supplier en web/app/mcp

## B2 — Historial compras

- [x] **AT** Operador/web no listan `purchase_entry`
- [ ] **AT** (opcional) Test/nota Core3: lectura `last_unit_cost` en COGS — **se mantiene pendiente** (código operador ya lee el campo; no hay test nuevo en esta rama)

## B3 — Anulación / corrección

- [x] **BOTH** Política de anulación aceptada (`existence - qty >= reserved`; no tocar `reserved` / `last_unit_cost`)
- [x] **AT** Sin endpoint cliente para anular entradas
- [ ] **AT** **DEP DASH UI B3:** smoke operador post-anulación (cuando el panel anule una entrada de prueba)

## B4 / B5 — Qué verificar (manual; código ya OK)

El dash marca frontera en código. **Confirmar en runtime** y luego marcar `[x]`:

### Cliente web/app
- [ ] No hay pantalla ni ruta de Proveedores / Compras / factura de entrada
- [ ] Un login cliente no puede crear documentos en `supplier` / `purchase_entry` / `purchase_entry_line` (error de permisos si se intenta por API)

### MCP
- [ ] `get_server_info` / health: scope `b2c-customer`
- [ ] No existe tool de supplier ni purchase_entry
- [ ] `notInScope` menciona `purchase_entry`

### Operador (`alejotallerscan`) — **antes** de anulación dash
- [ ] Tras una **entrada de compra en el panel**, confirmar venta VERIFIED
- [ ] Se escribe `salida_venta` + `sale_finance_event`
- [ ] COGS usa `last_unit_cost` del producto (el que dejó la factura), no un promedio
- [ ] `existence >= reserved` se mantiene

### Operador — **después** de anulación dash (cuando exista UI)
- [ ] Anular entrada en panel → stock baja, `last_unit_cost` **no** cambia
- [ ] Confirm VERIFIED de una venta que no dependa de ese stock anulado sigue OK
- [ ] Si el stock anulado dejaría `existence < reserved`, el panel debe haber bloqueado la anulación

### Docs / CI
- [x] README / `.roadmap/Core3` mencionan Core 3 en rama `Core3`
- [ ] CI módulos tocados verde
- [x] Checklist AT alineado a dash (este archivo + STATUS)

## B6 — Merge

- [ ] **AT** PR `Core3` → `master` (docs/frontera; no código de anulación cliente)
- [ ] **AT** No mergear asumiendo UI B3 dash si esa UI aún no está
- [ ] **BOTH** Coordinar con merge del panel

---

| Fecha | Nota |
|-------|------|
| 2026-08-27 | Apertura rama `Core3` |
| 2026-08-29 | B0–B2 AT cerrados en código/docs; B3 política aceptada; `status` dash provisionado |
| 2026-08-29 | B4/B5: lista de verificación manual (no marcar smoke sin runtime) |
