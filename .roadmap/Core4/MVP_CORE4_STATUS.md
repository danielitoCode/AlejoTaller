# MVP Core 4 — Estado vivo (AlejoTaller)

**Última actualización:** 2026-09-01  
**Rama:** `Core4`  
**Core 4 en este monorepo:** B0–B3 código OK; smoke dispositivo y B4 pendientes

| Bloque | Estado |
|--------|--------|
| B0 Docs / baseline | **Cerrado** |
| B1 Tipos contrato líneas/snapshot + repo | **Hecho** |
| B3 Write operador con snapshot en case use | **Hecho** (unit tests); smoke dispositivo pendiente |
| B4 Idempotencia / no reescritura | **Siguiente** (base Core2; formalizar con lines) |
| B5 Tests | unit B3 OK |
| B6 Frontera + smoke + PR | pendiente |

### B3 entregado

- `ApplyOperatorStockDecisionCaseUse` construye `SaleFinanceLineWrite` con `unitCostSnapshot`
- `SaleFinanceWrite.lines` → repo `lines_json`
- Unit: multi-línea, costo null → 0, DELETED sin finance, idempotencia

### Siguiente en ecosistema

Coordinado con dash: **B4** (estabilidad histórica / no recalcular). Smoke operador cuando haya dispositivo.
