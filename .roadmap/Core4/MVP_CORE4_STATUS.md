# MVP Core 4 — Estado vivo (AlejoTaller)

**Última actualización:** 2026-09-01  
**Rama:** `Core4`  
**Core 4 en este monorepo:** en implementación (B1 tipos OK)

| Bloque | Estado |
|--------|--------|
| B0 Docs / baseline | **Cerrado** |
| B1 Tipos contrato líneas/snapshot + repo | **Hecho** |
| B3 Write operador con snapshot en case use | pendiente |
| B4 Idempotencia / no reescritura | base Core2; validar con lines |
| B5 Tests | pendiente |
| B6 Frontera + smoke + PR | pendiente |

### Heredado Core 2

- `ApplyOperatorStockDecisionCaseUse`: VERIFIED → stock + `salida_venta` + finance agregado
- DELETED → solo libera `reserved`
- `createIdempotent` por `sale_id`

### B1 entregado

- `SaleFinanceLineWrite` + `SaleFinanceWrite.lines`
- Repo escribe/lee `lines_json` (paridad dash Opción A)

### Siguiente

B3: en `ApplyOperatorStockDecisionCaseUse`, construir `lines` con `unitCostSnapshot` al confirmar (no solo totales).
