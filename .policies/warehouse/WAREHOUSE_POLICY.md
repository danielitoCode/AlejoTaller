# Política de almacén (Warehouse)

Documento de validación de inventario / stock para AlejoTaller.

Última actualización: 2026-08-02  
Ámbito: **Core 1 (MVP) + soft-hold**  
Relacionado: [SALE_POLICY](../sale/SALE_POLICY.md)

---

## 1. Principio general

> **El stock físico (`existence`) solo baja cuando el operador confirma la venta (`BuyState.VERIFIED`).**  
> **Al crear el pedido (`UNVERIFIED`) se aplica soft-hold: se incrementa `reserved`.**

La cantidad descontada / reservada es la **cantidad comprada de cada línea** (`SaleItem.quantity`),  
independientemente del tipo de venta (`NORMAL`, `DISCOUNT`, `GIFT`).

---

## 2. Fuente de verdad

| Concepto | Dónde vive |
|----------|------------|
| Saldo físico | `Product.existence` (Appwrite + cache local) |
| Comprometido (soft-hold) | `Product.reserved` |
| Disponible para vender | `available = existence - reserved` |
| Auditoría de movimientos | Colección / entidad `stock_movements` (`StockMovement`) |
| Idempotencia hold | `Sale.stock_hold_applied` |

---

## 3. Soft-hold (Core 1)

| Evento | `existence` | `reserved` |
|--------|-------------|------------|
| Cliente crea pedido (`UNVERIFIED`) | sin cambio | `+= quantity` por línea |
| Operador **confirma** (`VERIFIED`) | `-= quantity` | `-= quantity` (libera hold + consume físico) |
| Operador **rechaza** (`DELETED`) | sin cambio | `-= quantity` (libera hold) |

### Reglas soft-hold

1. Cliente valida `quantity <= available` antes de crear.
2. Tras persistir sale UNVERIFIED, cliente (o función backend) incrementa `reserved`.
3. `stock_hold_applied = true` evita doble hold en reintentos.
4. `reserved >= 0` y `existence >= 0` siempre.
5. `available` nunca se persiste: se calcula.

---

## 4. Cuándo se mueve el stock físico

| Evento | ¿Mueve `existence`? | Tipo de movimiento |
|--------|---------------------|--------------------|
| Cliente crea pedido (`UNVERIFIED`) | **No** (solo `reserved`) | — |
| Operador **confirma** (`VERIFIED`) | **Sí** | `SALIDA_VENTA` por cada ítem |
| Operador **rechaza** (`DELETED`) | **No** (solo libera `reserved`) | — |
| Entrada de mercancía (futuro / admin) | Sí | `ENTRADA` |
| Ajuste manual | Sí | `AJUSTE` |
| Devolución | Sí | `DEVOLUCION` |

### Regla de cantidad (VERIFIED)

Para una venta confirmada con ítems `[(P1, 2), (P2, 1)]`:

- `P1.existence -= 2` y `P1.reserved -= 2`
- `P2.existence -= 1` y `P2.reserved -= 1`

Si `SaleType = GIFT` o `DISCOUNT`: **misma resta**. El tipo solo afecta dinero, no unidades físicas.

---

## 5. Modelo `StockMovement`

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

## 6. Invariantes

1. `existence >= 0` y `reserved >= 0` siempre.
2. `available = existence - reserved >= 0`.
3. No confirmar venta si alguna línea dejaría `existence < 0`.
4. Un mismo `saleId` no debe generar **doble** `SALIDA_VENTA` ni **doble** soft-hold (`stock_hold_applied`).
5. Rechazo / `DELETED` libera `reserved` y no toca `existence`.
6. Cliente y visitante **nunca** escriben `stock_movements`.

---

## 7. Flujo operativo (Core 1)

```text
Cliente → Sale UNVERIFIED
          + reserved += qty (soft-hold)
          + stock_hold_applied = true
                ↓
Operador QR o código manual
                ↓
        ┌───────┴───────┐
        ↓               ↓
   VERIFIED          DELETED
   + SaleType         + reserved -= qty
   + existence -= qty
   + reserved -= qty
   + StockMovement SALIDA_VENTA
   + realtime sale:confirmed / rejected
```

---

## 8. Alineación de modelos (resumen)

| Modelo | Campos clave para almacén |
|--------|---------------------------|
| `Product` | `existence`, `reserved` |
| `Sale` | `verified`, `saleType`, `stockHoldApplied`, `products[]` |
| `SaleItem` | `productId`, `quantity`, `unitPrice` |
| `StockMovement` | `type=SALIDA_VENTA`, `saleId`, `quantity`, `balanceAfter` |

Appwrite (atributos esperados):

- products: `existence`, `reserved`
- sale: `sale_type`, `stock_hold_applied`

---

## 9. Implementación de referencia

- Web: `Product.ts` (`availableStock`), `RegisterNewSaleCaseUse` (soft-hold)
- Android: paridad pendiente
- Operador: al VERIFIED/DELETED ajustar `existence`/`reserved` + movimientos

Idealmente pasos atómicos a nivel de backend; en Core 1 se acepta secuencia ordenada con reintento idempotente por `saleId` / `stock_hold_applied`.

---

## 10. Checklist Core 1

- [x] UNVERIFIED incrementa `reserved` (soft-hold)
- [ ] VERIFIED resta `existence` y `reserved`
- [ ] DELETED resta solo `reserved`
- [x] Check cliente usa `available = existence - reserved`
- [ ] GIFT / DISCOUNT / NORMAL restan igual en confirmación
- [ ] Cada salida genera `StockMovement` con `saleId`
- [x] `stock_hold_applied` para idempotencia de hold
- [ ] `existence` / `reserved` no quedan negativos

---

## 11. Fuera de alcance Core 1

- Multi-almacén / ubicaciones
- Conteos cíclicos y cierre de inventario formal
- Devoluciones parciales automatizadas
- Appwrite Function atómica (recomendado Core 2)
