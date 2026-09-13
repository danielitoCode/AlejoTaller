# Core 5 — Checklist de implementación · **AlejoTaller**

**Rama:** `Core5` · **Actualizado:** 2026-09-13  
**Canónico reportes:** [dash DASH checklist](https://github.com/danielitoCode/dash_alejo_taller/blob/master/.roadmap/Core5/DASH_IMPLEMENTATION_CHECKLIST.md) (mergeado a `master` vía PR #25)

---

## B0 — Baseline — **CERRADO** (2026-09-02)

- [x] Docs `.roadmap/Core5/` presentes
- [x] Política dash Core 5 **aceptada** (lectura-only; no redefine `.policies/sale` ni warehouse)
- [x] Core 4 write operador sigue siendo la fuente que el panel lee (sin cambios de dominio aquí)
- [x] CI workflows incluyen rama `Core5` (web/android/mcp/coverage)

**Evaluación políticas AT:** Core 5 no exige editar `SALE_POLICY` / `WAREHOUSE_POLICY` ni rehacer tests de confirm/reject. B2C y MCP siguen sin KPIs staff.

---

## B1–B4 — N/A (dash)

Reportes, agregados y UI de supervisión viven solo en `dash_alejo_taller`.

---

## B5 — Frontera — **CERRADO** (2026-09-13)

- [x] web: sin KPIs staff / create finance (sin código nuevo de reportes)
- [x] mcp: frontera documentada — `sale_finance_event` no es superficie B2C
- [x] scan: createIdempotent Core 4 intacto; DELETED sin finance (sin cambios de dominio en este release)
- [x] Dash Core 5 mergeado a `master` (PR #25)
- [ ] PR `Core5` → `master` (este PR de alineación docs + CI)

## Registro

| Fecha | Nota |
|-------|------|
| 2026-09-02 | B0 cerrado (espejo) |
| 2026-09-13 | B5 frontera + PR alineación → master |
