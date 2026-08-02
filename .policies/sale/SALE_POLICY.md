# Política de ventas (Sale)

Documento de validación del dominio `sale` (cliente web/Android + operador `alejotallerscan`).

Última actualización: 2026-08-02  
Ámbito: **Core 1 (MVP) + soft-hold**

---

## 1. Principio general

> **La venta solo se cierra (confirmada o rechazada) en la aplicación de escaneo del operador.**

El cliente **solicita** (pedido + soft-hold de inventario).  
El operador **decide**: tomar (VERIFIED) o rechazar (DELETED).

No hay auto-confirmación en cliente en Core 1.

---

## 2. Ciclo de vida (`BuyState`)

| Estado | Quién lo pone | Significado | Stock |
|--------|---------------|-------------|-------|
| **UNVERIFIED** | Cliente (web/Android) al registrar el pedido | Solicitud pendiente | soft-hold: `reserved += qty` |
| **VERIFIED** | Operador (`alejotallerscan`) al confirmar | Venta tomada / hecha | `existence -= qty`, `reserved -= qty` |
| **DELETED** | Operador al rechazar | Solicitud descartada | libera hold: `reserved -= qty` |

### Canales de atención del operador (Core 1)

1. **Escaneo QR** — canal en vivo (código de la reserva en pantalla del cliente).
2. **Entrada manual** — código / id de reserva tecleado o buscado.

Ambos caminos llegan a la misma decisión: **confirmar** o **rechazar**.

---

## 3. Tipos de venta (`SaleType`)

Definidos en dominio compartido. Afectan **precio / importe**, no el movimiento de stock.

| Tipo | Código | Descripción | Importe (`amount`) |
|------|--------|-------------|--------------------|
| **Venta normal** | `NORMAL` | Precio de lista del catálogo | Suma de `unitPrice × quantity` |
| **Venta con descuento** | `DISCOUNT` | Alineado con dueño o promoción de tienda | Menor que lista; se registra el precio efectivo |
| **Regalia / obsequio** | `GIFT` | Entrega sin cobro comercial | `0` (o nominal 0 por línea) |

### Reglas de tipo

- El **tipo se fija en la confirmación del operador** (Core 1), no por el cliente.
- Una solicitud UNVERIFIED no tiene aún `SaleType` definitivo (o se asume `NORMAL` provisional hasta confirmar).
- Al pasar a **VERIFIED**, el operador elige el tipo y, si aplica, el monto/descuento final.
- **GIFT** y **DISCOUNT** no eximen de baja de inventario (ver WAREHOUSE_POLICY).

---

## 4. Roles

| Actor | Puede crear UNVERIFIED | Puede VERIFIED / DELETED | Puede elegir SaleType |
|-------|------------------------|--------------------------|------------------------|
| Cliente (auth o visitor→login para comprar) | Sí | No | No |
| Operador (`alejotallerscan`) | No (salvo flujos futuros) | **Sí** | **Sí** |
| Visitante sin cuenta | No (solo catálogo) | No | No |

---

## 5. Ítems de venta (`SaleItem`)

Cada línea debe poder reconstruir contabilidad y stock:

| Campo | Uso |
|-------|-----|
| `productId` | Identidad del producto |
| `quantity` | Unidades (≥ 1) |
| `productName` | Snapshot legible |
| `unitPrice` | Precio unitario **efectivo** al cerrar (lista, descontado o 0 si GIFT) |
| `listUnitPrice` (opcional) | Precio de lista al momento (auditoría de descuento) |

`Sale.amount` = suma de `unitPrice × quantity` (0 en GIFT completo).

---

## 6. Soft-hold e idempotencia

| Campo | Uso |
|-------|-----|
| `Product.reserved` | Unidades comprometidas en UNVERIFIED |
| `available` | `existence - reserved` (calculado, no persistido) |
| `Sale.stockHoldApplied` / `stock_hold_applied` | Evita doble incremento de `reserved` |

Al crear UNVERIFIED el cliente:

1. Soft-check `quantity <= available`
2. Persiste sale
3. Aplica hold (`reserved += qty`) y marca `stock_hold_applied`

---

## 7. Efectos al confirmar / rechazar

### Al **VERIFIED**

1. Persistir `verified = VERIFIED` y `saleType`.
2. Por cada ítem: `existence -= qty`, `reserved -= qty`.
3. Registrar `StockMovement` SALIDA_VENTA.
4. Publicar realtime `sale:confirmed`.

### Al **DELETED** (rechazo)

1. Persistir `verified = DELETED`.
2. Por cada ítem: `reserved -= qty` (liberar hold).
3. **No** tocar `existence`.
4. Publicar realtime `sale:rejected`.

### Mientras **UNVERIFIED**

- Soft-hold activo: reduce `available` para otros clientes.
- No se reduce `existence`.

---

## 8. Entrega y pago (contexto, no cambian el tipo)

- `DeliveryType`: PICKUP / DELIVERY — preferencia del cliente tras o durante el flujo.
- `PaymentChannel`: canales de cobro existentes (Transfermóvil, UltraPay, etc.).
- Independientes de `SaleType`.

---

## 9. Implementación de referencia

### Dominio
- `web/.../sale/domain/entity/enums.ts` + `Sale.ts` (`saleType`, `stockHoldApplied`)
- `web/.../product/domain/entity/Product.ts` (`reserved`, `availableStock`)
- `RegisterNewSaleCaseUse` — create + soft-hold

### Operador
- Escaneo / confirmación: ajustar stock físico + liberar reserved

### Cliente
- Alta UNVERIFIED + hold
- Reacción realtime: UI; stock se sincroniza vía catálogo

---

## 10. Checklist Core 1

- [x] Cliente crea reserva → `UNVERIFIED` + soft-hold
- [x] Check usa `available`
- [ ] Operador carga por QR o código manual
- [ ] Confirmar → `VERIFIED` + `SaleType` + baja existence/reserved
- [ ] Rechazar → `DELETED` + libera reserved
- [ ] GIFT con amount 0 sigue bajando stock al confirmar
- [ ] Realtime llega a cliente web y Android
- [x] Visitante no puede crear venta

---

## 11. Fuera de alcance Core 1

- Multi-operador con locks optimistas avanzados
- Devoluciones parciales de línea
- Facturación fiscal formal
- Function atómica Appwrite (Core 2 recomendado)
