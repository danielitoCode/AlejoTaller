# Core 1 — Diseño soft-hold (existence / reserved / available)

**Derivado de:** diseño 2.1 original + decisión soft-hold Core 1.  
**Principio:** el cliente **reserva**; el operador **confirma o libera**. No hay auto-descuento al crear el pedido.

---

## Modelo mínimo Core 1

| Campo | Significado |
|-------|-------------|
| `existence` | Unidades físicas en almacén |
| `reserved` | Unidades comprometidas en pedidos UNVERIFIED |
| `available` | **Computado:** `existence - reserved` (nunca persistir negativo en lógica de negocio) |

```text
UNVERIFIED  → reserved += quantity   (si available >= quantity)
VERIFIED    → existence -= quantity; reserved -= quantity
DELETED     → reserved -= quantity   (existence intacto)
```

Idempotencia: flag `stock_hold_applied` (o equivalente) evita doble hold en reintentos.

---

## Responsabilidades

| Actor | Acción stock |
|-------|----------------|
| Cliente web / Android | Soft-check `qty <= available`; aplica hold al crear UNVERIFIED |
| Operador | Única escritura definitiva existence; release en rechazo |
| Visitante | No crea ventas ni holds |

---

## Paridad web ↔ Android

- Mismo contrato de campos en Product / DTO / mappers.
- Mismos casos de uso conceptuales: check existencia, apply soft-hold, operator stock decision.
- SaleType afecta **precio/monto**, no la cantidad de stock a mover.

---

## Fuera de Core 1

- Collection `stock_movements` y balance_after formal → Core 2
- `stock_min` / alertas → Core 2
- UI “quedan N unidades” en catálogo → Core 2
- Function atómica Appwrite confirm+stock → Core 2

Ver también: `.policies/warehouse/WAREHOUSE_POLICY.md`, `.policies/sale/SALE_POLICY.md`
