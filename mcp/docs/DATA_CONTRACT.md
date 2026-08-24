# Contrato de datos Appwrite — MCP Cliente B2C

**Fuente canónica de negocio:** `web/src/core/feature/**` (TypeScript).  
**Collections en código MCP:** `mcp/src/infrastructure/appwrite/config.ts` → `COLLECTIONS`.

El MCP solo usa la **capa net** (sin Dexie). Los nombres de campos deben coincidir con los DTOs web.

---

## Collections permitidas (B2C)

| Key MCP (`COLLECTIONS`) | Collection ID Appwrite | Uso MCP |
|-------------------------|------------------------|---------|
| `product` | `product` | Catálogo + soft-hold (`reserved`) |
| `category` | `category` | Catálogo |
| `promotions` | `promotions` | Promos activas |
| `sale` | `sale` | Pedidos del cliente |
| `supportThreads` | `support_threads` | Tickets propios |
| `supportMessages` | `support_messages` | Mensajes de hilos propios |

### Collections **prohibidas** en MCP

`stock_movements`, `purchase_entry` / líneas, `sale_finance_event`, workshop admin, users staff.

---

## Product (`product`)

| Campo Appwrite | Dominio MCP / web | Notas |
|----------------|-------------------|--------|
| `$id` | `id` | |
| `name` | `name` | |
| `description` | `description` | |
| `existence` | `existence` | Stock físico |
| `reserved` | `reserved` | Soft-hold UNVERIFIED (default 0) |
| `price` | `price` | |
| `photo_url` | `photoUrl` | |
| `category_id` | `categoryId` | |
| `rating` | `rating` | opcional |

**Regla Core 1:**

```text
available = max(0, existence − reserved)
```

**Mutaciones atómicas (Fase 1):**

- `incrementDocumentAttribute(..., "reserved", qty, maxReserved)`
- `decrementDocumentAttribute(..., "reserved", qty, 0)`

Espejo web: `web/.../product/data/repository/product.net.repository.ts`.

---

## Sale / Order (`sale`)

| Campo Appwrite | Dominio MCP | Notas |
|----------------|-------------|--------|
| `$id` | `id` | |
| `date` | `date` | ISO |
| `amount` | `totalAmount` | |
| `buy_state` | `status` | `UNVERIFIED` \| `VERIFIED` \| `DELETED` |
| `currency` | `currency` | `CUP` \| `USD` \| `MLC` |
| `products` | `items` | **JSON string** de líneas |
| `user_id` | `userId` | Ownership obligatorio |
| `delivery_type` | `deliveryType` | `PICKUP` \| `DELIVERY` |
| `delivery_address` | `deliveryAddress` | JSON string o null |
| `sale_type` | `orderType` | suele fijarse al VERIFIED (operador) |
| `stock_hold_applied` | `stockHoldApplied` | idempotencia soft-hold |
| `$createdAt` / `$updatedAt` | `createdAt` / `updatedAt` | |

**Cliente B2C puede:**

- Crear con `buy_state = UNVERIFIED`, `stock_hold_applied = false` → luego hold → `true`
- Cancelar solo `UNVERIFIED` → `DELETED` + release hold

**Cliente B2C NO puede:** poner `VERIFIED` ni tocar finance/movements.

Espejo web: `SaleDTO`, `RegisterNewSaleCaseUse`, `CancelUnverifiedSaleCaseUse`.

---

## Category (`category`)

Campos típicos alineados a web: `$id`, `name`, y los que exponga el mapper en `category.appwrite.repository`.

---

## Promotions (`promotions`)

Lectura de promos vigentes; filtrado de “activas” en service (policy web `PromotionPolicy` como referencia).

---

## Support

| Collection | Uso |
|------------|-----|
| `support_threads` | Hilos del `userId` autenticado |
| `support_messages` | Mensajes de hilos propios |

Validaciones de negocio: espejo `CreateSupportThreadCaseUse` / `PostSupportMessageCaseUse` (subject/body no vacíos, ownership).

---

## Auth / identidad

| Fase | Mecanismo |
|------|-----------|
| Staging / Fase 1 | Header `X-Customer-Id` (+ opcionales name/email) inyectado por el host MCP |
| Prod (planificado) | JWT Appwrite `Authorization: Bearer` en `auth/resolver.ts` |

**Nunca** aceptar `userId` / `customerId` como argumento de tool del usuario final.

---

## Equivalencia web → MCP (implementación)

| Web | MCP |
|-----|-----|
| `*.domain.caseuse` | `mcp/src/services/*` |
| `*.domain.entity` | `mcp/src/domain/*` |
| `*.domain.repository` (interface) | `mcp/src/repositories/*` |
| `*.net.repository` | `mcp/src/infrastructure/appwrite/repositories/*` |
| Offline-first / Dexie / Pusher | **No** en MCP |
