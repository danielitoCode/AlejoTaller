# Core 3 — Checklist (AlejoTaller · espejo)

**Última actualización:** 2026-08-28  
**Rama:** `Core3`  
**Orden canónico:** mismo que [dash CORE3_UNIFIED_CHECKLIST](https://github.com/danielitoCode/dash_alejo_taller/blob/Core3/.roadmap/Core3/CORE3_UNIFIED_CHECKLIST.md)

Solo se marcan ítems **AT** / **BOTH** aquí. Los **DASH** se marcan en el repo del panel.

---

## B0 — Baseline y política

- [x] **BOTH** Core 2 en `master` verificado (operador salida_venta + finance)
- [x] **BOTH** Política de compras Core 3 leída/aceptada (origen dash README Core3)
- [ ] **AT** Carpeta `.roadmap/Core3/` completa (este checklist + README + STATUS)
- [ ] **AT** Si aplica: nota en `.policies/warehouse` o doc enlace “entradas solo panel”

**DEP hacia dash:** gaps de schema los cierra el panel; AT no crea collections.

**2026-08-28:** Confirmado “casi cerrado” (política, audit schema, tipado `contact` hechos en dash). Restan consola Appwrite + espejo AT. Se inicia **B4**.

---

## B1 — Proveedores

- [ ] **AT** Verificar (consola o doc): rol cliente **sin** create/update en `supplier`
- [ ] **AT** Sin UI ni case use de supplier en web/app/mcp

**DASH B1 (panel):** smoke UI OK 2026-08-27 — subvista Proveedores + alta desde factura de entrada.  
**DEP:** implementación CRUD es **DASH B1**. AT solo guarda permisos/frontera.

---

## B2 — Historial compras

- [ ] **AT** Confirmar que operador/web **no** listan `purchase_entry`
- [ ] **AT** (opcional) Test o nota: lectura `last_unit_cost` en flujo COGS intacta

**DASH B2 (panel):** smoke UI OK 2026-08-27 — listado Compras → detalle.  
**DEP:** listados y detalle son **DASH B2**.

---

## B3 — Anulación / corrección

- [ ] **BOTH** Política de anulación aceptada (no dejar `existence < reserved`)
- [ ] **AT** **DEP DASH B3:** si el panel cambia costos o stock por anulación, re-smoke confirm operador en dispositivo o emulador
- [ ] **AT** Sin endpoint cliente para anular entradas

---

## B4 — Permisos y smoke cruzado

- [ ] **AT** Smoke cliente: no expone compras
- [ ] **AT** Smoke operador: tras una entrada creada en dash (DASH B1/B2), confirm VERIFIED → `salida_venta` + finance OK
- [ ] **AT** MCP health / tools: sin supplier/purchase

**DEP:** conviene tener al menos una factura de prueba creada en **DASH B2**.

**2026-08-28:** B4 iniciado. Ejecutar smokes según `SMOKE_B4.md` del panel.

---

## B5 — Cierre espejo AT

- [ ] **AT** README monorepo / roadmap mencionan Core 3 en rama `Core3` (sin marcar cerrado)
- [ ] **AT** CI de módulos tocados verde
- [ ] **AT** Checklist AT marcado en paralelo al dash

---

## B6 — Merge

- [ ] **AT** PR `Core3` → `master` solo con cambios reales de este núcleo (docs y/o guardas)
- [ ] **AT** **DEP:** no adelantar merge de código que dependa de B3 dash incompleto
- [ ] **BOTH** Coordinar con merge del panel (release parcial B1+B2+B4 o completo con B3)

---

## Orden recordatorio

```text
B0 BOTH
 → DASH B1 ✓ smoke UI → DASH B2 ✓ smoke UI → (DASH B3 opcional) → DASH B4
 → AT B4 smoke (DEP: datos de prueba en dash)
 → B5 AT → B6 ambos
```

| Fecha | Nota |
|-------|------|
| 2026-08-27 | Apertura rama `Core3` + checklist espejo |
| 2026-08-27 | DASH B1+B2 smoke UI verificados (panel) |
| 2026-08-28 | B0 casi cerrado confirmado. Inicio B4 |
