# QA Core 1 — Checklist de verificación (posición cliente + operador)

**Proyecto:** AlejoTaller  
**Objetivo:** Validar cada pieza del MVP Core 1 antes de dar por cerrado el núcleo.  
**Uso:** Marca `[x]` cuando el ítem esté verificado. Anota fallos en “Notas”.

**Orden recomendado:** 1) Web → 2) Android cliente → 3) Operador → 4) E2E cruzado.

**Última alineación de código (2026-08-08)** — no invalida los `[X]` ya marcados; ver §0.6 y §R.

**Entorno**
- Web: producción o build local
- Android cliente: `app` debug/release de prueba
- Operador: `alejotallerscan`
- Appwrite: productos con `existence` y `reserved` conocidos

**Productos de prueba sugeridos**
- A: existence 10, reserved 0 → available 10
- B: existence 5, reserved 3 → available 2
- C: existence 2, reserved 2 → available 0

---

## 0. Preparación común

- [X] 0.1 Productos A/B/C cargados en Appwrite con existence/reserved correctos
- [X] 0.2 Cuenta cliente autenticada de prueba (email real)
- [X] 0.3 Segunda sesión (otro navegador o dispositivo) para race de soft-hold
- [X] 0.4 Acceso a operador (`alejotallerscan`) con rol válido
- [ ] 0.5 Anotar saleId / QR de cada pedido de prueba
- [ ] 0.6 **Re-smoke post-alineación (2026-08-08):** auth visitante + login + 1 producto available=0 + 1 carrito clamp qty (A2.4) — confirmar que los `[X]` de A1/A2 siguen verdes

**Notas preparación:**

```
Alineaciones de código desde último pase parcial de QA (no re-abrir A1 salvo regresión en 0.6):
- Soft-hold atómico Appwrite (increment/decrement reserved) web + Android
- Stock + sale-verification: Appwrite Realtime (web y Android cliente); Pusher solo promo residual
- Operador: muestra sale.currency del cliente (sin reconvertir tasa)
- UI web: carrito + mis reservas (responsive, iconos, animaciones)
- Cart clamp available (A2.4) implementado en código web
```

---

# A. WEB (cliente Svelte)

## A1. Auth — sesión y visitante

### Primera visita
- [X] A1.1 Abrir URL sin cookies/localStorage → se muestra **Welcome / Landing**
- [X] A1.2 Continuar como visitante → home en modo **visitante**, navegación limitada a productos

### Visitante recurrente
- [X] A1.3 Con flag de visita (`alejo_has_visited` o equivalente) → **no** vuelve a Welcome; entra a productos como visitante
- [X] A1.4 Sesión anónima / email vacío → clasificado como **visitante** (no autenticado)

### Deeplink
- [X] A1.5 URL directa de detalle de producto **sin sesión** → visitor + detalle, **sin** Welcome
- [X] A1.6 Desde ese detalle, intentar comprar → bloqueo / overlay de auth (no crea venta)

### Login / logout
- [X] A1.7 Login con email → modo **authenticated**, navegación completa
- [X] A1.8 Logout → vuelve a restricciones de visitante

### Visitante no vende
- [X] A1.9 Como visitante, checkout **no** crea `Sale UNVERIFIED`
- [X] A1.10 Overlay o redirección a login al intentar comprar

### UX auth (notas históricas → código)
- [X] A1.11 Topbar: acceso claro a login para visitante (vía directa sin depender solo del checkout)
- [X] A1.12 Login/Registro: loading indicator no genérico al enviar (UX moderna)

**Notas Auth Web:**
```
Cumplidos A1.1–A1.10 en pase anterior. A1.11–A1.12 cerrados en código tras notas UX.
Alineaciones RT/stock/UI (2026-08-08) no tocan flujo AuthCheck ni gates isGuest.
Re-smoke 0.6: Welcome / visitante / login aún coherentes.
```

## A2. Catálogo y stock (check de available)

