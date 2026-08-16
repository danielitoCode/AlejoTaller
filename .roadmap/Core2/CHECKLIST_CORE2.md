# Core 2 — Checklist de implementación

**Estado:** fase 0 parcial (0.1–0.2)  
**Última actualización:** 2026-08-16  
**Enfoque de ejecución:** priorizar **web + Appwrite + operador** en este monorepo; Android cliente en sprints de paridad cuando la pieza web/operador esté estable.

Marcar cada ítem al completar **código + test** (o evidencia de schema/consola cuando aplique).  
No marcar “hecho” solo por diseño documentado.

**Políticas / diseño de referencia**

| Fuente | Uso en Core 2 |
|--------|----------------|
| `.policies/warehouse/WAREHOUSE_POLICY.md` | Soft-hold intacto; Function + `StockMovement` |
| `.policies/sale/SALE_POLICY.md` | Persistencia movimientos; no reinterpretar precio en operador |
| `DESIGN_STOCK_MOVEMENTS.md` | Tipos, campos, quién escribe |
| `APPWRITE_STOCK_MOVEMENTS.md` | Schema collection y permisos |
| `MVP_CORE2_BACKLOG.md` | Fases A–E (mapa de alto nivel) |

**Criterio de entrada:** Core 1 usable (soft-hold + sale + support web). QA formal Core 1 puede seguir en paralelo.

**Criterio de salida Core 2:** todas las fases A–E con casillas críticas en verde; tests automatizados de dominio/repo en verde en CI web (y operador cuando exista suite).

---

## Convención de marcado

- `[ ]` pendiente  
- `[x]` hecho (código + test o evidencia schema)  
- Cada micro-tarea incluye sub-ítem **Test** obligatorio salvo tareas solo de consola Appwrite.

---

## Fase 0 — Preparación (web-first)

- [x] **0.1** Confirmar modelo + código Core 1 de `existence` / `reserved` operativos (capa app + tests)  
  - [x] **Evidencia código:** `product.net.repository` usa `incrementDocumentAttribute` / `decrementDocumentAttribute` sobre `reserved` (max=existence / min=0); collection productos web = `product`  
  - [x] **Evidencia tests (2026-08-16):** 20 tests soft-hold en verde — `RegisterNewSaleCaseUse.soft-hold`, `CheckAProductExistence`, `ReleaseSoftHoldCaseUse.atomic`, `product.offline-first.repository.atomic`  
  - [x] **Evidencia schema doc:** `.roadmap/Core1/APPWRITE_CORE1_SCHEMA.md` — atributos `existence` + `reserved` en products  
  - [x] **Evidencia operador:** `AppwriteOperatorStockRepository.applyDeltas` lee/escribe `existence` y `reserved`  
  - [ ] **Smoke live Appwrite (manual):** UNVERIFIED incrementa reserved; VERIFIED consume — *requiere consola/proyecto; no ejecutado desde este entorno sin credenciales*  
- [x] **0.2** Decidir IDs de collection `stock_movements` y roles (operador/admin write; cliente sin write)  
  - [x] **Decisión:** collectionId canónico = `stock_movements`; mismo databaseId que Core 1; clientes sin write/read MVP  
  - [x] **Evidencia:** actualizado `APPWRITE_STOCK_MOVEMENTS.md` (tabla IDs + permisos)  
  - [ ] **Crear collection en consola** (pasa a A.1) — *aún no existe en código ni env*  
- [ ] **0.3** Branch / convención de commits `feat(core2): …` y no mezclar con sprint Android support  

---

## Fase A — Movimientos formales (`stock_movements`)

Alineado a `DESIGN_STOCK_MOVEMENTS.md` y WAREHOUSE_POLICY § checklist Core 2.

### A.1 Schema Appwrite

- [ ] **A.1.1** Crear collection `stock_movements` con atributos: `product_id`, `type`, `quantity`, `balance_after`, `reason`, `user_id`, `sale_id?`  
  - [ ] **Evidencia:** consola / export; actualizar tabla en `APPWRITE_STOCK_MOVEMENTS.md` si hay desviaciones  
- [ ] **A.1.2** Índices: `product_id`, `type`, `sale_id`, `$createdAt`  
- [ ] **A.1.3** Permisos: solo operador/admin lectura+escritura; clientes sin create/update  

### A.2 Dominio y datos (operador primero; shared si aplica)

- [ ] **A.2.1** Entidad / enum de tipos: `entrada` \| `salida_venta` \| `ajuste` \| `devolucion`  
  - [ ] **Test:** validación `quantity > 0`, `balance_after >= 0`, type conocido  
- [ ] **A.2.2** DTO + mapper Appwrite ↔ dominio  
  - [ ] **Test:** mapper round-trip / fixtures  
- [ ] **A.2.3** `StockMovementRepository` (create + list by product / by sale) en superficie operador (y web admin si existe)  
  - [ ] **Test:** repo mock o integration con client de test; no usar cache local como autoridad  

### A.3 Escritura en confirmación de venta (VERIFIED)

- [ ] **A.3.1** Tras consume de stock en VERIFIED, persistir un movimiento `salida_venta` por línea (o agregado documentado) con `sale_id`, `balance_after` = existence post-mutación  
  - [ ] **Test:** case-use operador (p. ej. extensión de `ApplyOperatorStockDecision`): al VERIFIED se llama create movement; `balance_after` coherente  
