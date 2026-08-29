# Core 3 — Checklist (AlejoTaller · espejo)

**Última actualización:** 2026-08-29  
**Rama:** `Core3`  
**Orden canónico:** mismo que dash CORE3_UNIFIED_CHECKLIST.

Solo se marcan ítems **AT** / **BOTH** aquí. Los **DASH** se marcan en el repo del panel.

---

## B0 — Baseline y política

- [x] **BOTH** Core 2 en `master` verificado (operador salida_venta + finance)
- [x] **BOTH** Política de compras Core 3 leída/aceptada
- [x] **AT** Carpeta `.roadmap/Core3/` completa
- [x] **AT** Entradas de compras/abastecimiento exclusivamente desde el panel; cliente B2C y operador no registran entradas.

**B0 AT cerrado 2026-08-29:** frontera de entradas exclusivamente panel documentada; no quedan tareas B0 AT pendientes.

## B1 — Proveedores

- [x] **AT** Verificar rol cliente sin create/update en `supplier` — verificado previamente.
- [x] **AT** Sin UI ni case use de supplier en web/app/mcp — verificado previamente.

## B2 — Historial compras

- [x] **AT** Confirmar que operador/web no listan `purchase_entry` — verificado previamente.
- [ ] **AT** (opcional) Test o nota: lectura `last_unit_cost` en flujo COGS intacta

## B3 — Anulación / corrección

- [ ] **BOTH** Política de anulación aceptada (no dejar `existence < reserved`)
- [ ] **AT** DEP DASH B3: si el panel cambia costos o stock por anulación, re-smoke confirm operador
- [x] **AT** Sin endpoint cliente para anular entradas — verificado previamente.

## B4 — Permisos y smoke cruzado

- [ ] **AT** Smoke cliente: no expone compras
- [ ] **AT** Smoke operador: tras entrada dash, confirm VERIFIED → `salida_venta` + finance OK
- [ ] **AT** MCP health / tools: sin supplier/purchase

## B5 — Cierre espejo AT

- [ ] **AT** README monorepo / roadmap mencionan Core 3 en rama `Core3`
- [ ] **AT** CI de módulos tocados verde
- [ ] **AT** Checklist AT marcado en paralelo al dash

## B6 — Merge

- [ ] **AT** PR `Core3` → `master` solo con cambios reales de este núcleo
- [ ] **AT** DEP: no adelantar merge que dependa de B3 dash incompleto
- [ ] **BOTH** Coordinar con merge del panel

---

| Fecha | Nota |
|-------|------|
| 2026-08-27 | Apertura rama `Core3` + checklist espejo |
| 2026-08-28 | Dash B0 consola + B2 completo reflejados aquí |
| 2026-08-29 | AT B0 cerrado; permisos/frontera B1 y ausencia de endpoint B3 verificados previamente |
