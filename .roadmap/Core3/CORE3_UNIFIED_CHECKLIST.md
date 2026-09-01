# Core 3 — Checklist (AlejoTaller · espejo)

**Última actualización:** 2026-09-01  
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
- [ ] **AT** (opcional) Test/nota Core3: lectura `last_unit_cost` en COGS — no bloquea

## B3 — Anulación / corrección

- [x] **BOTH** Política de anulación aceptada (`existence - qty >= reserved`; no tocar `reserved` / `last_unit_cost`)
- [x] **AT** Sin endpoint cliente para anular entradas
- [x] **DASH** UI B3.1 cerrada en panel (referencia; no código AT)
- [ ] **AT** Smoke operador post-anulación (opcional; no bloquea merge)

## B4 / B5 — Qué verificar (manual; código ya OK)

El dash marca frontera en código. Confirmación runtime es **opcional** para merge:

### Cliente web/app
- [ ] No hay pantalla ni ruta de Proveedores / Compras / factura de entrada
- [ ] Un login cliente no puede crear documentos en `supplier` / `purchase_entry` / `purchase_entry_line`

### MCP
- [ ] `get_server_info` / health: scope `b2c-customer`
- [ ] No existe tool de supplier ni purchase_entry
- [ ] `notInScope` menciona `purchase_entry`

### Operador (`alejotallerscan`)
- [ ] Tras entrada de compra en panel, confirm VERIFIED escribe `salida_venta` + finance
- [ ] COGS usa `last_unit_cost` del producto
- [ ] Tras anulación en panel: `last_unit_cost` no cambia; stock coherente con reserved

### Docs / CI
- [x] README / `.roadmap/Core3` alineados a release mínimo dash
- [ ] CI módulos tocados verde en PR
- [x] Checklist AT alineado a dash

## B6 — Merge

- [x] **AT** Docs/frontera listos (sin código de anulación cliente)
- [ ] **AT** PR `Core3` → `master`
- [ ] **BOTH** Coordinar con merge del panel

---

| Fecha | Nota |
|-------|------|
| 2026-08-27 | Apertura rama `Core3` |
| 2026-08-29 | B0–B2 AT cerrados; B3 política; release parcial docs a master (PR #25) |
| 2026-09-01 | Saneamiento: B3.1 dash cerrado; checklist listo para merge final |
