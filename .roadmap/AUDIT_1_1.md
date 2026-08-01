# Auditoría 1.1 — Diagnóstico MVP (AlejoTaller)

**Fecha:** 2026-08-01  
**Alcance:** Micro-tareas 1.1.1 – 1.1.4  
**Fuente:** análisis estático del código en `master` + README del monorepo  
**Limitación:** no hay acceso a consola Appwrite, Sentry ni logs de producción en vivo.

---

## 1.1.1 Revisar logs de errores de producción / Appwrite / Sentry

### Hallazgos

| Fuente | Estado en el repo |
|--------|-------------------|
| Sentry / crash reporting remoto | **No integrado** de forma visible en el código |
| Appwrite Console logs | No accesibles desde el conector GitHub |
| Logs locales de compilación | Existen archivos vacíos/stub en `.kotlin/errors/` (sin contenido útil de runtime) |
| Instrumentación en código | Hay logs en flujos críticos (sync, realtime, operador) pero sin agregación central |

### Conclusión
No es posible listar errores reales de los últimos 7–14 días sin acceso a:
1. Appwrite Console (Activity / Function logs)
2. Render logs de `alejo_publisher` y de la web
3. Un servicio de crash reporting (Sentry, Firebase Crashlytics, etc.)

### Acción recomendada (fuera de 1.1, para 1.2 / Core V2)
- Añadir Crashlytics o Sentry en `app` y `alejotallerscan`
- Revisar logs de Render de `alejo_publisher` y de la web desplegada
- Exportar últimos errores de Appwrite Functions / Auth

**Estado micro-tarea:** parcial — documentada la ausencia de observabilidad central; bloqueada la revisión de logs reales.

---

## 1.1.2 Top 5 riesgos / fallos que afectan compras o autenticación

Basado en **deuda técnica del README** + análisis de entidades y casos de uso:

| # | Riesgo | Impacto | Evidencia en código / docs |
|---|--------|---------|----------------------------|
| 1 | **Publisher con API key simple de MVP** | Un atacante con la key puede publicar eventos falsos `sale:confirmed` / `sale:rejected` | README deuda técnica + `function/alejo_publisher` (`PUBLISHER_API_KEY`) |
| 2 | **Operaciones sensibles desde el cliente hacia Appwrite** | Reglas de permisos débiles o mal configuradas pueden permitir writes indebidos de ventas | README: "Appwrite sigue siendo accedido desde clientes en varias operaciones sensibles" |
| 3 | **`products` serializado como string en esquema remoto** | Parsing frágil, riesgo de inconsistencia entre plataformas al leer/escribir líneas de venta | README deuda de modelado; mappers `Document.toSaleDto` |
| 4 | **Reconciliación offline incompleta / conflictos** | Pedidos locales vs remotos pueden diverger; doble registro o pérdida de estado | README: falta endurecer políticas de conflicto |
| 5 | **Auth / sesión multi-superficie no homogeneizada** | Google login, roles operador vs cliente, y sesión web pueden comportarse distinto y bloquear compra | Módulos `shared-auth` + implementaciones separadas en `app`, `web`, `alejotallerscan` |

### Notas adicionales
- **Product** (cliente Android) **no tiene campo de stock** → coherente con el roadmap de almacén aún no iniciado.
- `RegisterNewSaleCauseUse` depende de Telegram + `SaleIdProvider` + repositorio; un fallo en notificación Telegram no debe tumbar el guardado (hoy está en el mismo `runCatching` → riesgo de fallo acoplado).

---

## 1.1.3 Flujo actual documentado

