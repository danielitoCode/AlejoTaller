# Política de almacén (Warehouse)

Documento de validación de inventario / stock para AlejoTaller.

Última actualización: 2026-08-02  
Ámbito: **Core 1 (MVP)**  
Relacionado: [SALE_POLICY](../sale/SALE_POLICY.md)

---

## 1. Principio general

> **El stock solo baja cuando el operador confirma la venta (`BuyState.VERIFIED`).**

La cantidad descontada es la **cantidad comprada de cada línea** (`SaleItem.quantity`),  
independientemente del tipo de venta (`NORMAL`, `DISCOUNT`, `GIFT`).

---

## 2. Fuente de verdad

| Concepto | Dónde vive |
|----------|------------|
| Saldo actual del producto | `Product.existence` (Appwrite + cache local) |
| Auditoría de movimientos | Colección / entidad `stock_movements` (`StockMovement`) |

Todo cambio de existencia por venta debe dejar un movimiento trazable.

---

## 3. Cuándo se mueve el stock

| Evento | ¿Mueve stock? | Tipo de movimiento |
|--------|---------------|--------------------|
| Cliente crea pedido (`UNVERIFIED`) | **No** | — |
| Operador **confirma** (`VERIFIED`) | **Sí** | `SALIDA_VENTA` por cada ítem |
| Operador **rechaza** (`DELETED`) | **No** | — |
| Entrada de mercancía (futuro / admin) | Sí | `ENTRADA` |
| Ajuste manual | Sí | `AJUSTE` |
| Devolución | Sí | `DEVOLUCION` |

### Regla de cantidad

Para una venta confirmada con ítems `[(P1, 2), (P2, 1)]`:

- `P1.existence -= 2`
- `P2.existence -= 1`

Si `SaleType = GIFT` o `DISCOUNT`: **misma resta**. El tipo solo afecta dinero, no unidades físicas.

---

## 4. Modelo `StockMovement`

```text
id, productId, type, quantity (>0), balanceAfter (>=0),
reason, userId (operador), saleId?, createdAtIso
```

| `StockMovementType` | Uso Core 1 |
|---------------------|------------|
| `SALIDA_VENTA` | Confirmación de venta en operador |
| `ENTRADA` | Reposición (admin / fase posterior) |
| `AJUSTE` | Corrección de inventario |
| `DEVOLUCION` | Post-venta (fuera de Core 1 estricto) |

Para `SALIDA_VENTA`:

- `saleId` **obligatorio**
- `quantity` = `SaleItem.quantity` de esa línea
- `userId` = id del operador que confirmó
- `reason` ejemplo: `"Venta confirmada NORMAL|DISCOUNT|GIFT"`
- `balanceAfter` = existencia resultante **después** de restar

---

## 5. Invariantes

1. `existence >= 0` siempre.
2. No confirmar venta si alguna línea dejaría `existence < 0` (Core 1: rechazar confirmación o avisar al operador).
3. Un mismo `saleId` no debe generar **doble** `SALIDA_VENTA` (idempotencia en confirmación).
4. Rechazo / `DELETED` no crea movimientos ni revierte nada (porque nunca salió stock).
5. Cliente y visitante **nunca** escriben `stock_movements`.

---

## 6. Flujo operativo (Core 1)

```text
Cliente → Sale UNVERIFIED (sin stock)
                ↓
Operador QR o código manual
                ↓
        ┌───────┴───────┐
        ↓               ↓
   VERIFIED          DELETED
   + SaleType         (fin)
   + por cada ítem:
       existence -= qty
       StockMovement SALIDA_VENTA
   + realtime sale:confirmed
```

---

## 7. Alineación de modelos (resumen)

| Modelo | Campos clave para almacén |
|--------|---------------------------|
| `Product` | `existence: Int` |
| `Sale` | `verified`, `saleType`, `products[]` |
| `SaleItem` | `productId`, `quantity`, `unitPrice` |
| `StockMovement` | `type=SALIDA_VENTA`, `saleId`, `quantity`, `balanceAfter` |

---

## 8. Implementación de referencia

- `app/.../product/domain/entity/Product.kt` — `existence`
- `app/.../product/domain/entity/StockMovement.kt`
- Confirmación operador: al marcar VERIFIED, en el mismo caso de uso / transacción lógica:
  1. actualizar sale
  2. restar existence
  3. persistir movimientos
  4. notificar realtime

Idealmente pasos 1–3 atómicos a nivel de backend; en Core 1 se acepta secuencia ordenada con reintento idempotente por `saleId`.

---

## 9. Checklist Core 1

- [ ] UNVERIFIED no modifica `existence`
- [ ] VERIFIED resta `quantity` por línea
- [ ] GIFT / DISCOUNT / NORMAL restan igual
- [ ] DELETED no toca stock
- [ ] Cada salida genera `StockMovement` con `saleId`
- [ ] No hay doble descuento al re-confirmar el mismo id
- [ ] `existence` no queda negativo

---

## 10. Fuera de alcance Core 1

- Reservas de stock (hold) al crear el pedido
- Multi-almacén / ubicaciones
- Conteos cíclicos y cierre de inventario formal
- Devoluciones parciales automatizadas