- [X] A2.1 Listado de productos carga (online y tras refresh)
- [X] A2.2 Detalle de producto coherente (precio, nombre, categoría)
- [X] A2.3 Producto available = 0 → al intentar pedir → error de **no disponibilidad**; no crea pedido
- [X] A2.4 Producto available = 2 → permite qty 1 o 2; **rechaza** qty 3  
  *(código: clamp en carrito + check antes de sale; **re-verificar en UI** tras rediseño carrito 2026-08-08)*

### A2b. Badges / realtime stock (alineación nueva)
- [X] A2.5 Badge available en listado/detalle (existence − reserved); no bloquear catálogo mientras sincroniza
- [ ] A2.6 Appwrite Realtime product: al cambiar reserved/existence (otro cliente u operador), badge se actualiza sin salir de la vista + feedback “Se están actualizando los datos…”
- [ ] A2.7 Offline-first: snapshot RT se aplica a Dexie; al reabrir catálogo persiste

**Notas Catálogo Web:**
```
A2.1–A2.3 verificados previamente; RT Appwrite no cambia getAll/fallback Dexie del listado inicial.
A2.4: implementado clamp local + soft-hold atómico; marcar regresión solo si qty 3 vuelve a pasar.
A2.6–A2.7: QA pendiente explícito post-migración Pusher → Appwrite RT.
```

## A3. Carrito y checkout (Sale + soft-hold)

### Carrito
- [ ] A3.1 Añadir 1 línea → cantidades y total correctos
- [ ] A3.2 Añadir 2+ productos → total = suma de líneas
- [ ] A3.3 Cambiar cantidad / quitar ítem → total se actualiza
- [ ] A3.3b UI carrito (2026-08-08): stepper +/−, subtotal línea, moneda via CurrencySwitch, barra sticky Continuar usable en móvil

### Checkout → UNVERIFIED + soft-hold
- [ ] A3.4 Usuario autenticado completa checkout → venta **UNVERIFIED**
- [ ] A3.5 Pedido aparece en reservas / mis pedidos
- [ ] A3.6 Soft-hold: `reserved` del producto sube en la cantidad pedida (Appwrite o segundo cliente agota available)
- [ ] A3.7 Fallo de Telegram/notificación **no** impide que el pedido quede creado

### Idempotencia
- [ ] A3.8 No se aplica **doble** soft-hold en reintento/refresh si ya hay hold aplicado
- [ ] A3.9 Refresh tras crear: pedido sigue UNVERIFIED; available no baja “el doble”

### Race / segundo cliente
- [ ] A3.10 Producto available 2; Cliente A pide 2 → OK
- [ ] A3.11 Cliente B intenta 1 del mismo → **falla** por disponibilidad
- [ ] A3.12 (Tras rechazo operador del pedido A) Cliente B puede comprar de nuevo → ver sección C

**Notas Checkout Web:**
```
Soft-hold usa incrementDocumentAttribute (atómico). Compensación multi-línea en código.
UI carrito rediseñada: no altera cart.store clamp ni RegisterNewSaleCaseUse.
Moneda del pedido = la del checkout (tasa en cliente); no recalcular en operador.
```

## A4. Post-pedido (cliente web)

- [ ] A4.1 Listado de reservas muestra el nuevo pedido
- [ ] A4.2 Detalle: estado UNVERIFIED, ítems, monto, **currency**, dirección si aplica
- [ ] A4.3 **Appwrite Realtime sale** (no Pusher): al confirmar/rechazar en operador, UI actualiza estado + toast/alerta sin depender de publish con secret
- [ ] A4.4 Cliente **no** puede auto-confirmar ni pasar a VERIFIED/DELETED desde la web
- [ ] A4.5 UI “Mis reservas” (2026-08-08): importe con moneda del pedido, badges estado, cards responsive

**Notas Post-pedido Web:**
```
Canal canónico: databases.*.collections.sale.documents → ApplySaleRealtimeSnapshot.
Pusher sale-verification-* ya no es requerido en web para A4.3.
```

## A5. Regresiones Web

