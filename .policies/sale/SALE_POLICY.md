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
| **UNVERIFIED** | Cliente (web/Android) | Solicitud pendiente | soft-hold: `reserved += qty` |
| **VERIFIED** | Operador | Venta tomada | `existence -= qty`, `reserved -= qty` |
| **DELETED** | Operador | Descartada | libera hold: `reserved -= qty` |

### Canales de atención del operador

1. **Escaneo QR**
2. **Entrada manual** (código / id)

---

## 3. Tipos de venta (`SaleType`)

Afectan **precio**, no el movimiento de stock.

| Tipo | Código | Importe |
|------|--------|---------|
| Normal | `NORMAL` | lista |
| Descuento | `DISCOUNT` | efectivo menor |
| Regalia | `GIFT` | 0 |

- Tipo se fija al confirmar (Core 1: default `NORMAL` si no hay UI de tipo aún).
- GIFT/DISCOUNT **sí** bajan stock al confirmar.

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
1. `verified = VERIFIED` (+ `saleType` default NORMAL).
2. Por ítem: `existence -= qty`, `reserved -= qty`.
3. Realtime `sale:confirmed`.

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
| Operador | `OperatorSalesViewModel` → `ApplyOperatorStockDecisionCaseUse` |

---

## 7. Checklist Core 1

- [x] Cliente crea UNVERIFIED + soft-hold (web + Android)
- [x] Check usa `available`
- [x] Operador carga por QR o código manual
- [x] Confirmar → VERIFIED + baja existence/reserved
- [x] Rechazar → DELETED + libera reserved
- [x] GIFT/DISCOUNT/NORMAL restan igual al confirmar
- [x] Realtime best-effort (no revierte estado remoto)
- [x] Visitante no puede crear venta
- [ ] UI operador para elegir SaleType (DISCOUNT/GIFT) — default NORMAL
- [ ] Persistencia `StockMovement` — Core 2

---

## 8. Fuera de alcance Core 1

- Multi-operador locks avanzados
- Devoluciones parciales
- Function atómica Appwrite
