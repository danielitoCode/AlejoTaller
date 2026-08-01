# Política de ventas (Sale)

Documento de validación del dominio `sale` (cliente web/Android + operador `alejotallerscan`).

Última actualización: 2026-08-02  
Ámbito: **Core 1 (MVP)**

---

## 1. Principio general

> **La venta solo se cierra (confirmada o rechazada) en la aplicación de escaneo del operador.**

El cliente **solicita** (reserva / pedido).  
El operador **decide**: tomar (VERIFIED) o rechazar (DELETED).

No hay auto-confirmación en cliente en Core 1.

---

## 2. Ciclo de vida (`BuyState`)

| Estado | Quién lo pone | Significado |
|--------|---------------|-------------|
| **UNVERIFIED** | Cliente (web/Android) al registrar el pedido | Solicitud pendiente de atención en tienda |
| **VERIFIED** | Operador (`alejotallerscan`) al confirmar | Venta tomada / hecha |
| **DELETED** | Operador al rechazar | Solicitud descartada (no es venta) |

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

## 6. Efectos al confirmar / rechazar

### Al **VERIFIED**

1. Persistir `verified = VERIFIED` y `saleType`.
2. Aplicar **salida de stock** por cada ítem (`quantity`) — ver WAREHOUSE_POLICY.
3. Publicar realtime `sale:confirmed` (cliente actualiza UI).
4. Registrar historial local del operador.

### Al **DELETED** (rechazo)

1. Persistir `verified = DELETED`.
2. **No** mover stock.
3. Publicar realtime `sale:rejected`.
4. Registrar historial local del operador.

### Mientras **UNVERIFIED**

- **No** se reduce stock.
- El producto sigue vendible por otros clientes (Core 1: sin soft-hold de inventario).

---

## 7. Entrega y pago (contexto, no cambian el tipo)

- `DeliveryType`: PICKUP / DELIVERY — preferencia del cliente tras o durante el flujo.
- `PaymentChannel`: canales de cobro existentes (Transfermóvil, UltraPay, etc.).
- Independientes de `SaleType`: una venta DISCOUNT puede ser PICKUP + efectivo en tienda.

---

## 8. Implementación de referencia

### Dominio compartido
- `shared-sale/.../entity/Sale.kt` — `BuyState`, `SaleType`, `Sale`, `SaleItem`
- `web/.../sale/domain/entity/enums.ts` + `Sale.ts`

### Operador
- Escaneo: `alejotallerscan/.../scan/`
- Confirmación: `.../confirmation/`
- Decisión + realtime: `OperatorSalesViewModel`, `NotifyOperatorSaleDecisionCaseUse`

### Cliente
- Alta UNVERIFIED: `RegisterNewSaleCauseUse` / web equivalente
- Reacción realtime: `InterpretSaleRealtimeEventCaseUse`

---

## 9. Checklist Core 1

- [ ] Cliente crea reserva → `UNVERIFIED`, sin baja de stock
- [ ] Operador carga por QR o código manual
- [ ] Confirmar → `VERIFIED` + `SaleType` (NORMAL | DISCOUNT | GIFT)
- [ ] Confirmar → baja de stock = suma de quantities por producto
- [ ] Rechazar → `DELETED`, stock intacto
- [ ] GIFT con amount 0 sigue bajando stock
- [ ] Realtime llega a cliente web y Android
- [ ] Visitante no puede crear venta

---

## 10. Fuera de alcance Core 1

- Soft-reservation de stock al crear UNVERIFIED
- Multi-operador con locks optimistas avanzados
- Devoluciones parciales de línea
- Facturación fiscal formal
