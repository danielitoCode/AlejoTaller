# MVP Core 4 — Estado vivo (AlejoTaller)

**Última actualización:** 2026-09-02  
**Rama:** `Core4`  
**Core 4 en este monorepo:** B0–B4 unit OK; smoke dispositivo y B6 pendientes

| Bloque | Estado |
|--------|--------|
| B0 Docs / baseline | **Cerrado** |
| B1 Tipos contrato líneas/snapshot + repo | **Hecho** |
| B3 Write operador con snapshot en case use | **Hecho** (unit tests); smoke dispositivo pendiente |
| B4 Idempotencia / no reescritura | **Hecho** (unit 2026-09-02) |
| B5 Tests | unit B3 + B4 OK |
| B6 Frontera + smoke + PR | pendiente |

### B3 entregado

- `ApplyOperatorStockDecisionCaseUse` construye `SaleFinanceLineWrite` con `unitCostSnapshot`
- `SaleFinanceWrite.lines` → repo `lines_json`
- Unit: multi-línea, costo null → 0, DELETED sin finance, idempotencia

### B4 entregado

- `AppwriteOperatorSaleFinanceRepository.createIdempotent` ya devolvía existing por `sale_id`
- Fake de tests ahora **persiste** el primer event (paridad real)
- Unit: 2º confirm con `last_unit_cost=99` conserva snapshot `5` / cogs `10`
- Unit: `createIdempotent` con payload nuevo no sobrescribe lines

### Siguiente

B6 frontera/smoke/CI o B5 residual en dash (margen doc vs Σ líneas). Coordinar PR cuando CI verde.
