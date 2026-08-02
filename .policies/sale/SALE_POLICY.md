# Política de ventas (Sale)

Documento de validación del dominio `sale` (cliente web/Android + operador `alejotallerscan`).

Última actualización: 2026-08-02  
Ámbito: **Core 1 (MVP) + soft-hold + SaleType + importe efectivo + tests parciales**

---

## 1. Principio general

> **La venta solo se cierra (confirmada o rechazada) en la aplicación de escaneo del operador.**

El cliente **solicita** (pedido + soft-hold de inventario).  
El operador **decide**: tomar (VERIFIED) o rechazar (DELETED), elige el **tipo de venta** y, si aplica, el **importe efectivo**.

No hay auto-confirmación en cliente en Core 1.

---

## 2. Ciclo de vida (`BuyState`)

| Estado | Quién lo pone | Significado | Stock |
|--------|---------------|-------------|-------|
| **UNVERIFIED** | Cliente (web/Android) | Solicitud pendiente | soft-hold: `reserved += qty` |
| **VERIFIED** | Operador | Venta tomada | `existence -= qty`, `reserved -= qty` |
| **DELETED** | Operador | Descartada | libera hold: `reserved -= qty` |

### Canales de atención del operador

1. **Escaneo QR**
2. **Entrada manual** (código / id)

---

## 3. Tipos de venta (`SaleType`) — operador

Afectan **precio / importe**, no el movimiento de stock.

| Tipo | Código | Importe al confirmar (`amount`) | Stock |
|------|--------|----------------------------------|-------|
| Normal | `NORMAL` | precio de lista del pedido | baja |
| Descuento | `DISCOUNT` | **importe efectivo** ingresado por el operador (`0 ≤ amount < lista`) | baja |
| Regalia | `GIFT` | **0** | baja |

### UI operador (Core 1)

En pantalla de confirmación:

1. Chips de tipo: **Normal** (default) / **Descuento** / **Regalia**
2. Si **Descuento**: campo numérico de **importe efectivo** (sugerido ~90% del lista; validación: `>= 0` y `< amount` de lista)
3. Badge de lista vs efectivo
4. Al confirmar se persiste `sale_type` + `amount` efectivo + `buy_state = VERIFIED`

---

## 4. Soft-hold e idempotencia

| Campo | Uso |
|-------|-----|
| `Product.reserved` | Comprometido en UNVERIFIED |
| `available` | `existence - reserved` |
| `Sale.stockHoldApplied` | Evita doble hold |

---

## 5. Efectos al confirmar / rechazar

### VERIFIED
1. `verified = VERIFIED` + `saleType` elegido.
2. `amount` según tipo (lista / descuento / 0).
3. Por ítem: `existence -= qty`, `reserved -= qty`.
4. Realtime `sale:confirmed`.

### DELETED
1. `verified = DELETED`.
2. Por ítem: `reserved -= qty`.
3. Realtime `sale:rejected`.

---

## 6. Implementación de referencia

| Actor | Pieza |
|-------|--------|
| Web | `RegisterNewSaleCaseUse` + soft-hold |
| Android | `ApplySoftHoldCaseUse` + `SaleViewModel` |
| Operador | `OperatorConfirmPaymentScreen` (tipo + importe DISCOUNT) → `confirmSelectedSale(saleType, discountAmount)` → `UpdateSaleVerificationFromRealtimeCaseUse` + stock |

---

## 7. Tests parciales (Core 1)

| Suite | Qué valida |
|-------|------------|
| Web `check-a-product-existence.case.use.test.ts` | available antes de crear |
| Web `RegisterNewSaleCaseUse.soft-hold.test.ts` | reserved += qty, insuficiencia, telegram best-effort |
| Android `SoftHoldCaseUseTest` | availableStock, check, ApplySoftHold, idempotencia |

---

## 8. Checklist Core 1

- [x] Cliente crea UNVERIFIED + soft-hold (web + Android)
- [x] Check usa `available`
- [x] Operador carga por QR o código manual
- [x] Confirmar → VERIFIED + `SaleType` (UI chips) + baja existence/reserved
- [x] Rechazar → DELETED + libera reserved
- [x] GIFT fuerza amount 0 y sigue bajando stock
- [x] DISCOUNT: importe efectivo editable y validado (`0 ≤ amount < lista`)
- [x] Realtime best-effort (no revierte estado remoto)
- [x] Visitante no puede crear venta
- [x] Tests parciales soft-hold / available (web + Android cliente)
- [ ] Persistencia `StockMovement` — Core 2
- [ ] Tests automatizados operador confirm/reject stock — Core 2 / QA

---

## 9. Fuera de alcance Core 1

- Descuento por línea de ítem (solo amount total de la venta)
- Multi-operador locks avanzados
- Devoluciones parciales
- Function atómica Appwrite
- Colección `stock_movements`