- [ ] A5.1 Navegación guest vs auth estable con atrás/adelante del navegador
- [ ] A5.2 Offline breve (DevTools) → no crash; al volver online catálogo/pedidos coherentes
- [ ] A5.3 Mobile viewport: checkout y reservas usables
- [ ] A5.4 Consola sin errores rojos repetidos en flujo feliz
- [ ] A5.5 Auth A1.1–A1.10 siguen pasando tras RT/UI (smoke 0.6)

### Criterio salida Web

| Bloque | Mínimo para aprobar |
|--------|---------------------|
| Auth | A1.1–A1.10 |
| Stock check | A2.3–A2.4 |
| Checkout + soft-hold | A3.4–A3.6 + A3.10–A3.11 |
| Post-pedido | A4.1–A4.2, A4.4 |
| RT stock/sale (nuevo) | A2.6 + A4.3 recomendados para cierre “paridad tiempo real” |

**Bloqueantes Web:** visitante crea venta; checkout crea VERIFIED; over-sell (vende por encima de available); soft-hold no aplica.

- [ ] **A. WEB APROBADO** (fecha: ________)

---

# B. ANDROID CLIENTE (`app`)

## B1. Auth — sesión y visitante

- [ ] B1.1 Primera apertura sin sesión → flujo visitante (productos / gates según política)
- [ ] B1.2 Visitante: navegación limitada; **no** acceso a crear venta
- [ ] B1.3 Deeplink / deep link a producto sin sesión → detalle en modo visitante, sin forzar Welcome innecesario
- [ ] B1.4 Intento de compra como visitante → login / bloqueo; **no** crea Sale
- [ ] B1.5 Login email → modo authenticated
- [ ] B1.6 Logout → vuelve a restricciones de visitante
- [ ] B1.7 Sesión anónima / perfil vacío → tratado como visitante (`isGuest`)

**Notas Auth Android:**
```
Sin cambios de política auth en la alineación 2026-08-08 (solo RT stock/sale).
```

## B2. Catálogo y stock

- [ ] B2.1 Listado productos carga (online / offline cache)
- [ ] B2.2 Detalle coherente
- [ ] B2.3 available = 0 → no permite completar pedido; mensaje claro
- [ ] B2.4 available = 2 → permite 1–2; rechaza 3
- [ ] B2.5 Badge available en listado/detalle
- [ ] B2.6 **Appwrite Realtime product** (reemplaza Pusher stock-updates): snapshot → Room; UI reactiva + mensaje de sincronización

**Notas Catálogo Android:**
```
StockUpdatesListener = AppwriteStockUpdatesListener; soft-hold atómico intacto.
```

## B3. Carrito / checkout + soft-hold

- [ ] B3.1 Carrito: añadir, cambiar qty, quitar, total correcto
- [ ] B3.2 Checkout autenticado → Sale **UNVERIFIED** en Appwrite + local
- [ ] B3.3 Soft-hold: `reserved` incrementa en la qty pedida (atómico Appwrite)
- [ ] B3.4 Pedido visible en reservas / mis pedidos
- [ ] B3.5 Telegram/notificación fallida no tumba el guardado del pedido
- [ ] B3.6 Idempotencia: no doble hold en reintento
- [ ] B3.7 Race: A consume available; B falla al pedir el mismo cupo

**Notas Checkout Android:**
```
ApplySoftHoldCaseUse + compensación multi-línea; no depende de Pusher para mutar stock.
```

## B4. Post-pedido

- [ ] B4.1 Listado y detalle de pedido UNVERIFIED correctos
- [ ] B4.2 **Appwrite Realtime sale** (RealTimeManagerImpl): confirm/reject operador → VERIFIED/DELETED + notificación UI
- [ ] B4.3 Cliente **no** puede auto-verificar el pedido

## B5. Regresiones Android

- [ ] B5.1 Rotación de pantalla / background no pierde estado crítico del carrito
- [ ] B5.2 Offline breve → no crash; sync al recuperar red
- [ ] B5.3 FAB / navegación respetan `isGuest` (sin crashes de argumentos)
- [ ] B5.4 Promo canal: sigue por Pusher (no regresionar si solo se migró stock/sale)

