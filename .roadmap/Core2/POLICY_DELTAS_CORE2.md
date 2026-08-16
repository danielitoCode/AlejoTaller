# Core 2 — Deltas de política (respecto a Core 1)

**Fecha:** 2026-08-13 (origen dash) · espejo AlejoTaller 2026-08-16  
**Canónico ecosistema:** `AlejoTaller/.policies/warehouse`, `AlejoTaller/.policies/sale`  
**Origen:** `dash_alejo_taller/.roadmap/Core2/POLICY_DELTAS_CORE2.md`

## Congelado (no negociable en Core 2)

| Regla | Detalle |
|-------|---------|
| Soft-hold | `available = max(0, existence − reserved)` |
| UNVERIFIED | Solo cliente; `reserved += qty`; `existence` intacto |
| VERIFIED | `existence -= qty` y `reserved -= qty` |
| DELETED / reject | Solo `reserved -= qty` |
| Panel no crea B2C | Sin ventas UNVERIFIED desde el dash |
| `reserved` no se edita a mano | Solo vía flujo de venta o release |

## Añadido en Core 2

### 1. `stock_movements` (traza)

| type | Efecto en `existence` | Quién |
|------|----------------------|--------|
| `entrada` | `+= quantity` | staff (dash / operador admin) |
| `salida_venta` | `-= quantity` | al VERIFIED (operador **primario** en AlejoTaller) |
| `ajuste` | ± según dirección | staff; post-ajuste `existence ≥ reserved` |
| `devolucion` | `+= quantity` | staff; solo post-VERIFIED |

Campos mínimos: `product_id`, `type`, `quantity` (>0), `balance_after`, `reason`, `user_id`, `sale_id?`, `created_at`.

### 2. Devolución formal

- Solo sobre venta **VERIFIED** (o línea ya consumida).  
- `existence += qty` + movimiento `devolucion` + motivo obligatorio.  
- **No** reabrir el soft-hold de esa venta.

### 3. Ajuste de inventario

- Motivo + `user_id` obligatorios.  
- Validación: tras el ajuste, `existence >= reserved`.  
- Preferible escribir siempre fila en `stock_movements`.

### 4. Reservas de taller (MVP Core 2)

- Dominio **aparte** de `Sale` (p. ej. collection `appointment` / `booking`).  
- Estados típicos: solicitada → confirmada → realizada / cancelada.  
- **No** listar pedidos de tienda en el menú Reservas.  
- Stock de piezas en cita: **fuera** del MVP mínimo.  
- **Incluidas en el núcleo** (decisión 2026-08-13).

## Competencias por superficie (Core 2)

| Acción | Cliente (web/Android) | Operador (`alejotallerscan`) | Dash |
|--------|------------------------|------------------------------|------|
| Soft-hold al pedir | Sí | No | No |
| Confirm/reject + `salida_venta` | No | **Primario** | Secundario |
| Entrada / ajuste / devolución | No | Sí (si se expone) | **Sí** |
| Ver movimientos | No | Lectura | **Sí** |
| Agenda reservas taller | Solicitar (futuro/web) | Operar | **Gobernar** |

## 5. Finanzas (Core 2) — aceptado 2026-08-13

| Evento | Stock | Dinero |
|--------|-------|--------|
| Registrar entrada (factura) | `existence +=` | Costo en `purchase_entry` (+ líneas) |
| UNVERIFIED | `reserved +=` | **Sin** ingreso |
| VERIFIED | consume existence/reserved | **Ingreso + COGS + margen** |
| DELETED | release reserved | **Sin** ingreso |

- COGS: `last_unit_cost × qty` al VERIFIED. **No promedio.**
- Detalle: [`FINANCE_MODEL_CORE2.md`](./FINANCE_MODEL_CORE2.md)

## 6. Reservas de taller (MVP Core 2)

- Incluidas en el núcleo (decisión 2026-08-13).
- Collection separada de `Sale`; menú Reservas ≠ Ventas pendientes.
