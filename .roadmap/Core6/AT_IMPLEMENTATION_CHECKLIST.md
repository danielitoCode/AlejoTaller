# Core 6 — Checklist de implementación · **AlejoTaller**

**Rama:** `Core6` · **Actualizado:** 2026-09-13  
**Canónico producto:** [dash DASH checklist](https://github.com/danielitoCode/dash_alejo_taller/blob/Core6/.roadmap/Core6/DASH_IMPLEMENTATION_CHECKLIST.md)

---

## B0 — Baseline / frontera — **en curso**

- [ ] Docs `.roadmap/Core6/` presentes
- [ ] Aceptar regla: reserva taller **≠** `Sale` / soft-hold de producto
- [ ] Confirmar que web/MCP/scan **no** escriben `sale_finance_event` ni stock por crear cita
- [ ] CI: incluir rama `Core6` en workflows web / android / mcp / coverage

---

## B1 — N/A (dominio staff en dash)

Modelo `Appointment`, repos staff y agenda operativa → **dash**.

---

## B2 — Solicitud B2C (web) — **opcional release**

Incluir solo si el producto quiere que el cliente pida cita desde la app:

- [ ] Case use “request appointment” → estado `REQUESTED` (permisos cliente limitados)
- [ ] UI mínima (servicio + franja + notas)
- [ ] Sin conversión a venta; sin tocar `reserved` de productos
- [ ] Tests unitarios del case use
- [ ] (Android) paridad diferible a post-MVP web

Si **no** entra en el release: marcar N/A y documentar “solo panel crea/gestiona citas”.

---

## B3 — Operador (scan)

- [ ] Sin agenda completa en scan (fuera de alcance salvo acuerdo explícito)
- [ ] Si hay deep-link o aviso: solo lectura / no mutar stock por cita

---

## B4 — MCP / agente

- [ ] Tools MCP no inventan `Sale` al “reservar taller”
- [ ] Si hay tool de cita: solo `REQUESTED` + mismos límites de escritura que web

---

## B5 — Cierre frontera

- [ ] STATUS actualizado
- [ ] PR `Core6` → `master` coordinado con dash (docs + CI; + B2 si aplica)
- [ ] Smoke: pedir cita (si B2) no crea sale ni movement

---

## Orden

```text
B0 → (B2 opcional) → B3/B4 → B5 → merge
```

## Registro

| Fecha | Nota |
|-------|------|
| 2026-09-13 | Apertura espejo AT |
