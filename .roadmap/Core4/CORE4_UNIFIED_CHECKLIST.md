# Core 4 — Checklist (AlejoTaller)

**Última actualización:** 2026-09-02  
**Rama:** `Core4`  
**Canónico completo:** [dash CORE4_UNIFIED_CHECKLIST](https://github.com/danielitoCode/dash_alejo_taller/blob/Core4/.roadmap/Core4/CORE4_UNIFIED_CHECKLIST.md)

Este archivo lista solo lo que **toca a este monorepo**. El orden global es B0→B6 en el dash.

---

## B0 — Baseline

- [x] Core 2 operador: finance al VERIFIED verificado en código
- [x] Política del dash leída; sin contradicciones locales
- [x] Docs `.roadmap/Core4/` presentes en esta rama

## B1 — Contrato

- [x] Tipos / `SaleFinanceWrite` + `SaleFinanceLineWrite` con `unitCostSnapshot`
- [x] Alineación de nombres con el panel (`productId`, `unitCostSnapshot`, `lines_json`)
- [x] Repo `AppwriteOperatorSaleFinanceRepository` serializa/parsea `lines_json`

## B3 — Confirm operador (bloque principal AT)

- [x] `ApplyOperatorStockDecisionCaseUse` escribe finance con snapshot por línea al `confirmed=true`
- [x] `createIdempotent` no duplica ni recalcula si ya existe event para `sale_id` (unit)
- [x] `confirmed=false` (DELETED): sin finance (unit)
- [x] Costo ausente → snapshot 0 sin tumbar el flujo de stock (unit)
- [ ] Smoke runtime dispositivo/emulador (opcional)

## B4 — Estabilidad

- [x] Tras crear event, un cambio de `last_unit_cost` en producto **no** se refleja en el event existente *(unit 2026-09-02)*
- [x] Reintento de confirm no crea segundo documento; `createIdempotent` conserva lines/snapshot *(unit)*

## B5 — Tests

- [x] Unit: COGS con varias líneas, snapshots y costo ausente
- [x] Unit: idempotencia por `sale_id`
- [x] Unit B4: no-reescritura con costo vivo distinto + payload createIdempotent ignorado

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
| 2026-09-01 | B0+B1; B3 case use + unit tests (lines/snapshot); smoke dispositivo pendiente |
| 2026-09-02 | B4 unit: 2º confirm + createIdempotent no reescribe histórico (FakeFinanceRepo paridad Appwrite) |