- [ ] **A.3.2** Idempotencia: re-confirmar / reintento no duplica movimientos para el mismo `sale_id` + `product_id` (clave lógica o guard)  
  - [ ] **Test:** segunda ejecución no crea segundo documento  
- [ ] **A.3.3** Soft-hold **sin cambio semántico**: reserved UNVERIFIED igual que Core 1; el movimiento solo en VERIFIED (y tipos de ajuste)  
  - [ ] **Test:** regresión soft-hold web (`RegisterNewSale` / `CheckAProductExistence`) sigue en verde  

### A.4 Tipos no-venta

- [ ] **A.4.1** `entrada`: `existence += quantity` + movement  
  - [ ] **Test:** existence y `balance_after`  
- [ ] **A.4.2** `ajuste`: direction in/out + reason obligatorio + movement  
  - [ ] **Test:** no permite `balance_after < 0`  
- [ ] **A.4.3** `devolucion`: `existence += quantity` + movement opcionalmente ligado a `sale_id`  
  - [ ] **Test:** quantity > 0 y balance  

### A.5 Invariantes globales

- [ ] **A.5.1** Tests de no negativo en existence tras cualquier tipo  
- [ ] **A.5.2** Documentar limitación Core 1 restante hasta Function (E.3): sin transacción multi-documento Appwrite aún  

---

## Fase B — UI stock cliente (**web primero**)

Core 1 ya tiene badge available en listado/detalle. Core 2 endurece consistencia y carrito.

### B.1 Web

- [ ] **B.1.1** Listado y detalle muestran `available` (y opcionalmente existence) de forma consistente con `available = existence - reserved`  
  - [ ] **Test:** unitario de helper/presenter o test de componente con fixtures reserved > 0  
- [ ] **B.1.2** Indicador de datos stale / syncing cuando offline-first aún no refrescó post `stock:changed`  
  - [ ] **Test:** store/flag de sync; no afirmar available “fresco” sin evidencia  
- [ ] **B.1.3** Carrito / checkout no permite qty > available visible; clamp alineado a policy  
  - [ ] **Test:** case-use o store de carrito con available 2 y qty 5  

### B.2 Android cliente (paridad — sprint posterior)

- [ ] **B.2.1** Misma semántica available / stale / clamp  
  - [ ] **Test:** unitarios Android equivalentes  

---

## Fase C — Ajustes y reportes (operador)

- [ ] **C.1** Pantalla o flujo **entrada de mercancía** (productId, qty, reason) → A.4.1  
  - [ ] **Test:** ViewModel / case-use; no escribe si qty ≤ 0  
- [ ] **C.2** **Ajuste manual** con motivo obligatorio → A.4.2  
  - [ ] **Test:** rechazo sin reason; rechazo balance negativo  
- [ ] **C.3** Listado filtrable de movimientos (por producto, tipo, rango fechas)  
  - [ ] **Test:** filtros del repo o use-case con fixtures  
- [ ] **C.4** (Opcional) atributo `stock_min` + alerta UI si `available < stock_min`  
  - [ ] **Test:** regla de alerta pura  

---

## Fase D — Contabilidad y datos Sale

Alineado a SALE_POLICY (amount congelado; no reinterpretar en operador).

- [ ] **D.1** Timestamp de Sale con **hora** (no solo fecha) en persistencia y UI relevante  
  - [ ] **Test:** mapper/DTO preserva instate ISO con tiempo  
- [ ] **D.2** `unitPrice` (o equivalente) por línea de Sale estable en create y visible en operador  
  - [ ] **Test:** create sale no pierde unitPrice; operador lee el del documento  
- [ ] **D.3** Reporte mínimo: ventas VERIFIED ↔ movimientos `salida_venta` por `sale_id`  
  - [ ] **Test:** query/agregación con fixtures sale + movements  

---

## Fase E — Endurecimiento

- [ ] **E.1** Observabilidad web (Sentry u equivalente) en errores de sale/stock/support  
  - [ ] **Test / evidencia:** evento de prueba en entorno dev o flag documentado  
- [ ] **E.2** Rotar / restringir `PUBLISHER_API_KEY` (publisher function)  
  - [ ] **Evidencia:** runbook breve en function README  
- [ ] **E.3** Appwrite Function atómica: confirm sale + deduct stock + write `salida_venta` (reduce trust en APK operador)  
  - [ ] **Test:** tests de función o contrato (input VERIFIED → existence y movement)  
- [ ] **E.4** Revisión permisos Appwrite en writes sensibles (sale, product reserved/existence, stock_movements)  
  - [ ] **Evidencia:** checklist de permisos firmada en este archivo o anexo  

---

## Regresión obligatoria (no negociable al cerrar cada fase)

Tras cada fase A–C, en **web**:

- [ ] `npm ci` / `npm test` (suite unitaria) en verde  
- [ ] `npm run build` en verde  
- [ ] Soft-hold + CheckAProductExistence sin regresiones  
- [ ] Support web no roto (rutas + build)

---

## Registro de progreso

| Fecha | Fase / ítem | Commit / nota |
|-------|-------------|----------------|
| 2026-08-16 | Checklist creado | Ningún ítem de implementación marcado |
| 2026-08-16 | 0.1 código+tests; 0.2 decisión IDs | Soft-hold 20 tests OK; collection pendiente consola |
| | | |

Al completar un bloque, añadir fila aquí y marcar casillas arriba.
