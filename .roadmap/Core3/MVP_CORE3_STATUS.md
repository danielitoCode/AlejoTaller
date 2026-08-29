# MVP Core 3 — Estado vivo (AlejoTaller)

**Última actualización:** 2026-08-29  
**Rama:** `Core3`  
**Core 3 cerrado:** **NO**  
**Superficie:** secundaria (docs, permisos, no regresión operador/cliente)

| Bloque AT | Estado |
|-----------|--------|
| B0 Baseline / política / frontera entradas | **Hecho** |
| B1 Permisos frontera supplier | **Hecho** (sin UI/write cliente) |
| B2 Sin listado purchase_entry en cliente/operador | **Hecho** |
| B3 Política + sin endpoint anular | **Hecho.** Smoke post-anulación **DEP UI dash** |
| B3 opcional test `last_unit_cost` COGS | **Pendiente** (no bloquea; código operador ya lee el campo) |
| B4/B5 smokes runtime | **Por verificar** — lista en `CORE3_UNIFIED_CHECKLIST.md` |
| B6 merge | no |

### Código ya verificado (no es smoke de dispositivo)

- MCP: scope B2C; `purchase_entry` en `notInScope`; sin tools de abastecimiento
- Operador: COGS `last_unit_cost × qty` al VERIFIED (`ApplyOperatorStockDecisionCaseUse`)
- Cliente: sin pantallas de compras/proveedores

### Referencia panel (`dash_alejo_taller` / `Core3`)

- B0–B2 + B4 panel: hechos
- `purchase_entry.status` ACTIVE\|CANCELLED: **provisionado 2026-08-29**
- B3.1 núcleo transaccional: hecho; **falta UI anular**
