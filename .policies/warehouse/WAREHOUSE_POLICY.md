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

**Regla Core 1:** las mutaciones críticas de stock no pueden usar Room/Dexie como autoridad. El cliente puede leer desde la cache para UI, pero antes de reservar debe revalidar desde Appwrite y la escritura de `reserved` debe ejecutarse mediante la operación numérica atómica de Appwrite.

---

## 3. Soft-hold / rollback / consume

| Evento | `existence` | `reserved` | Pulse `reason` |
|--------|-------------|------------|----------------|
| Cliente crea pedido (`UNVERIFIED`) | sin cambio | `+= quantity` atómico | `hold` |
| Cliente **cancela** UNVERIFIED | sin cambio | `-= quantity` atómico | `release` |
| Operador **confirma** (`VERIFIED`) | `-= quantity` | `-= quantity` atómico | `consume` |
| Operador **rechaza** (`DELETED`) | sin cambio | `-= quantity` atómico | `release` |

### Reglas

1. Cliente valida `quantity <= available` antes de crear.
2. Soft-hold re-lee el producto desde Appwrite justo antes de escribir.
3. La mutación de `reserved` no usa read-modify-write: Appwrite ejecuta el incremento/decremento numérico de forma atómica.
4. El incremento de `reserved` usa `existence` remoto observado como límite máximo de la operación; esto impide que reservas concurrentes superen ese límite bajo la misma versión de `existence`.
5. El decremento de `reserved` usa límite mínimo `0`.
6. Cola por `productId` en el mismo cliente serializa holds locales, pero **no sustituye** la concurrencia del servidor entre clientes.
7. `stock_hold_applied = true` evita doble hold en reintentos cuando el indicador persistido se actualizó correctamente.
8. `reserved >= 0` y `existence >= 0` siempre.
9. Tras hold/release/consume se emite `stock:changed` con `productIds[]`.
10. Clientes (web/Android) refrescan **solo esos ids** desde Appwrite → cache offline-first.
11. Pusher es únicamente señalizador/invalidation; nunca aplica una mutación de stock.
12. Si Appwrite no está disponible durante una mutación crítica, la operación falla; no se autoriza la reserva usando una copia local.
13. El soft-hold multi-producto debe compensar las líneas confirmadas si una línea posterior falla.

### Compensación de soft-hold multi-producto

Cuando una venta contiene múltiples líneas de productos, el soft-hold se trata como una operación lógica todo-o-nada dentro de Core 1 mediante compensación:

```text
Producto A → hold atómico OK
Producto B → hold atómico OK
Producto C → ERROR

        ↓ compensación

Producto A → release atómico
Producto B → release atómico
Producto C → sin cambios

Resultado: ningún hold parcial confirmado por el intento.
```

La compensación se ejecuta únicamente sobre las cantidades cuyas reservas fueron confirmadas como aplicadas durante ese intento. No se libera una línea que no haya sido confirmada como aplicada.

Si la compensación también falla, se registra trazabilidad del fallo para reconciliación posterior; `stock_hold_applied` **no debe marcarse como `true`** salvo que todas las líneas hayan quedado correctamente reservadas.

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

- Core 1 **no usa transacciones de negocio distribuidas**; la transacción multi-entidad de Warehouse queda para Core 2.
- Core 1 sí utiliza operaciones numéricas atómicas de Appwrite para evitar lost updates sobre `reserved` y aplicar límites `max/min` por operación.
- Re-read remoto + validación `available` + operación atómica constituyen la mitigación de concurrencia del Core 1.
- La cola local por `productId` solo protege la concurrencia dentro de una instancia del cliente.
- Si `existence` cambia concurrentemente entre la relectura y la reserva, Core 1 no garantiza una invariancia transaccional entre ambas columnas; esa garantía corresponde a Core 2.
- En ventas con múltiples productos, un fallo durante el soft-hold requiere compensar todas las líneas ya confirmadas antes de considerar fallido el intento.

---

## 6. Implementación de referencia

| Superficie | Pieza |
|------------|--------|
| Web | `RegisterNewSaleCaseUse`, `ReleaseSoftHoldCaseUse`, `CancelUnverifiedSaleCaseUse`, `RefreshProductsByIdsCaseUse`, `product.net.repository.ts`, `product.offline-first.repository.ts`, `stock-pulse.ts` |
| Android cliente | `ApplySoftHoldCaseUse`, `ReleaseSoftHoldCaseUse`, `RefreshProductsByIdsCaseUse`, `ProductNetRepositoryImpl`, `ProductOfflineFirstRepository` |
| Operador | `ApplyOperatorStockDecisionCaseUse` (release/consume) |

---

## 7. Checklist

- [x] UNVERIFIED incrementa `reserved`
- [x] Incremento de `reserved` usa operación atómica Appwrite
- [x] Incremento atómico usa `existence` como máximo observado
- [x] Cancel/DELETED libera `reserved` mediante decremento atómico con mínimo 0
- [x] VERIFIED resta existence + reserved
- [x] Re-read remoto en soft-hold (concurrencia)
- [x] No se usa cache local como autoridad para una mutación crítica
- [x] Soft-hold multi-producto compensa líneas confirmadas ante fallo parcial
- [x] Señal `stock:changed` + refresh selectivo por ids
- [ ] Appwrite Function transaccional de Warehouse (Core 2)
- [ ] StockMovement persistido (Core 2)

> Model: GPT-5.6 Luna  
> Fecha: 2026-08-07
