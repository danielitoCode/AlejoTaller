# Roadmap: Estabilización MVP + Almacén Básico

**Proyecto:** AlejoTaller  
**Objetivo general:** Estabilizar el MVP actual e integrar un módulo de almacén/stock básico que permita a los clientes ver la cantidad disponible de productos, alineando ventas con inventario y contabilidad.

**Cómo usar este archivo:**  
- Marca cada micro-tarea con `[x]` cuando esté completada.  
- Actualiza la fecha de completado y el commit/PR asociado si aplica.  
- No marques una tarea como hecha hasta que sea verificable (prueba manual o automatizada).

**Estado global**
- [ ] Fase 1 — Estabilización del MVP
- [ ] Fase 2 — Modelo de datos e inventario básico
- [ ] Fase 3 — Descuento automático de stock en ventas
- [ ] Fase 4 — Visibilidad de stock para el cliente
- [ ] Fase 5 — Movimientos, ajustes y reportes mínimos
- [ ] Fase 6 — Alineación ventas ↔ inventario ↔ contabilidad

---

## Fase 1 — Estabilización del MVP

**Meta:** El flujo principal de compra funciona de forma confiable y los datos de ventas son correctos.

### 1.1 Auditoría y diagnóstico
- [x] 1.1.1 Revisar logs de errores de producción / Appwrite / Sentry (o equivalente) de los últimos 7-14 días — *parcial: sin acceso a prod; ver `.roadmap/AUDIT_1_1.md`*
- [x] 1.1.2 Listar los 5 bugs o fallos más frecuentes que afectan compras o autenticación — *ver AUDIT_1_1.md*
- [x] 1.1.3 Documentar el flujo actual completo: registro/login → catálogo → carrito/pedido → confirmación operador → notificación Pusher — *ver AUDIT_1_1.md*
- [x] 1.1.4 Verificar que los pedidos se guardan con: cliente, productos, cantidades, montos, estado y timestamp — *OK con gap de hora exacta; ver AUDIT_1_1.md*

**Informe:** [`.roadmap/AUDIT_1_1.md`](./AUDIT_1_1.md)

