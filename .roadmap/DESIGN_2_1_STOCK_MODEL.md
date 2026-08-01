# Diseño 2.1 — Modelo de datos de inventario / stock

**Fecha:** 2026-08-01  
**Alcance:** Micro-tareas 2.1.1 – 2.1.3  
**Principio:** Paridad funcional cliente Android (`app`) ↔ cliente Web (`web`). El descuento definitivo de stock ocurre al **confirmar** la venta en `alejotallerscan` (operador), no al crear el pedido.

---

## 0. Estado actual (baseline del código)

| Superficie | Campo de stock hoy | Comportamiento |
|------------|--------------------|----------------|
| **Web** (`web/.../Product`) | `existence: number` | Existe en entidad + DTO Appwrite. `CheckAProductExistenceCaseUse` valida al crear venta que `quantity <= existence`. **No descuenta** stock. |
| **Android cliente** (`app/.../Product`) | **Ausente** | `Product` / `ProductDto` no mapean `existence` desde Appwrite. |
| **Operador** (`alejotallerscan`) | Solo nombres de producto | Confirma/rechaza venta en Appwrite + Pusher. **No toca inventario**. |
| **Appwrite** | Atributo `existence` (usado por web) | Fuente remota parcial; sin colección de movimientos. |

**Conclusión:** el esquema remoto ya anticipa stock (`existence`). Hay que **alinear Android**, formalizar movimientos y definir quién escribe y cuándo.

---

## 2.1.1 Campos de stock en producto

### Decisión de nomenclatura

Se mantiene el atributo Appwrite existente **`existence`** como stock disponible (evitar migración destructiva). En dominio se documenta el sinónimo:

| Campo Appwrite / DTO | Dominio (Kotlin / TS) | Significado |
|----------------------|----------------------|-------------|
| `existence` | `existence` (alias mental: stock disponible) | Unidades físicamente vendibles **ahora** |
| `stock_reserved` *(nuevo, opcional Fase 3)* | `stockReserved` | Unidades comprometidas en pedidos `UNVERIFIED` |
| `stock_min` *(nuevo, Fase 5)* | `stockMin` | Umbral de alerta "últimas unidades" |

**MVP Fase 2 – mínimo obligatorio**

```text
Product {
  ...campos actuales...
  existence: Int >= 0     // stock disponible
}
```

- Valor por defecto en lectura si falta en documento remoto: `0` (no `null` en dominio).
- Regla de escritura: **nunca** persistir `existence < 0`.
- `stock_reserved` y `stock_min` se dejan definidos en este diseño pero **no bloquean** 2.2; se implementan cuando toque Fase 3 / 5.

### Paridad Android ↔ Web

| Capa | Android (`app`) | Web (`web`) |
|------|-----------------|-------------|
| Domain | Añadir `existence: Int` a `Product` | Ya existe |
| DTO / Room | Añadir a `ProductDto` + migración Room | Ya existe en `ProductDTO` |
| Mapper Appwrite | Leer `data["existence"]` en `Document.toProductDto` | Ya mapea |
| Validación checkout | Caso de uso análogo a `CheckAProductExistenceCaseUse` | Ya existe |
| UI catálogo / detalle | Mostrar en Fase 4 | Mostrar en Fase 4 |

---

## 2.1.2 Entidad `stock_movements`

Colección Appwrite + modelo de dominio para traza contable/operativa.

### Campos

| Campo | Tipo | Obligatorio | Notas |
|-------|------|-------------|-------|
| `$id` | string | sí | Id documento Appwrite |
| `product_id` | string | sí | FK lógica al producto |
| `type` | enum string | sí | ver tipos abajo |
| `quantity` | int | sí | Siempre **positiva**; el signo lo da el `type` |
| `balance_after` | int | sí | `existence` del producto **después** del movimiento |
| `reason` | string | sí | Motivo legible (máx. razonable ~200 chars) |
| `user_id` | string | sí | Quién originó el movimiento |
| `sale_id` | string? | no | Referencia al pedido si `salida_venta` / `devolucion` |
| `created_at` | datetime ISO | sí | Preferir server datetime Appwrite |

### Tipos de movimiento (`type`)

| Valor | Efecto en `existence` | Quién lo genera |
|-------|----------------------|-----------------|
| `entrada` | `+= quantity` | Admin / dashboard / operador autorizado (Fase 5) |
| `salida_venta` | `-= quantity` | **Operador al confirmar** venta (`VERIFIED`) — Fase 3 |
| `ajuste` | `+=` o `-=` según convención en `reason` / campo extra `signed_delta`* | Admin (Fase 5) |
| `devolucion` | `+= quantity` | Operador o admin si se reabre stock de venta rechazada/devuelta |

\*Para `ajuste`, en implementación se puede usar `quantity` + `direction: in|out` o `signed_quantity`. Preferencia MVP: **`direction`: `"in" | "out"`** solo en tipo `ajuste`.

### Dominio (sketch)

```kotlin
// Kotlin (futuro shared o feature stock)
enum class StockMovementType { ENTRADA, SALIDA_VENTA, AJUSTE, DEVOLUCION }

data class StockMovement(
    val id: String,
    val productId: String,
    val type: StockMovementType,
    val quantity: Int,          // > 0
    val balanceAfter: Int,      // >= 0
    val reason: String,
    val userId: String,
    val saleId: String? = null,
    val createdAt: Instant
)
```

