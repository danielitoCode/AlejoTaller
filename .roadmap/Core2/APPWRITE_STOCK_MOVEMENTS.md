# Appwrite — Collection stock_movements (Core 2)

## IDs canónicos (decisión 0.2)

| Recurso | ID propuesto | Estado |
|---------|--------------|--------|
| Database | el mismo `VITE_APPWRITE_DATABASE_ID` / `APPWRITE_DATABASE_ID` de Core 1 | ya en uso |
| Collection | **`stock_movements`** | **creada en consola** (2026-08-18, confirmado equipo) |
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
| **`supplier`** | Core 2 finanzas entrada |
| **`purchase_entry`** / **`purchase_entry_line`** | Core 2 factura entrada |
| **`sale_finance_event`** | Core 2 ingreso/COGS al VERIFIED |

Android/operador: añadir `STOCK_MOVEMENTS_TABLE_ID=stock_movements` (o hardcode del ID canónico) al cablear repos en bloque B1.

### Roles / permisos (MVP) — **aplicados en Appwrite**

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
| `entry_id` | String | no | enlace a purchase_entry si aplica |

Índices: `product_id`, `type`, `sale_id`, `$createdAt`.

**Carga inicial:** no requiere backfill histórico; registrar desde el día de activación.

---

## Verificación 0.2 / 2.1 schema

- [x] ID de collection **decidido**: `stock_movements`
- [x] Roles **decididos** y **aplicados**: solo operador/admin (+ future Function); clientes sin acceso
- [x] Collection **creada** en consola Appwrite del proyecto real (2026-08-18)
- [x] Colecciones hermanas: `supplier`, `purchase_entry`, `purchase_entry_line`, `sale_finance_event`, `last_unit_cost` en product
- [ ] IDs reales del proyecto anotados aquí si difieren del canónico (pegar databaseId + collectionId)

Cuando se anoten IDs reales:

```
databaseId: <VITE_APPWRITE_DATABASE_ID>
collectionId: stock_movements
createdAt: 2026-08-18
```