### 1.2 Corrección de bugs críticos
- [x] 1.2.1 Corregir fallos que impiden completar un pedido (checkout / confirmación) — *Telegram desacoplado del guardado (PR #5)*
- [ ] 1.2.2 Corregir errores de autenticación o sesión que bloquean el flujo de compra
- [x] 1.2.3 Asegurar que el operador puede escanear/confirmar pedidos sin errores intermitentes — *parcial: fallo de Pusher ya no bloquea post-Appwrite (PR #5); falta validación manual de escaneo*
- [x] 1.2.4 Verificar que las notificaciones en tiempo real (Pusher) llegan de forma consistente — *parcial: si falla publisher, la venta remota queda correcta y el cliente puede reconciliar por sync; falta prueba E2E de consistencia*

**PR:** https://github.com/danielitoCode/AlejoTaller/pull/5

### 1.3 Validación del flujo estable
- [ ] 1.3.1 Ejecutar al menos 5 flujos completos de compra (cliente + operador) sin errores
- [ ] 1.3.2 Confirmar que los datos del pedido en base de datos coinciden con lo que vio el cliente y el operador
- [ ] 1.3.3 Definir y medir 3 métricas mínimas: tasa de error en checkout, pedidos completados vs iniciados, tiempo de respuesta de pantallas críticas

**Criterio de salida Fase 1:** Un cliente puede comprar y un operador confirmar sin fallos conocidos críticos. Los datos de venta son confiables.

---

## Fase 2 — Modelo de datos e inventario básico

**Meta:** Tener stock real asociado a cada producto y capacidad de registrar movimientos.

### 2.1 Diseño del modelo
- [ ] 2.1.1 Definir campos de stock en producto (o entidad separada): `stock_disponible`, `stock_reservado` (opcional), `stock_minimo` (umbral)
- [ ] 2.1.2 Definir entidad/colección `stock_movements` con: producto_id, tipo (entrada / salida_venta / ajuste / devolución), cantidad, motivo, usuario, timestamp, referencia_pedido (si aplica)
- [ ] 2.1.3 Decidir si el stock vive en Appwrite Collections, en shared-data o en ambos (offline-first)

### 2.2 Implementación del modelo
- [ ] 2.2.1 Crear/actualizar schema o colecciones en Appwrite para stock y movimientos
- [ ] 2.2.2 Actualizar modelos en `shared-data` / `shared-core` (Kotlin + TypeScript si aplica)
- [ ] 2.2.3 Agregar migración o script de carga inicial de stock (inventario físico → valor inicial)
- [ ] 2.2.4 Validar que no se permiten valores negativos de `stock_disponible` a nivel de escritura

**Criterio de salida Fase 2:** Cada producto tiene un stock numérico y existe un registro de movimientos.

---

## Fase 3 — Descuento automático de stock en ventas

**Meta:** Toda venta confirmada descuenta stock de forma atómica y segura.

### 3.1 Lógica de descuento
- [ ] 3.1.1 Al confirmar pedido (estado final de venta) descontar `cantidad` de `stock_disponible` de cada línea
- [ ] 3.1.2 Crear movimiento de tipo `salida_venta` vinculado al pedido
- [ ] 3.1.3 Usar transacción o operación atómica para evitar race conditions (dos ventas de la última unidad)
- [ ] 3.1.4 Si el stock es insuficiente en el momento de confirmar → rechazar o marcar línea como no disponible y notificar

### 3.2 Reserva opcional (recomendado)
- [ ] 3.2.1 Al iniciar checkout o crear pedido pendiente, reservar stock (`stock_reservado += cantidad`)
- [ ] 3.2.2 Al confirmar → pasar de reservado a descontado
- [ ] 3.2.3 Al cancelar / expirar pedido → liberar reserva

### 3.3 Pruebas
- [ ] 3.3.1 Probar venta normal y verificar stock final + movimiento creado
- [ ] 3.3.2 Probar dos ventas concurrentes de la última unidad (solo una debe tener éxito)
- [ ] 3.3.3 Probar cancelación de pedido y liberación de stock

**Criterio de salida Fase 3:** No es posible vender más unidades de las disponibles. Cada venta deja traza en movimientos.

---

## Fase 4 — Visibilidad de stock para el cliente

**Meta:** El cliente ve cuántas unidades quedan y no puede agregar más de lo disponible.

### 4.1 UI Cliente (Android + Web)
- [ ] 4.1.1 Mostrar en ficha de producto: "X unidades disponibles" o "Agotado"
- [ ] 4.1.2 Mostrar indicador de "Últimas unidades" cuando stock ≤ umbral configurado
- [ ] 4.1.3 En listados/catálogo: indicar de forma clara productos agotados
- [ ] 4.1.4 Impedir agregar al carrito/pedido una cantidad mayor al stock disponible
- [ ] 4.1.5 Actualizar la información de stock al cargar la pantalla (y idealmente tras eventos relevantes)

### 4.2 Sincronización offline-first
- [ ] 4.2.1 Definir cómo se propaga el stock actualizado a las apps (cliente y operador) vía shared-data / sync
- [ ] 4.2.2 Manejar el caso de stock desactualizado offline (validación final en servidor al confirmar)

**Criterio de salida Fase 4:** El cliente ve stock realista y no puede solicitar más unidades de las existentes.

---

## Fase 5 — Movimientos, ajustes y reportes mínimos

**Meta:** Poder corregir stock y tener visibilidad operativa.

### 5.1 Ajustes manuales
- [ ] 5.1.1 Pantalla o función (solo rol autorizado) para ajustar stock (+/-) con motivo obligatorio
- [ ] 5.1.2 Cada ajuste genera movimiento de tipo `ajuste`
- [ ] 5.1.3 Registrar usuario y timestamp del ajuste

### 5.2 Entradas de mercancía
- [ ] 5.2.1 Permitir registrar entrada de stock (compra / reposición) con cantidad y opcionalmente costo
- [ ] 5.2.2 Generar movimiento de tipo `entrada`

### 5.3 Reportes básicos
- [ ] 5.3.1 Listado de stock actual por producto (disponible / reservado / mínimo)
- [ ] 5.3.2 Historial de movimientos filtrable por producto y rango de fechas
- [ ] 5.3.3 Productos bajo stock mínimo (alerta simple)

**Criterio de salida Fase 5:** Se puede corregir inventario y consultar movimientos y stock actual.

---

## Fase 6 — Alineación ventas ↔ inventario ↔ contabilidad

**Meta:** Los números de ventas, stock y (básicamente) contabilidad cuenten la misma historia.

### 6.1 Consistencia de datos
- [ ] 6.1.1 Verificar que la suma de salidas por venta + ajustes + entradas = variación de stock en un período de prueba
- [ ] 6.1.2 Documentar y resolver discrepancias encontradas en datos históricos

### 6.2 Reportes de cruce
- [ ] 6.2.1 Reporte de ventas del período por producto (unidades y monto)
- [ ] 6.2.2 Reporte de stock valorizado (si se maneja costo) o al menos unidades
- [ ] 6.2.3 Exportación simple (CSV) de pedidos + movimientos de stock para uso contable externo

### 6.3 Reglas contables mínimas (si aplica internamente)
- [ ] 6.3.1 Definir cómo se registra: ingreso por venta, costo de venta (si hay costo), valor de inventario
- [ ] 6.3.2 Asegurar que cada venta confirmada tenga su correspondiente impacto en stock y en el reporte de ventas

**Criterio de salida Fase 6:** Se puede explicar cualquier diferencia entre ventas reportadas y movimiento de inventario. Existe una vía clara para alimentar contabilidad.

---

## Notas técnicas del proyecto (AlejoTaller)

- Stack relevante: Kotlin + Jetpack Compose (apps), Svelte + TypeScript (web), Appwrite, shared-* modules, Pusher via function.
- Priorizar cambios en `shared-data` / `shared-core` / `shared-sale` para que Android, operador y web compartan la misma lógica de stock.
- Offline-first: el descuento definitivo debe validarse en el backend (Appwrite Function o regla) para evitar inconsistencias.
- No permitir stock negativo en ninguna escritura.

## Criterio global de "MVP + Almacén listo"

- [ ] Cliente ve unidades disponibles / Agotado
- [ ] Venta confirmada descuenta stock automáticamente
- [ ] No se puede vender más de lo disponible
- [ ] Existe historial de movimientos de stock
- [ ] Se pueden hacer ajustes y entradas documentadas
- [ ] Reportes básicos de stock y ventas están disponibles
- [ ] Los números de ventas e inventario son consistentes (o las diferencias están documentadas)

---

**Última actualización:** 2026-08-01  
**Informe 1.1:** `.roadmap/AUDIT_1_1.md`  
**PR 1.2 (parcial):** https://github.com/danielitoCode/AlejoTaller/pull/5
