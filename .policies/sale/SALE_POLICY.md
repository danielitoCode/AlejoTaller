# Política de ventas (Sale)

Documento de validación del dominio `sale` (cliente web/Android + operador `alejotallerscan`).

Última actualización: 2026-08-02  
Ámbito: **Core 1 (MVP) + soft-hold + SaleType en operador**

---

## 1. Principio general

> **La venta solo se cierra (confirmada o rechazada) en la aplicación de escaneo del operador.**

El cliente **solicita** (pedido + soft-hold de inventario).  
El operador **decide**: tomar (VERIFIED) o rechazar (DELETED), y al confirmar elige el **tipo de venta**.

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

Afectan **precio**, no el movimiento de stock.

| Tipo | Código | Importe al confirmar | Stock |
|------|--------|----------------------|-------|
| Normal | `NORMAL` | se mantiene el del pedido | baja |
| Descuento | `DISCOUNT` | se mantiene el del pedido (ajuste manual futuro) | baja |
| Regalia | `GIFT` | **0** | baja |

### UI operador (Core 1)

En pantalla de confirmación el operador elige con chips:

- **Normal** (default)
- **Descuento**
- **Regalia**

Al confirmar se persiste `sale_type` en Appwrite junto con `buy_state = VERIFIED`.

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
2. Si `GIFT` → `amount = 0`.
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
| Operador | `OperatorConfirmPaymentScreen` (chips SaleType) → `confirmSelectedSale(saleType)` → `UpdateSaleVerificationFromRealtimeCaseUse` + stock |

---

## 7. Checklist Core 1

- [x] Cliente crea UNVERIFIED + soft-hold (web + Android)
- [x] Check usa `available`
- [x] Operador carga por QR o código manual
- [x] Confirmar → VERIFIED + `SaleType` (UI chips) + baja existence/reserved
- [x] Rechazar → DELETED + libera reserved
- [x] GIFT fuerza amount 0 y sigue bajando stock
- [x] Realtime best-effort (no revierte estado remoto)
- [x] Visitante no puede crear venta
- [ ] Ajuste de importe en DISCOUNT desde UI (Core 1: amount del pedido)
- [ ] Persistencia `StockMovement` — Core 2

---

## 8. Fuera de alcance Core 1

- Editor de monto para DISCOUNT en operador
- Multi-operador locks avanzados
- Devoluciones parciales
- Function atómica Appwrite
