# Appwrite — Schema stock (Fase 2.2)

## 1. Colección / atributos en `products`

| Atributo | Tipo | Required | Default | Notas |
|----------|------|----------|---------|-------|
| `existence` | Integer | sí (recomendado) | `0` | Stock disponible. Ya usado por web. |

**Acción manual en consola Appwrite:**
1. Abrir collection de productos.
2. Si falta `existence`, crear atributo Integer, min 0, default 0.
3. Backfill: poner valores reales del inventario físico (script o dashboard).

## 2. Nueva collection `stock_movements`

| Atributo | Tipo | Required | Notas |
|----------|------|----------|-------|
| `product_id` | String | sí | Id del producto |
| `type` | String (enum) | sí | `entrada` \| `salida_venta` \| `ajuste` \| `devolucion` |
| `quantity` | Integer | sí | Siempre > 0 |
| `balance_after` | Integer | sí | existence tras el movimiento (≥ 0) |
| `reason` | String | sí | Motivo |
| `user_id` | String | sí | Actor |
| `sale_id` | String | no | Pedido asociado |

Índices sugeridos: `product_id`, `type`, `sale_id`, `$createdAt`.

**Permisos (MVP):**
- Lectura: roles autenticados de operador/admin.
- Escritura: solo operador/admin (no clientes finales).

## 3. Carga inicial de stock (2.2.3)

Opciones:
1. Dashboard admin: editar `existence` por producto.
2. Script one-shot (Node) con Appwrite SDK: listar productos + update `existence`.
3. CSV import si el dashboard lo soporta.

No hay script automatizado en el monorepo aún; la verdad operativa es el conteo físico del taller.

## 4. Regla de negocio

Ninguna escritura debe dejar `existence < 0`. Validar en dominio (Android/web) y en el flujo de descuento del operador (Fase 3).
