# Appwrite — Collection stock_movements (Core 2)

## IDs canónicos (decisión 0.2)

| Recurso | ID propuesto | Estado |
|---------|--------------|--------|
| Database | el mismo `VITE_APPWRITE_DATABASE_ID` / `APPWRITE_DATABASE_ID` de Core 1 | ya en uso |
| Collection | **`stock_movements`** | **pendiente crear en consola** |
| Document `$id` | generado por Appwrite (o `{saleId}_{productId}` si se fuerza idempotencia) | — |

Convención alineada a colecciones ya usadas en web:

| Collection ID (código) | Uso |
|------------------------|-----|
| `product` | productos (`APPWRITE_COLLECTIONS.product`) |
| `sale` | ventas |
| `category` | categorías |
| `promotions` | promociones |
| `support_threads` / `support_messages` | soporte |
| **`stock_movements`** | Core 2 (este documento) |

Android/operador leen product/sale vía `BuildConfig.PRODUCT_TABLE_ID` / `SALE_TABLE_ID` (local.properties). Cuando exista la collection, añadir p. ej. `STOCK_MOVEMENTS_TABLE_ID=stock_movements` (o hardcode del ID canónico si se mantiene estable).

### Roles / permisos (MVP)

| Actor | create | read | update | delete |
|-------|--------|------|--------|--------|
| Cliente final | no | no | no | no |
| Operador / admin (team o role label) | sí | sí | no* | no* |
| API key server / Function (E.3) | sí | sí | según diseño | — |

\*MVP: documentos append-only; correcciones vía nuevo movimiento `ajuste`, no edit in-place.

---

## Atributos

| Atributo | Tipo | Required | Notas |
|----------|------|----------|-------|
| `product_id` | String | sí | |
| `type` | String | sí | entrada \| salida_venta \| ajuste \| devolucion |
| `quantity` | Integer | sí | > 0 |
| `balance_after` | Integer | sí | ≥ 0 |
| `reason` | String | sí | |
| `user_id` | String | sí | operador o system |
| `sale_id` | String | no | obligatorio en práctica para `salida_venta` |

Índices sugeridos: `product_id`, `type`, `sale_id`, `$createdAt`.

**Carga inicial:** no requiere backfill histórico; registrar desde el día de activación.

---

## Verificación 0.2

- [x] ID de collection **decidido**: `stock_movements`
- [x] Roles **decididos**: solo operador/admin (+ future Function); clientes sin acceso
- [ ] Collection **creada** en consola Appwrite del proyecto real
- [ ] IDs reales del proyecto anotados aquí si difieren del canónico (pegar databaseId + collectionId)

Cuando esté creada, completar:

```
databaseId: <VITE_APPWRITE_DATABASE_ID>
collectionId: stock_movements
createdAt: YYYY-MM-DD
```
