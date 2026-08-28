# Política de tipo de cambio y moneda (Exchange)

Última actualización: 2026-08-28  
Ámbito: **AlejoTaller** (web cliente, Android cliente, operador)  
Canónico compras / protección de margen: `dash_alejo_taller/.policies/exchange/EXCHANGE_POLICY.md`  
Relacionado: [SALE_POLICY](../sale/SALE_POLICY.md), [WAREHOUSE_POLICY](../warehouse/WAREHOUSE_POLICY.md)

---

## 1. Principio general

> **USD es la moneda referencial del negocio.**  
> `product.price` y `product.last_unit_cost` viven en **USD** (escritos por el panel).  
> El cliente puede **mostrar** precios en CUP usando la tasa de mercado del día; eso es solo presentación.  
> La moneda del pedido (`sale.currency`: CUP / USD / MLC) es la que eligió el cliente en checkout; el operador **no reconvierte** con otra tasa.

Este repo **no registra compras ni escribe** `last_unit_cost` ni ajusta precio por margen. Eso es competencia del dash (Core 3).

---

## 2. Fuente de tasa (canónica del ecosistema)

| Aspecto | Valor |
|---------|--------|
| API | `https://widgets.directoriocubano.info/api/tasas` (env `VITE_DIRECTORIO_CUBANO_API_URL` en web) |
| Campo | `tasas.USD.CUP` → **CUP por 1 USD** |
| También | `tasas.EUR.CUP` (referencia secundaria si se usa) |
| Fuente auditada | `DIRECTORIO_CUBANO` |
| Patrón | Offline-first: cache del día + fetch; ver feature `exchange` |

Implementación de referencia:

| Superficie | Pieza |
|------------|--------|
| Web | `ExchangeNetRepository`, `ExchangeOfflineFirstRepository`, `exchanges.store`, `CurrencySwitch` |
| Android | `ExchangeNetRepository`, `ExchangeOfflineFirstRepository`, `CurrencySwitch` |

El **dash** debe reutilizar la misma API y convención al convertir compras CUP → USD.

---

## 3. Uso de la tasa en este monorepo

### 3.1 Display en tienda (web / Android cliente)

- Catálogo y detalle muestran precio según `DisplayCurrency` (`CUP` \| `USD`).
- Si el usuario elige CUP: `precioMostrado ≈ priceUSD × usdReference` (con la tasa cacheada/hoy).
- Fallo de tasa: no inventar número; degradar a USD o mensaje claro (no bloquear navegación del catálogo).
- La tasa de display **no** se persiste en el pedido como “tasa de compra del negocio”; el pedido guarda la **moneda elegida** y el **amount** en esa moneda según el flujo de checkout actual.

### 3.2 Pedido / venta (`sale.currency`)

| Campo | Regla |
|-------|--------|
| `currency` | `CUP` \| `USD` \| `MLC` — la del checkout del cliente |
| `amount` | En la moneda del pedido |
| Operador | Muestra moneda e importe del cliente **sin reconvertir** con una tasa nueva |

No hay snapshot de tasa de compra en el sale del cliente (no es factura de abastecimiento).

### 3.3 Operador (`alejotallerscan`)

- Confirma VERIFIED / DELETED según [SALE_POLICY](../sale/SALE_POLICY.md).
- COGS en finanzas del panel usa `last_unit_cost` (USD) × qty — **no** depende de la moneda del cliente.
- No consulta tasa de Directorio Cubano para confirmar pago.

### 3.4 Qué no hace este monorepo

| Acción | ¿Aquí? | Dónde |
|--------|---------|--------|
| Registrar factura de entrada CUP/USD | No | dash |
| Snapshot de tasa en `purchase_entry` | No | dash |
| Auto-ajustar `product.price` (+30 % si costo > precio) | No | dash |
| Escribir `last_unit_cost` | No (solo lectura para COGS si aplica) | dash escribe |

---

## 4. Invariantes (capa de dominio / tests)

1. `product.price` y `last_unit_cost` se tratan como **USD** en lógica de negocio compartida.
2. Display CUP = transformación de presentación; no muta `product.price` en Appwrite.
3. Operador no aplica tasa de mercado al importe del cliente.
4. Tasa de API inválida (≤ 0, ausente) no se usa para display ni se inventa un default silencioso en flujos críticos de compra (N/A aquí; en display: degradar con claridad).
5. Soft-hold y stock no dependen de moneda ni de tasa.

---

## 5. Relación con el dash (protección del negocio)

El panel aplica:

- conversión CUP → USD al registrar entradas con snapshot,
- `last_unit_cost` siempre en USD,
- si costo unitario USD > precio de lista → `price = unitCostUSD × 1.30` + señal en producto.

Tras eso, tienda y operador consumen el nuevo `price` / `last_unit_cost` sin lógica extra de cambio en este repo.

Detalle normativo de compras: **`dash_alejo_taller/.policies/exchange/EXCHANGE_POLICY.md`**.

---

## 6. Checklist

- [x] API Directorio Cubano + mapper `USD.CUP` / `EUR.CUP` (web + Android)
- [x] Offline-first tasa del día
- [x] CurrencySwitch display CUP/USD
- [x] Operador: moneda del cliente sin reconversión de tasa
- [ ] Documentar degradación UX si falla la API de tasas en catálogo
- [ ] Paridad documentada con dash (esta política + dash EXCHANGE_POLICY)

> Fecha: 2026-08-28  
> Rama: Core3
