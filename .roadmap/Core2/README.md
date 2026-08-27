# Core 2 — Inventario formal, finanzas, reservas y endurecimiento

**Estado:** **cerrado** (2026-08-24)  
**Ecosistema:** operador en este monorepo + back-office en `dash_alejo_taller` (PR #12 → master).

## Qué incrementa este núcleo (vs Core 1)

| Superficie | Entregado |
|------------|-----------|
| **Operador** (`alejotallerscan`) | Al VERIFIED: `stock_movements` tipo `salida_venta` + `sale_finance_event` (COGS = `last_unit_cost × qty`) |
| **Dash** | Factura de entrada multi-línea; movements `entrada`; listados inventario; cola UNVERIFIED + KPIs; reservas taller (`workshop_reservation`); permisos staff/cliente |
| **Cliente** | Soft-hold Core 1 intacto; sin write a movements / purchase / finance / reservation |
| **Contrato** | Misma fórmula `available = max(0, existence − reserved)`; paridad confirm panel ↔ operador |

## No incluido (implementación futura)

| Ítem | Notas |
|------|-------|
| **Ajuste de inventario (UI)** | Política y tipo `ajuste` documentados; **UI no disponible** |
| Devolución formal (UI) | Política documentada |
| Reserva taller desde cliente web | Solo gobierno en dash en Core 2 |
| Smoke dispositivo físico operador | Opcional post-cierre |

## Documentos

| Archivo | Rol |
|---------|-----|
| [**CORE2_UNIFIED_CHECKLIST.md**](./CORE2_UNIFIED_CHECKLIST.md) | Checklist cerrado del ecosistema |
| [MVP_CORE2_STATUS.md](./MVP_CORE2_STATUS.md) | Estado vivo |
| [POLICY_DELTAS_CORE2.md](./POLICY_DELTAS_CORE2.md) | Deltas vs Core 1 |
| [FINANCE_MODEL_CORE2.md](./FINANCE_MODEL_CORE2.md) | Factura entrada + COGS último costo |
| [APPWRITE_STOCK_MOVEMENTS.md](./APPWRITE_STOCK_MOVEMENTS.md) | Schema `stock_movements` |
| [DESIGN_STOCK_MOVEMENTS.md](./DESIGN_STOCK_MOVEMENTS.md) | Diseño tipos de movimiento |
| [MVP_CORE2_BACKLOG.md](./MVP_CORE2_BACKLOG.md) | Vista legada / histórico |
| ~~CHECKLIST_CORE2.md~~ | **Deprecado** |

Canónico dash: `dash_alejo_taller/.roadmap/Core2/`  
Support web Core 1: [../Core1/SUPPORT_CORE1_CLOSURE.md](../Core1/SUPPORT_CORE1_CLOSURE.md)
