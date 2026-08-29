# Core 3 — Checklist (AlejoTaller · espejo)

**Última actualización:** 2026-08-28  
**Rama:** `Core3`  
**Orden canónico:** mismo que [dash CORE3_UNIFIED_CHECKLIST](https://github.com/danielitoCode/dash_alejo_taller/blob/Core3/.roadmap/Core3/CORE3_UNIFIED_CHECKLIST.md)

Solo se marcan ítems **AT** / **BOTH** aquí. Los **DASH** se marcan en el repo del panel.

---

## B0 — Baseline y política

- [x] **BOTH** Core 2 en `master` verificado (operador salida_venta + finance)
- [x] **BOTH** Política de compras Core 3 leída/aceptada (origen dash README Core3)
- [x] **AT** Carpeta `.roadmap/Core3/` completa (este checklist + README + STATUS)
- [ ] **AT** Si aplica: nota en `.policies/warehouse` o doc enlace “entradas solo panel”

**DASH B0:** consola índices / `entry_id` / permisos cliente **verificada 2026-08-28**.

---

## B1 — Proveedores

- [ ] **AT** Verificar (consola o doc): rol cliente **sin** create/update en `supplier`
- [ ] **AT** Sin UI ni case use de supplier en web/app/mcp

**DASH B1 (panel):** smoke UI OK 2026-08-27.

---

## B2 — Historial compras

- [ ] **AT** Confirmar que operador/web **no** listan `purchase_entry`
- [ ] **AT** (opcional) Test o nota: lectura `last_unit_cost` en flujo COGS intacta

**DASH B2 (panel):** completo — listado, detalle, filtros, movements `entry_id`, filtro producto. Código verificado 2026-08-28.

---

## B3 — Anulación / corrección

- [ ] **BOTH** Política de anulación aceptada (no dejar `existence < reserved`)
- [ ] **AT** **DEP DASH B3:** si el panel cambia costos o stock por anulación, re-smoke confirm operador
- [ ] **AT** Sin endpoint cliente para anular entradas

---

## B4 — Permisos y smoke cruzado

- [ ] **AT** Smoke cliente: no expone compras
- [ ] **AT** Smoke operador: tras entrada dash, confirm VERIFIED → `salida_venta` + finance OK
- [ ] **AT** MCP health / tools: sin supplier/purchase

**2026-08-28:** B4 en curso (panel + frontera AT).

---

## B5 — Cierre espejo AT

- [ ] **AT** README monorepo / roadmap mencionan Core 3 en rama `Core3` (sin marcar cerrado)
- [ ] **AT** CI de módulos tocados verde
- [ ] **AT** Checklist AT marcado en paralelo al dash

---

## B6 — Merge

- [ ] **AT** PR `Core3` → `master` solo con cambios reales de este núcleo
- [ ] **AT** **DEP:** no adelantar merge que dependa de B3 dash incompleto
- [ ] **BOTH** Coordinar con merge del panel (release parcial B1+B2+B4 o completo con B3)

---

| Fecha | Nota |
|-------|------|
| 2026-08-27 | Apertura rama `Core3` + checklist espejo |
| 2026-08-28 | Dash B0 consola + B2 completo reflejados aquí |
