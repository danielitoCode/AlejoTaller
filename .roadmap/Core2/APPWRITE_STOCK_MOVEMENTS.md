# Appwrite — Collection stock_movements (Core 2)

| Atributo | Tipo | Required | Notas |
|----------|------|----------|-------|
| `product_id` | String | sí | |
| `type` | String | sí | entrada \| salida_venta \| ajuste \| devolucion |
| `quantity` | Integer | sí | > 0 |
| `balance_after` | Integer | sí | ≥ 0 |
| `reason` | String | sí | |
| `user_id` | String | sí | |
| `sale_id` | String | no | |

Índices sugeridos: `product_id`, `type`, `sale_id`, `$createdAt`.

**Permisos MVP Core 2:** lectura y escritura operador/admin; no clientes finales.

**Carga inicial:** no requiere backfill histórico obligatorio; empezar a registrar desde el día de activación.
