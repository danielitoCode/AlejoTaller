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
8. **Soft-hold multi-producto debe ser compensatorio:** si una venta contiene varios productos y el proceso de hold falla después de aplicar reservas a una o más líneas, se deben liberar (`reserved -= quantity`) todas las reservas aplicadas previamente para esa misma venta antes de considerar el hold fallido. No debe quedar una venta parcialmente reservada ni reservas huérfanas por un fallo intermedio.

### Compensación de soft-hold multi-producto

Cuando una venta contiene múltiples líneas de productos, el soft-hold debe tratarse como una operación lógica todo-o-nada dentro de Core 1:

```text
Producto A → hold OK
Producto B → hold OK
Producto C → ERROR

        ↓ compensación

Producto A → release
Producto B → release
Producto C → sin cambios

Resultado: ningún hold parcial de la venta.
```

La compensación debe ejecutarse únicamente sobre los `productIds` cuyas reservas hayan sido confirmadas como aplicadas durante ese intento. Debe ser segura ante reintentos y no debe liberar reservas pertenecientes a otra venta.

Si la compensación también falla, el sistema debe dejar trazabilidad del estado parcial para permitir reconciliación posterior; `stock_hold_applied` **no debe marcarse como `true`** salvo que todas las líneas hayan quedado correctamente reservadas.

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
- En ventas con múltiples productos, un fallo durante el soft-hold requiere compensar todas las líneas ya aplicadas antes de considerar fallido el intento.

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
- [ ] Compensación de soft-hold multi-producto ante fallo parcial
- [ ] Appwrite Function atómica hold (Core 2)
- [ ] StockMovement persistido (Core 2)

> (Model: GPT-5.6 Luna__fecha: 2026-08-07)
