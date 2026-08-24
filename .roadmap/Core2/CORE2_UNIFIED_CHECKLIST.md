# Core 2 — Checklist unificado (cliente + backoffice + operador)

**Última actualización:** 2026-08-24  
**Core 2 cerrado (ecosistema):** **SÍ** (dash mergeado a master PR #12; operador B2 en código)

### Fórmulas congeladas

- `available = max(0, existence − reserved)`
- COGS = `last_unit_cost × qty` al VERIFIED
- Cliente **no** escribe `stock_movements` / `purchase_*` / `sale_finance_event` / `workshop_reservation`

---

## Bloque 0 — Baseline ✓

- [x] Rama `Core2` (AlejoTaller + dash)
- [x] Soft-hold Core 1
- [x] Collections + permisos Appwrite staff
- [x] Políticas documentadas

## Bloque 1 — Dominio + DTO/repo ✓

- [x] Enums / net repos movements + finance (operador + dash)
- [x] Tests mapper

## Bloque 2 — Operador traza VERIFIED ✓

- [x] `salida_venta` + `sale_finance_event` al confirm
- [x] Sin finance en UNVERIFIED; sin salida en DELETED
- [x] Idempotencia + tests unitarios
- [x] Paridad equivalente smokeada desde backoffice
- [ ] Smoke dispositivo operador (opcional)

## Bloque 3–5 — Dash (superficie primaria) ✓

Implementado y mergeado en **dash_alejo_taller** (PR #12 → master):

- [x] Factura multi-línea + movements `entrada` + `last_unit_cost`
- [x] Ajuste + listados Inventario
- [x] Cola UNVERIFIED + KPIs finance + paridad confirm panel
- [x] Reservas taller (`workshop_reservation`)

## Bloque 6 — CI, permisos, cierre ✓

- [x] Permisos Appwrite auditados
- [x] CI verde (dash + cliente)
- [x] Smoke cruzado (entrada → pedido → confirm → salida_venta + finance)
- [x] Merge dash Core2 → master (PR #12, 2026-08-24)
- [x] STATUS / checklist cerrados en dash; este archivo alineado

---

```text
B0 ✓ → B1 ✓ → B2 ✓ (operador) → B3–B5 ✓ (dash) → B6 ✓
```

| Fecha | Ítem |
|-------|------|
| 2026-08-21 | B2 operador + B3.1 dash |
| 2026-08-24 | B3–B5 dash + B6 merge master dash |
