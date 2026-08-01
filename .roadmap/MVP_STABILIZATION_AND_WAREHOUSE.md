# Roadmap: Estabilización MVP + Almacén Básico

**Proyecto:** AlejoTaller  
**Objetivo general:** Estabilizar el MVP actual e integrar un módulo de almacén/stock básico que permita a los clientes ver la cantidad disponible de productos, alineando ventas con inventario y contabilidad.

**Cómo usar este archivo:**  
- Marca cada micro-tarea con `[x]` cuando esté completada.  
- Actualiza la fecha de completado y el commit/PR asociado si aplica.  
- No marques una tarea como hecha hasta que sea verificable (prueba manual o automatizada).

**Estado global**
- [ ] Fase 1 — Estabilización del MVP
- [ ] Fase 2 — Modelo de datos e inventario básico
- [ ] Fase 3 — Descuento automático de stock en ventas
- [ ] Fase 4 — Visibilidad de stock para el cliente
- [ ] Fase 5 — Movimientos, ajustes y reportes mínimos
- [ ] Fase 6 — Alineación ventas ↔ inventario ↔ contabilidad

---

## Fase 1 — Estabilización del MVP

**Meta:** El flujo principal de compra funciona de forma confiable y los datos de ventas son correctos.

### 1.1 Auditoría y diagnóstico
- [x] 1.1.1 – 1.1.4 — *ver AUDIT_1_1.md*

### 1.2 Corrección de bugs críticos
- [x] 1.2.1 Checkout / Telegram desacoplado
- [ ] 1.2.2 Auth/sesión
- [x] 1.2.3 / 1.2.4 Operador-Pusher (parcial código)

### 1.3 Validación del flujo estable
- [ ] 1.3.1 – 1.3.3 (validación manual pendiente)

---

## Fase 2 — Modelo de datos e inventario básico

**Meta:** Tener stock real asociado a cada producto y capacidad de registrar movimientos.

### 2.1 Diseño del modelo
- [x] 2.1.1 – 2.1.3 — *ver DESIGN_2_1_STOCK_MODEL.md*

**Diseño:** [`.roadmap/DESIGN_2_1_STOCK_MODEL.md`](./DESIGN_2_1_STOCK_MODEL.md)

### 2.2 Implementación del modelo
- [x] 2.2.1 Schema Appwrite documentado (`existence` + `stock_movements`) — *ver APPWRITE_STOCK_SCHEMA.md; creación en consola pendiente del operador*
- [x] 2.2.2 Modelos Android + Web: `existence` en Product/DTO/mappers; `StockMovement` dominio; Room v11
- [x] 2.2.3 Procedimiento de carga inicial documentado en APPWRITE_STOCK_SCHEMA.md
- [x] 2.2.4 Validación `existence >= 0` en dominio Android y `createProduct` web

**Schema:** [`.roadmap/APPWRITE_STOCK_SCHEMA.md`](./APPWRITE_STOCK_SCHEMA.md)

**Nota build:** el sandbox de CI/agente no tiene Android SDK; validar localmente con:
```bash
./gradlew :app:compileDebugKotlin
./gradlew :app:testDebugUnitTest --tests "*ProductsCaseUseTest*"
```

**Criterio de salida Fase 2:** Cada producto tiene un stock numérico en modelo de código; collection de movimientos definida; falta aplicar atributos en consola Appwrite y backfill real.

---

## Fase 3 — Descuento automático de stock en ventas

**Meta:** Toda venta confirmada en **alejotallerscan** descuenta stock.

### 3.1 Lógica de descuento
- [ ] 3.1.1 Al confirmar (VERIFIED) descontar `existence` por línea
- [ ] 3.1.2 Crear movimiento `salida_venta`
- [ ] 3.1.3 Evitar race conditions
- [ ] 3.1.4 Stock insuficiente al confirmar

### 3.2 Reserva opcional
- [ ] 3.2.1 – 3.2.3

### 3.3 Pruebas
- [ ] 3.3.1 – 3.3.3

---

## Fase 4 — Visibilidad de stock para el cliente (app + web)

- [ ] 4.1.1 – 4.1.5 UI paridad
- [ ] 4.2.1 – 4.2.2 Sync offline-first

---

## Fase 5 — Movimientos, ajustes y reportes

- [ ] 5.1 – 5.3

---

## Fase 6 — Alineación ventas ↔ inventario ↔ contabilidad

- [ ] 6.1 – 6.3

---

**Última actualización:** 2026-08-01  
**Diseño 2.1:** `.roadmap/DESIGN_2_1_STOCK_MODEL.md`  
**Schema 2.2:** `.roadmap/APPWRITE_STOCK_SCHEMA.md`
