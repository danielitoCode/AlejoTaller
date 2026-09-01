# MVP Core 4 — Estado vivo (AlejoTaller)

**Última actualización:** 2026-09-01  
**Rama:** `Core4`  
**Core 4 en este monorepo:** **NO** cerrado — docs iniciales

| Bloque | Estado |
|--------|--------|
| B0 Docs / baseline | **Docs creados** |
| B1 Tipos contrato líneas/snapshot | pendiente |
| B3 Write operador con snapshot | pendiente |
| B4 Idempotencia / no reescritura | pendiente (base Core2 existe) |
| B5 Tests | pendiente |
| B6 Frontera + smoke + PR | pendiente |

### Heredado Core 2

- `ApplyOperatorStockDecisionCaseUse`: VERIFIED → stock + `salida_venta` + finance agregado
- DELETED → solo libera `reserved`
- `createIdempotent` por `sale_id`

### Enfoque

No implementar UI de compras ni reportes. Solo **paridad del evento financiero** con el panel y **frontera** cliente/MCP.