```typescript
// TypeScript (web)
export type StockMovementType = 'entrada' | 'salida_venta' | 'ajuste' | 'devolucion'

export interface StockMovement {
  id: string
  productId: string
  type: StockMovementType
  quantity: number
  balanceAfter: number
  reason: string
  userId: string
  saleId?: string | null
  createdAt: string // ISO
}
```

---

## 2.1.3 Dónde vive el stock (offline-first)

### Decisión

| Dato | Fuente de verdad | Copia local | Quién escribe |
|------|------------------|-------------|----------------|
| `Product.existence` | **Appwrite** collection products | Room (`app`) / Dexie (`web`) vía sync de catálogo | Escritura de descuento: **operador** (Fase 3). Entradas/ajustes: admin (Fase 5). Clientes: **solo lectura**. |
| `stock_movements` | **Appwrite** collection nueva | Opcional en operador (historial reciente); no requerido en cliente final en MVP | Mismo actor que muta `existence`, en la misma operación |

**No** poner la verdad solo en `shared-data` sin remoto: el monorepo ya usa Appwrite como remoto y offline-first para catálogo.

### Por qué el descuento es del operador

```text
Cliente (app / web)
  → crea Sale UNVERIFIED  (puede validar existence localmente; NO descuenta)
       ↓
Appwrite Sale
       ↓
alejotallerscan confirma (VERIFIED)
  → 1) actualiza buy_state (hoy)
  → 2) [Fase 3] por cada línea: existence -= qty + movimiento salida_venta
  → 3) publisher / Pusher (cliente refresca o re-sync de productos)

Rechazo (DELETED)
  → no toca existence
```

Razones:
1. El negocio ya centraliza la **confirmación de pago** en el operador.
2. Evita doble descuento si el cliente crea pedidos que nunca se pagan.
3. Un solo punto de escritura reduce race conditions multi-cliente.

Validación en cliente (Android + Web) al armar el carrito/pedido: **soft check** (`quantity <= existence` local). Validación **hard** al confirmar en operador: si no hay stock suficiente → no confirmar o parcial (definir en 3.1.4).

### Offline-first y conflictos

- Cliente offline puede mostrar stock desfasado → al volver online, sync de productos actualiza `existence`.
- Operador **debe** tener red para confirmar (hoy ya depende de Appwrite + publisher). El descuento de stock se hace **en remoto** en el mismo flujo de confirmación.
- Ideal a medio plazo (Core V2): Appwrite Function atómica `confirmSaleAndDeductStock(saleId)` para evitar que el APK operador sea el único escritor sensible. **MVP Fase 3** puede escribir desde el operador con reintento y verificación de lectura (mismo patrón que `verifyRemoteSaleState`).

### Ubicación de código (implementación posterior 2.2+)

| Pieza | Ubicación recomendada |
|-------|----------------------|
| Campos en Product | `app` + `web` (paridad); product aún no está en shared |
| Casos de uso descuento | Preferible `shared-sale` o nuevo `shared-stock` si crece; si no, dominio en operador + réplica de tipos en web admin |
| Movimientos DTO/repo | Nuevo feature o bajo product data en cada superficie que escriba |
| Lectura stock cliente | Ya cubierta por sync de productos una vez exista el campo en Android |

> Nota: product vive en `app` y `web` por separado (no en `shared-data`). La paridad se mantiene **por contrato de campos y reglas**, no necesariamente moviendo ya el módulo a shared (eso sería refactor mayor fuera de 2.1).

---

## Matriz de responsabilidades por aplicación

| Responsabilidad | `app` (Android cliente) | `web` (Svelte cliente) | `alejotallerscan` (operador) |
|-----------------|-------------------------|------------------------|------------------------------|
| Leer `existence` | Sí (tras 2.2) | Sí (ya) | Útil al confirmar (Fase 3) |
| Mostrar stock en UI | Fase 4 | Fase 4 | Opcional (detalle reserva) |
| Soft-check al pedir | Fase 4 / análogo web | Ya parcialmente | N/A |
| Crear pedido | Sí | Sí | No |
| Confirmar/rechazar venta | No | No | **Sí** |
| Descontar stock + movimiento | No | No | **Sí (Fase 3)** |
| Entradas / ajustes | No (MVP) | No (MVP; admin externo) | Posible Fase 5 |
| Publicar realtime post-confirm | No | Consume | Emite vía publisher |

---

## Orden de implementación sugerido (post 2.1)

1. **2.2** Alinear Android con `existence` (DTO, Room migration, mapper, dominio).
2. Confirmar/crear atributos Appwrite: `existence` (si falta en algunos docs), colección `stock_movements`.
3. Carga inicial de stock (script o dashboard).
4. **Fase 3** en `OperatorSalesViewModel.changeSelectedSale(true)`: tras verificar remoto VERIFIED, descontar + movimiento.
5. **Fase 4** UI paridad app/web + límites de carrito.

---

## Criterios de aceptación 2.1

- [x] 2.1.1 Campos de stock definidos (`existence` + opcionales documentados)
- [x] 2.1.2 Modelo `stock_movements` con tipos y campos
- [x] 2.1.3 Decisión de ubicación: Appwrite verdad remota + cache offline en clientes; escritura de descuento en operador
- [x] Implicaciones multi-app documentadas (paridad app/web; operador como punto de descuento)

**Siguiente:** 2.2 Implementación del modelo (empezar por paridad Android `existence`).