### Criterio salida Android cliente

| Bloque | Mínimo |
|--------|--------|
| Auth | B1.1–B1.7 |
| Stock | B2.3–B2.4 |
| Checkout + hold | B3.2–B3.3 + B3.7 |
| Post-pedido | B4.1, B4.3 |
| RT (nuevo) | B2.6 + B4.2 recomendados |

**Bloqueantes Android:** mismos que Web (visitante vende, over-sell, sin soft-hold, auto-VERIFIED).

- [ ] **B. ANDROID CLIENTE APROBADO** (fecha: ________)

---

# C. OPERADOR (`alejotallerscan`)

> El operador es la **única** superficie que pasa pedidos a VERIFIED/DELETED y muta existence/reserved de forma definitiva.

## C1. Acceso y listado

- [ ] C1.1 Login operador con cuenta autorizada
- [ ] C1.2 Listado de reservas / pedidos UNVERIFIED visible
- [ ] C1.3 Escaneo QR o búsqueda por id abre el detalle correcto
- [ ] C1.4 Detalle muestra ítems, cantidades, cliente, monto base
- [ ] C1.5 **Moneda del cliente visible** (`sale.currency`: CUP/USD/MLC) en resumen y badges de importe — sin forzar `$` ni reconvertir tasa

**Notas listado operador:**
```
Confirmación UI 2026-08-08: money(amount, currency). Tasa elToque solo en clientes.
Si currency falta en documento Appwrite → fallback USD (revisar create en cliente).
```

## C2. SaleType al confirmar (NORMAL / DISCOUNT / GIFT)

- [ ] C2.1 UI permite elegir tipo: **NORMAL**, **DISCOUNT**, **GIFT** (chips o equivalente)
- [ ] C2.2 **NORMAL:** monto = suma de líneas (o policy vigente); no exige editor forzado de importe
- [ ] C2.3 **DISCOUNT:** campo de **importe efectivo editable** en **misma moneda del pedido**; se guarda el monto editado
- [ ] C2.4 **GIFT:** monto 0 (o policy); no debe exigir pago
- [ ] C2.5 Tipo y monto quedan reflejados en el documento Sale tras confirmar

**Notas SaleType:**
```
DISCOUNT no debe convertir CUP↔USD; solo editar amount en currency del pedido.
```

## C3. Confirmar → VERIFIED + consumo de stock

- [ ] C3.1 Confirmar pedido UNVERIFIED → estado remoto **VERIFIED**
- [ ] C3.2 Por cada línea: `existence -= quantity` y `reserved` se libera en la misma qty (neto: available no “se come dos veces”)
- [ ] C3.3 Si stock insuficiente al confirmar (edge): comportamiento controlado (no deja existence < 0; mensaje o bloqueo según policy)
- [ ] C3.4 Clientes web/Android actualizan vía **Appwrite RT** (updateDocument basta; publish Pusher sale ya no es requisito para clientes migrados)
- [ ] C3.5 Operador **no** crea ventas nuevas de cliente final

**Notas confirmación:**
```
NotifyOperatorSaleDecision (Pusher) puede seguir best-effort; no bloquear si falla.
Stock operador aún RMW (no atómico como cliente); documentar edge multi-operador Core 2.
```

## C4. Rechazar → DELETED + release soft-hold

- [ ] C4.1 Rechazar pedido → estado **DELETED**
- [ ] C4.2 `reserved` de cada línea **se libera** (reserved -= qty); existence **no** baja
- [ ] C4.3 Tras rechazo, otro cliente puede comprar el cupo liberado (available recupera)
- [ ] C4.4 Cliente ve rechazo por Realtime Appwrite o refresh

**Notas rechazo:**
```
```

## C5. Regresiones operador

- [ ] C5.1 Confirmar dos veces el mismo pedido no descuenta stock dos veces (idempotencia)
- [ ] C5.2 Rechazar pedido ya VERIFIED no aplica (o no corrompe stock)
- [ ] C5.3 Sin red: no confirma a ciegas; error claro
- [ ] C5.4 App de escaneo **no** admite flujo visitante de compra (solo operador)

