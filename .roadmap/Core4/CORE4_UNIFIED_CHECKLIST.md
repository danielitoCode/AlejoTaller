# Core 4 — Checklist (AlejoTaller)

**Última actualización:** 2026-09-01  
**Rama:** `Core4`  
**Canónico completo:** [dash CORE4_UNIFIED_CHECKLIST](https://github.com/danielitoCode/dash_alejo_taller/blob/Core4/.roadmap/Core4/CORE4_UNIFIED_CHECKLIST.md)

Este archivo lista solo lo que **toca a este monorepo**. El orden global es B0→B6 en el dash.

---

## B0 — Baseline

- [ ] Core 2 operador: finance al VERIFIED verificado en código
- [ ] Política del dash leída; sin contradicciones locales
- [ ] Docs `.roadmap/Core4/` presentes en esta rama

## B1 — Contrato

- [ ] Tipos / `SaleFinanceWrite` (o equivalente) listos para detalle de líneas + `unitCostSnapshot`
- [ ] Alineación de nombres de campos con el panel (documentada)

## B3 — Confirm operador (bloque principal AT)

- [ ] `ApplyOperatorStockDecisionCaseUse` (o sucesor) escribe finance con snapshot por línea al `confirmed=true`
- [ ] `createIdempotent` no duplica ni recalcula si ya existe event para `sale_id`
- [ ] `confirmed=false` (DELETED): sin finance
- [ ] Costo ausente → snapshot 0 sin tumbar el flujo de stock

## B4 — Estabilidad

- [ ] Tras crear event, un cambio de `last_unit_cost` en producto **no** se refleja en el event existente
- [ ] Reintento de confirm no crea segundo documento

## B5 — Tests

- [ ] Unit/instrumented: COGS con varias líneas y costos
- [ ] Unit/instrumented: idempotencia por `sale_id`

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
| 2026-09-01 | Apertura rama `Core4`; docs espejo iniciales |
