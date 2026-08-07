# Core 1 — MVP estabilizado (Auth + Sale + Soft-hold Warehouse)

**Estado:** listo para QA manual.  
**Criterio de cierre:** checklist `QA_CORE1_CHECK_plan.md` en verde en Web → Android cliente → Operador.

**Última actualización:** 2026-08-07

## Qué incluye Core 1

- **Auth:** visitante vs autenticado; Welcome solo primera visita; deeplink sin Welcome; visitante no crea ventas.
- **Sale (cliente):** crea solo `UNVERIFIED`; soft-hold (`reserved += qty`); no auto-confirma.
- **Warehouse (cliente + operador):** `available = existence - reserved`; check de disponibilidad antes de UNVERIFIED.
- **UI stock cliente:** badge `available` en listado y detalle (web + Android); compra bloqueada si available = 0.
- **Operador (`alejotallerscan`):** confirma → `VERIFIED` + consume stock; rechaza → `DELETED` + libera reserved; elige `SaleType` (NORMAL / DISCOUNT / GIFT) y monto editable en DISCOUNT.
- **Tests parciales:** soft-hold / RegisterNewSale (web + Android).

## Qué NO incluye (va a Core 2)

- Collection `stock_movements` operativa y reportes.
- Ajustes de inventario, entradas, devoluciones formales.
- Sentry/Crashlytics y function atómica en backend.
- Tests automatizados de operador confirm/reject.

## Índice de este directorio

| Archivo | Descripción |
|---------|-------------|
| [QA_CORE1_CHECK_plan.md](./QA_CORE1_CHECK_plan.md) | **Checklist QA** por tecnología (Web, Android, Operador) — marcar con `x` |
| [MVP_CORE1_STATUS.md](./MVP_CORE1_STATUS.md) | Estado de fases / micro-tareas Core 1 |
| [AUDIT_1_1.md](./AUDIT_1_1.md) | Auditoría estabilización (Fase 1.1) |
| [DESIGN_SOFT_HOLD.md](./DESIGN_SOFT_HOLD.md) | Diseño soft-hold existence/reserved/available |
| [APPWRITE_CORE1_SCHEMA.md](./APPWRITE_CORE1_SCHEMA.md) | Atributos Appwrite mínimos Core 1 |

**Políticas:** `.policies/auth`, `.policies/sale`, `.policies/warehouse`