```text
[Cliente Android / Web]
    |
    | 1. Registro / Login (email-password o Google)
    |    - Appwrite Auth + SessionManager (shared-auth / app / web)
    |
    | 2. Catálogo de productos
    |    - Product offline-first (Room / Dexie) + sync Appwrite
    |
    | 3. Carrito / selección de ítems
    |    - SaleItem(productId, quantity, productName?)
    |
    | 4. Confirmar compra / reserva
    |    - RegisterNewSaleCauseUse:
    |         a) genera id (SaleIdProvider)
    |         b) notifica Telegram (opcional operativo)
    |         c) repository.save(Sale) → local + remoto Appwrite
    |    - Estado inicial: BuyState.UNVERIFIED
    |
    v
[Appwrite]  <-- documento Sale (user_id, products, amount, currency, verified, date, ...)
    |
    | 5. Operador (alejotallerscan)
    |    - Escanea QR del pedido o busca reserva
    |    - Revisa detalle (enriquece nombres de producto si hace falta)
    |    - Confirma o rechaza pago
    |    - Actualiza buy_state en Appwrite (VERIFIED / DELETED)
    |    - Verifica que el cambio remoto se aplicó
    |
    | 6. Publicación realtime
    |    - Operador llama a function/alejo_publisher
    |    - POST /sale-verification/publish { saleId, userId, decision, amount, productCount }
    |    - Function publica en Pusher canal sale-verification-{userId}
    |         event: sale:confirmed | sale:rejected
    |
    v
[Cliente Android / Web]
    | 7. Suscripción Pusher
    |    - InterpretSaleRealtimeEventCaseUse actualiza UI / estado local
    |    - Cliente puede elegir DeliveryType (PICKUP / DELIVERY) si VERIFIED
```

### Superficies involucradas
| Paso | Android cliente (`app`) | Web (`web`) | Operador (`alejotallerscan`) | Backend |
|------|-------------------------|-------------|------------------------------|---------|
| Auth | sí | sí | sí (rol operador) | Appwrite Auth |
| Catálogo | sí | sí | parcial (nombres) | Appwrite |
| Crear venta | sí | sí | no | Appwrite + local |
| Confirmar/rechazar | no | no | sí | Appwrite + publisher |
| Realtime | sí | sí | emisor vía function | Pusher |

---

## 1.1.4 Campos guardados en el pedido (Sale)

### Entidad de dominio (`shared-sale` → `Sale`)

| Campo requerido por 1.1.4 | Presente | Campo real |
|---------------------------|----------|------------|
| Cliente | **Sí** | `userId`, `customerName?` |
| Productos | **Sí** | `products: List<SaleItem>` |
| Cantidades | **Sí** | `SaleItem.quantity` |
| Montos | **Sí** | `amount` + `currency` (CUP/USD/MLC) |
| Estado | **Sí** | `verified: BuyState` (UNVERIFIED / VERIFIED / DELETED) |
| Timestamp / fecha | **Parcial** | `date: LocalDate` (solo día, **sin hora**) |

### Campos extra útiles ya modelados
- `deliveryType` (PICKUP / DELIVERY)
- `deliveryAddress` (provincia, municipio, calles, teléfono, etc.)
- `SaleItem.productName` (opcional, enriquecido en operador)

### Persistencia
- **Local:** Room (`SaleDto` entity) en Android; Dexie en web
- **Remoto:** Appwrite document con `user_id`, `customer_name`, `delivery_type`, `delivery_address`, etc.

### Gaps respecto a una auditoría contable estricta
1. **No hay timestamp con hora** (`LocalDate` vs `Instant` / `LocalDateTime`) → dificulta ordenar ventas del mismo día y conciliación fina.
2. **No hay precio unitario por línea** en `SaleItem` (solo `productId` + `quantity`); el monto total está en `Sale.amount`.
3. **No hay stock** asociado al producto ni movimiento de inventario al vender.

### Veredicto 1.1.4
Los pedidos **sí guardan** cliente, productos, cantidades, monto, estado y fecha.  
Para alinear con contabilidad (Fase 6) conviene enriquecer con **hora exacta** y, idealmente, **precio unitario por línea**.

---

## Resumen ejecutivo

| Micro-tarea | Estado | Notas |
|-------------|--------|-------|
| 1.1.1 Logs producción | Parcial | Sin Sentry/Appwrite access; documentada la brecha |
| 1.1.2 Top 5 fallos/riesgos | Hecho | 5 riesgos priorizados |
| 1.1.3 Flujo completo | Hecho | Documentado end-to-end |
| 1.1.4 Campos del pedido | Hecho | OK con gaps de hora y precio unitario |

**Siguiente paso natural:** 1.2 Corrección de bugs críticos (empezar por acoplamiento Telegram en registro de venta y validación del publisher), y/o cerrar observabilidad mínima para desbloquear 1.1.1 completo.
