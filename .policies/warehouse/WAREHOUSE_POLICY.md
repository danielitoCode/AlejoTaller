# Política de almacén (Warehouse)

Documento de validación de inventario / stock para AlejoTaller.

Última actualización: 2026-08-02  
Ámbito: **Core 1 (MVP) + soft-hold + tests parciales**  
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
| Auditoría de movimientos | Colección / entidad `stock_movements` (`StockMovement`) — Core 2 |
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
2. Tras persistir sale UNVERIFIED, cliente incrementa `reserved`.
3. `stock_hold_applied = true` evita doble hold en reintentos.
4. `reserved >= 0` y `existence >= 0` siempre (clamp en escritura).
5. `available` nunca se persiste: se calcula.

---

## 4. Cuándo se mueve el stock físico

| Evento | ¿Mueve `existence`? | Tipo de movimiento |
|--------|---------------------|--------------------|
| Cliente crea pedido (`UNVERIFIED`) | **No** (solo `reserved`) | — |
| Operador **confirma** (`VERIFIED`) | **Sí** | `SALIDA_VENTA` (Core 2 colección) |
| Operador **rechaza** (`DELETED`) | **No** (solo libera `reserved`) | — |

### Regla de cantidad (VERIFIED)

Para una venta confirmada con ítems `[(P1, 2), (P2, 1)]`:

- `P1.existence -= 2` y `P1.reserved -= 2`
- `P2.existence -= 1` y `P2.reserved -= 1`

Si `SaleType = GIFT` o `DISCOUNT`: **misma resta**. El tipo solo afecta dinero.

---

## 5. Invariantes

1. `existence >= 0` y `reserved >= 0` siempre.
2. `available = existence - reserved >= 0`.
3. No confirmar si ya no está UNVERIFIED (guard en operador).
4. Un mismo `saleId` no debe generar doble soft-hold (`stock_hold_applied`) ni doble transición de estado.
5. Rechazo / `DELETED` libera `reserved` y no toca `existence`.
6. Cliente **nunca** escribe `stock_movements`; operador escribe existence/reserved.

---

## 6. Flujo operativo (Core 1)

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
   + existence -= qty  + reserved -= qty
   + reserved -= qty
   + realtime
```

---

## 7. Implementación de referencia

| Superficie | Pieza |
|------------|--------|
| Web cliente | `RegisterNewSaleCaseUse` soft-hold; `availableStock()` |
| Android cliente | `ApplySoftHoldCaseUse`; `CheckAProductExistenceCaseUse` |
| Operador | `ApplyOperatorStockDecisionCaseUse` + `AppwriteOperatorStockRepository` en `OperatorSalesViewModel` |

---

## 8. Tests parciales (Core 1)

| Suite | Qué valida |
|-------|------------|
| Web `check-a-product-existence.case.use.test.ts` | available = existence − reserved |
| Web `RegisterNewSaleCaseUse.soft-hold.test.ts` | reserved += qty, insuficiencia |
| Android `SoftHoldCaseUseTest` | availableStock, check, hold, idempotencia |

---

## 9. Checklist Core 1

- [x] UNVERIFIED incrementa `reserved` (web + Android)
- [x] VERIFIED resta `existence` y `reserved` (operador)
- [x] DELETED resta solo `reserved` (operador)
- [x] Check cliente usa `available = existence - reserved`
- [x] GIFT / DISCOUNT / NORMAL restan igual en confirmación
- [x] `stock_hold_applied` para idempotencia de hold
- [x] `existence` / `reserved` no quedan negativos (clamp)
- [x] Tests parciales soft-hold / available (web + Android cliente)
- [ ] Cada salida genera `StockMovement` con `saleId` (Core 2 / colección)
- [ ] Tests automatizados operador confirm/reject — QA / Core 2

---

## 10. Fuera de alcance Core 1

- Colección `stock_movements` persistida
- Multi-almacén / ubicaciones
- Conteos cíclicos
- Appwrite Function atómica (recomendado Core 2)
