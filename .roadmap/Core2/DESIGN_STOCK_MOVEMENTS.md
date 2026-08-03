# Core 2 — Diseño stock_movements

Extraído del diseño 2.1 original; **implementación diferida a Core 2**.

## Campos

| Campo | Tipo | Obligatorio | Notas |
|-------|------|-------------|-------|
| `$id` | string | sí | |
| `product_id` | string | sí | |
| `type` | enum | sí | entrada \| salida_venta \| ajuste \| devolucion |
| `quantity` | int | sí | Siempre > 0 |
| `balance_after` | int | sí | existence tras el movimiento |
| `reason` | string | sí | |
| `user_id` | string | sí | |
| `sale_id` | string? | no | |
| `created_at` | datetime | sí | |

## Tipos y efecto en existence

| type | existence |
|------|-----------|
| entrada | += quantity |
| salida_venta | -= quantity (al VERIFIED; alinear con soft-hold Core 1) |
| ajuste | in/out según direction |
| devolucion | += quantity |

## Quién escribe

Operador / admin. Clientes solo lectura. Preferible a medio plazo: Appwrite Function atómica junto al cambio de buy_state.

## Relación con Core 1 soft-hold

Core 1 ya muta `existence` y `reserved` sin traza en `stock_movements`.  
Core 2 añade la traza **sin cambiar** la semántica soft-hold (reserved sigue siendo la reserva de UNVERIFIED).
