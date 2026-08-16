# Core 2 — Modelo financiero (entradas + ventas)

**Fecha:** 2026-08-13  
**Estado:** **aceptado** (2026-08-13) · COGS = último costo · reservas de taller en MVP Core 2  
**Espejo en:** AlejoTaller (2026-08-16) · origen: `dash_alejo_taller/.roadmap/Core2/FINANCE_MODEL_CORE2.md`  
**No altera** soft-hold Core 1 (`existence` / `reserved` / `available`).

## Problema que resuelve

Core 1 solo contabiliza **unidades**. No hay costo de adquisición, proveedor/factura, margen al confirmar ni análisis COGS/beneficio. Core 2 añade la capa **económica** encima del stock.

## Principios

1. **Stock y dinero se mueven en momentos distintos pero enlazados.**  
   - Entrada → sube `existence` **y** registra costo.  
   - UNVERIFIED → solo `reserved`; **no** ingreso.  
   - VERIFIED → consume stock **y** reconoce ingreso / margen.  
   - DELETED → libera `reserved`; **no** ingreso ni COGS.

2. **UX canónica de entrada = factura de compra** (multi-línea), gobernada en **dash**.

3. Atajo “Dar entrada” individual puede existir, pero el camino económico es la factura.

## Flujo económico — Venta (operador AlejoTaller)

| Evento | Stock (Core 1) | Finanzas (Core 2) |
|--------|----------------|-------------------|
| Cliente UNVERIFIED | `reserved +=` | **Sin** ingreso ni COGS |
| Confirm VERIFIED | `existence -=`, `reserved -=` | **Ingreso** + **COGS** + margen; traza `salida_venta` |
| Reject DELETED | `reserved -=` | **Sin** movimiento financiero |
| Registrar entrada | `existence +=` | Costo en `purchase_entry` (+ líneas) |

**COGS MVP:** `last_unit_cost × qty` (por línea). Si no hay costo previo → `cogs = 0` + log/advertencia. **No** promedio móvil.

## Tipos de datos (Appwrite)

### `supplier`

| Campo | Tipo | Notas |
|-------|------|--------|
| name | string | obligatorio |
| contact | string? | |
| notes | string? | |

### `purchase_entry`

| Campo | Tipo | Notas |
|-------|------|--------|
| supplier_id | string? | null si regalía sin proveedor |
| reference | string? | nº factura |
| entry_date | datetime | |
| total_cost | number | suma líneas |
| currency | string | |
| user_id | string | staff |
| notes | string? | |
| line_count | int | |

### `purchase_entry_line`

| Campo | Tipo | Notas |
|-------|------|--------|
| entry_id | string | |
| product_id | string | |
| quantity | int | > 0 |
| unit_cost | number | ≥ 0 |
| concept | enum | `purchase` \| `royalty` \| `other` |
| line_cost | number | qty × unit_cost |

### `sale_finance_event` (recomendado)

`sale_id`, `revenue`, `cogs`, `margin`, `user_id`, `at`  
+ `salida_venta` en `stock_movements` solo para qty.

### Producto

| Campo | Uso |
|-------|-----|
| `last_unit_cost` | último costo de entrada compra; base COGS |

## Qué no es Core 2 finanzas

- Doble partida formal, impuestos, multi-moneda viva, FIFO/LIFO por lote, CxP proveedor.

## Relación con fases

| Fase | Encaje |
|------|--------|
| 2.0 | Modelo aceptado |
| 2.1 | Schema collections |
| 2.2 | Operador: finance event + `salida_venta` |
| 2.3 | Dash: UX factura |
| 2.4 | Dash: reportes margen |
| 2.6 | Roles viewer solo lectura |
