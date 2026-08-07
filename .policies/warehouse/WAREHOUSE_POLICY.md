# Política de almacén (Warehouse)

Documento de validación de inventario / stock para AlejoTaller.

Última actualización: 2026-08-07  
Ámbito: **Core 1 (MVP) + soft-hold + rollback + concurrencia + pulse stock**  
Relacionado: [SALE_POLICY](../sale/SALE_POLICY.md)

---

## 1. Principio general

> **El stock físico (`existence`) solo baja cuando el operador confirma la venta (`BuyState.VERIFIED`).**  
> **Al crear el pedido (`UNVERIFIED`) se aplica soft-hold: se incrementa `reserved`.**  
> **Al cancelar/rechazar se hace rollback: se decrementa `reserved`.**

---

## 2. Fuente de verdad

| Concepto | Dónde vive |
|----------|------------|
| Saldo físico | `Product.existence` (Appwrite + cache local) |
| Comprometido (soft-hold) | `Product.reserved` |
| Disponible para vender | `available = existence - reserved` |
| Idempotencia hold | `Sale.stock_hold_applied` |
| Señal realtime stock | Pusher canal `stock-updates` evento `stock:changed` |

---

## 3. Soft-hold / rollback / consume

| Evento | `existence` | `reserved` | Pulse `reason` |
|--------|-------------|------------|----------------|
| Cliente crea pedido (`UNVERIFIED`) | sin cambio | `+= quantity` | `hold` |
| Cliente **cancela** UNVERIFIED | sin cambio | `-= quantity` | `release` |
| Operador **confirma** (`VERIFIED`) | `-= quantity` | `-= quantity` | `consume` |
| Operador **rechaza** (`DELETED`) | sin cambio | `-= quantity` | `release` |

### Reglas

1. Cliente valida `quantity <= available` antes de crear.
2. Soft-hold re-lee el producto desde Appwrite justo antes de escribir (concurrencia).
3. Cola por `productId` en el mismo cliente serializa holds locales.
4. `stock_hold_applied = true` evita doble hold en reintentos.
5. `reserved >= 0` y `existence >= 0` siempre (clamp).
6. Tras hold/release/consume se emite `stock:changed` con `productIds[]`.
7. Clientes (web/Android) refrescan **solo esos ids** desde Appwrite → cache offline-first.

---

## 4. Contrato pulse `stock:changed`

```json
{
  "productIds": ["id1", "id2"],
  "reason": "hold" | "release" | "consume",
  "saleId": "optional",
  "timestamp": "ISO-8601"
}
```

- Canal: `stock-updates` (override `VITE_PUSHER_STOCK_CHANNEL`)
- Publicación preferida: `POST {ALSET_PULSE}/pulse/stock` con API key
- Suscripción: Pusher client en web; Android vía `StockUpdatesListener` + `RefreshProductsByIdsCaseUse`

---

## 5. Concurrencia

- No hay transacción distribuida en Core 1 (Appwrite Function atómica = Core 2).
- Mitigación: re-read remoto + validación `available` + cola local por producto.
- Si dos clientes compiten por el último cupo, el segundo falla en soft-hold con mensaje de stock insuficiente.
- Operador al confirmar hace clamp `existence/reserved >= 0`.

---

## 6. Implementación de referencia

| Superficie | Pieza |
|------------|--------|
| Web | `RegisterNewSaleCaseUse`, `ReleaseSoftHoldCaseUse`, `CancelUnverifiedSaleCaseUse`, `RefreshProductsByIdsCaseUse`, `stock-pulse.ts`, `subscribeStockUpdates` |
| Android cliente | `ApplySoftHoldCaseUse`, `ReleaseSoftHoldCaseUse`, `RefreshProductsByIdsCaseUse`, `ProductRepository.refreshFromRemote` |
| Operador | `ApplyOperatorStockDecisionCaseUse` (release/consume) |

---

## 7. Checklist

- [x] UNVERIFIED incrementa `reserved`
- [x] Cancel/DELETED libera `reserved` (rollback)
- [x] VERIFIED resta existence + reserved
- [x] Re-read remoto en soft-hold (concurrencia)
- [x] Señal `stock:changed` + refresh selectivo por ids
- [ ] Appwrite Function atómica hold (Core 2)
- [ ] StockMovement persistido (Core 2)