### Criterio salida Operador

| Bloque | Mínimo |
|--------|--------|
| Acceso | C1.1–C1.5 |
| SaleType | C2.1–C2.5 |
| VERIFIED + stock | C3.1–C3.2 |
| DELETED + release | C4.1–C4.3 |

**Bloqueantes Operador:** existence negativa; reserved no se libera en rechazo; DISCOUNT sin poder editar monto; doble descuento al re-confirmar; UI que ignora currency del cliente en cobro.

- [ ] **C. OPERADOR APROBADO** (fecha: ________)

---

# D. E2E cruzado (Web/Android + Operador)

- [ ] D1 Cliente Web crea UNVERIFIED → Operador confirma NORMAL → stock consumido → Web ve VERIFIED **por Appwrite RT**
- [ ] D2 Cliente Android crea UNVERIFIED → Operador confirma DISCOUNT (monto editado, misma currency) → monto y stock correctos
- [ ] D3 Cliente Web crea UNVERIFIED → Operador rechaza → reserved liberado → segundo cliente puede comprar
- [ ] D4 Cliente Android + soft-hold race (dos dispositivos) coherente con Web
- [ ] D5 GIFT: operador marca GIFT → VERIFIED con monto 0 según policy; stock sí se consume (regalo físico)
- [ ] D6 Operador muestra **CUP/USD/MLC** coherente con el pedido del cliente (sin reconversión)

**Notas E2E:**
```
```

- [ ] **D. E2E APROBADO** (fecha: ________)

---

# R. Matriz de no-regresión (alineaciones 2026-08-08)

| Área ya en verde / estable | ¿Qué cambió? | ¿Riesgo de romper? | Acción QA |
|----------------------------|--------------|--------------------|-----------|
| A1 Auth web | Ninguno en AuthCheck | Bajo | Smoke 0.6 / A5.5 |
| A2.1–A2.3 catálogo | RT listener + badges | Bajo si listado sigue offline-first | Smoke listado + available=0 |
| A2.4 clamp | UI carrito rediseño | Medio (solo UI controls) | Re-probar qty máx |
| Soft-hold atómico | Infra ya en master | Bajo | A3.6 + race A3.10–11 |
| Operador VERIFIED/DELETED | Solo display currency | Bajo en mutación stock | C1.5 + C3/C4 |
| Visitante no vende | Sin cambio | Bajo | A1.9 |

**Conclusión desarrollo:** las alineaciones **no invalidan** los `[X]` de A1 ni A2.1–A2.3; A2.4 queda marcado cumplido en código con **re-verificación UI** obligatoria tras el rediseño del carrito.

---

# E. Veredicto final Core 1

| Superficie | Estado | Fecha |
|------------|--------|-------|
| A. Web | [ ] Aprobado / [ ] Bloqueado | |
| B. Android cliente | [ ] Aprobado / [ ] Bloqueado | |
| C. Operador | [ ] Aprobado / [ ] Bloqueado | |
| D. E2E | [ ] Aprobado / [ ] Bloqueado | |

**Cerrar Core 1 y dar por listo el núcleo solo si A+B+C (y D recomendado) están aprobados sin bloqueantes.**

**Bloqueantes globales (cualquiera impide cierre):**
1. Visitante puede crear venta
2. Over-sell (pedido con qty > available)
3. Soft-hold no reserva o no libera en DELETED
4. VERIFIED deja existence < 0 o no descuenta
5. Cliente puede auto-pasar a VERIFIED

**No bloqueantes (backlog Core 2):**
- stock_movements / reportes
- Stock operador 100% atómico (hoy RMW)
- Multi-operador locks
- Quitar Pusher promo por completo
- Observabilidad (Sentry/Crashlytics)
- Telegram caído (pedido igual se crea)

**Firma QA / responsable:** _______________  **Fecha cierre Core 1:** _______________
