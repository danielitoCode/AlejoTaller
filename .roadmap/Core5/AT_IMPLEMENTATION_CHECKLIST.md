# Core 5 — Checklist de implementación · **AlejoTaller**

**Rama:** `Core5` · **Actualizado:** 2026-09-02  
**Canónico reportes:** [dash DASH checklist](https://github.com/danielitoCode/dash_alejo_taller/blob/Core5/.roadmap/Core5/DASH_IMPLEMENTATION_CHECKLIST.md)

---

## B0 — Baseline — **CERRADO** (2026-09-02)

- [x] Docs `.roadmap/Core5/` presentes
- [x] Política dash Core 5 **aceptada** (lectura-only; no redefine `.policies/sale` ni warehouse)
- [x] Core 4 write operador sigue siendo la fuente que el panel lee (sin cambios de dominio aquí)

**Evaluación políticas AT:** Core 5 no exige editar `SALE_POLICY` / `WAREHOUSE_POLICY` ni rehacer tests de confirm/reject. B2C y MCP siguen sin KPIs staff.

---

## B1–B4 — N/A (dash)

---

## B5 — Frontera (al cierre Core 5)

- [ ] web: sin KPIs staff / create finance
- [ ] mcp: `sale_finance_event` prohibido
- [ ] scan: createIdempotent Core 4 intacto; DELETED sin finance
- [ ] STATUS + PR coordinado con dash

## Registro

| Fecha | Nota |
|-------|------|
| 2026-09-02 | B0 cerrado (espejo) |
