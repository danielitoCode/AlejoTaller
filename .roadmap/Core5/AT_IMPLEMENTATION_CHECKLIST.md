# Core 5 — Checklist de implementación · **AlejoTaller**

**Rama:** `Core5`  
**Última actualización:** 2026-09-02  
**Canónico de producto (KPIs/UI):** [dash DASH_IMPLEMENTATION_CHECKLIST](https://github.com/danielitoCode/dash_alejo_taller/blob/Core5/.roadmap/Core5/DASH_IMPLEMENTATION_CHECKLIST.md)

Este monorepo **no** implementa el panel de reportes. Solo asegura **frontera** y que Core 4 (write finance operador) no se rompa.

---

## B0 — Baseline

- [ ] Docs `.roadmap/Core5/` presentes en esta rama
- [ ] Leer política dash: reportes = **lectura**; write finance solo al confirm
- [ ] Confirmar que operador en esta base sigue con `createIdempotent` + lines (Core 4)

**Salida:** docs OK; sin código de KPIs staff.

---

## B1–B4 — No aplican (dash)

No abrir issues de agregados/UI de reportes en:

- `web/` (cliente B2C)
- `mcp/`
- `alejotallerscan/` (salvo bugfix de write Core 4)

Si en el futuro se pide un indicador local en el operador (“última confirmación”), documentarlo como **extras** fuera del MVP Core 5 y no como KPI de panel.

---

## B5 — Frontera y cierre espejo

### web

- [ ] No hay store/pantalla de resumen `sale_finance_event` global staff
- [ ] No hay create/update a colección finance desde cliente

### mcp

- [ ] `sale_finance_event` sigue en colecciones prohibidas / notInScope
- [ ] No hay tool de “reporte financiero staff”

### alejotallerscan

- [ ] Confirm sigue escribiendo finance solo vía path Core 4 (`createIdempotent`)
- [ ] Reject / DELETED sigue **sin** finance
- [ ] CI operador verde en cambios de esta rama

### Cierre

- [ ] [MVP_CORE5_STATUS.md](./MVP_CORE5_STATUS.md) actualizado
- [ ] PR docs/frontera coordinado con dash `Core5` → `master` (si solo docs, un PR de docs basta)

**Salida:** frontera verificada; sin regresión Core 4 write.

---

## Orden de trabajo (AT)

```text
B0 (ahora) → … esperar dash B1–B4 … → B5 frontera al cerrar Core 5
```

## Registro

| Fecha | Nota |
|-------|------|
| 2026-09-02 | Checklist AT de implementación (frontera) |
