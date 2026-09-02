# Core 4 — Checklist (AlejoTaller)

**Última actualización:** 2026-09-02  
**Rama:** `Core4`  
**Canónico completo:** [dash CORE4_UNIFIED_CHECKLIST](https://github.com/danielitoCode/dash_alejo_taller/blob/Core4/.roadmap/Core4/CORE4_UNIFIED_CHECKLIST.md)

---

## B0 — Baseline

- [x] Core 2 operador: finance al VERIFIED verificado en código
- [x] Política del dash leída; sin contradicciones locales
- [x] Docs `.roadmap/Core4/` presentes en esta rama

## B1 — Contrato

- [x] Tipos / `SaleFinanceWrite` + `SaleFinanceLineWrite` con `unitCostSnapshot`
- [x] Alineación de nombres con el panel (`productId`, `unitCostSnapshot`, `lines_json`)
- [x] Repo `AppwriteOperatorSaleFinanceRepository` serializa/parsea `lines_json`

## B3 — Confirm operador

- [x] `ApplyOperatorStockDecisionCaseUse` escribe finance con snapshot por línea al `confirmed=true`
- [x] `createIdempotent` no duplica ni recalcula si ya existe event para `sale_id` (unit)
- [x] `confirmed=false` (DELETED): sin finance (unit)
- [x] Costo ausente → snapshot 0 sin tumbar el flujo de stock (unit)
- [ ] Smoke runtime dispositivo/emulador (opcional)

## B4 — Estabilidad

- [x] Tras crear event, un cambio de `last_unit_cost` en producto **no** se refleja en el event existente *(unit)*
- [x] Reintento de confirm no crea segundo documento; `createIdempotent` conserva lines/snapshot *(unit)*

## B5 — Tests / paridad

- [x] Unit: COGS con varias líneas, snapshots y costo ausente
- [x] Unit: idempotencia por `sale_id` + B4 no-reescritura
- [x] Nota paridad: [PARITY_PANEL_OPERATOR.md](./PARITY_PANEL_OPERATOR.md) (espejo dash 2026-09-02)

## B6 — Frontera, smoke, cierre

- [ ] Cliente web/app: sin write a colección finance
- [ ] MCP: sin tool de create finance
- [ ] Smoke operador confirm / reject
- [ ] CI módulos tocados verde
- [ ] STATUS actualizado; PR coordinado con dash

---

## Registro

| Fecha | Nota |
|---|---|
| 2026-09-01 | Apertura rama `Core4`; docs espejo |
| 2026-09-01 | B0+B1; B3 case use + unit tests |
| 2026-09-02 | B4 unit no-reescritura; fix CI FakeStockRepo |
| 2026-09-02 | B5 espejo: PARITY_PANEL_OPERATOR |
