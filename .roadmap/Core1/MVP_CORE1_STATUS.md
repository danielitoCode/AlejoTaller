# Core 1 — Estado MVP (Auth + Sale + Soft-hold)

**Última actualización:** 2026-08-08  
**Veredicto desarrollo:** listo para QA (`QA_CORE1_CHECK_plan.md`), con alineaciones RT/currency/UI documentadas.

## Resumen de alcance cerrado en código

| Área | Estado desarrollo | Evidencia |
|------|-------------------|-----------|
| Auth visitante (web + Android) | Hecho | Políticas AUTH + gates isGuest |
| Sale UNVERIFIED solo desde cliente | Hecho | RegisterNewSale + policy SALE |
| Soft-hold reserved en UNVERIFIED | Hecho | ApplySoftHold / RegisterNewSale (atómico Appwrite) |
| available = existence − reserved | Hecho | Check + cart clamp |
| Operador VERIFIED consume stock | Hecho | ApplyOperatorStockDecision |
| Operador DELETED libera reserved | Hecho | Idem |
| SaleType NORMAL/DISCOUNT/GIFT | Hecho | UI chips + monto DISCOUNT |
| Tests parciales soft-hold | Hecho | Web + Android unit/instrumentation |
| UI badge available (listado + detalle) | Hecho | ProductCard / ProductDetail web; ProductItem / ProductDetail Android |
| Appwrite Realtime stock (web + Android) | Hecho | Snapshot → Dexie/Room; feedback UI |
| Appwrite Realtime sale verification (web + Android) | Hecho | buy_state → UI; sin secret Pusher en cliente |
| Operador muestra currency del pedido | Hecho | OperatorConfirmPaymentScreen + notices |
| UI web carrito + reservas | Hecho | Responsive, iconos, animaciones |
| stock_movements / reportes | **Core 2** | No bloquea QA Core 1 |
| Operador stock 100% atómico | **Core 2** | RMW aceptable Core 1 |

## Mapa respecto al roadmap original

| Fase original | Core | Notas |
|---------------|------|-------|
| Fase 1 estabilización | Core 1 | Auditoría + bugs críticos + validación vía QA |
| Fase 2 modelo existence (+ reserved) | Core 1 | reserved añadido por soft-hold |
| Fase 3 descuento en confirmación | Core 1 (soft-hold path) | Sin movimientos formales aún |
| Fase 3.2 reserva | Core 1 | Implementado como soft-hold |
| Fase 4 UI stock cliente | **Core 1** | Badges + RT Appwrite |
| Fase 5 movimientos/reportes | Core 2 | |
| Fase 6 contabilidad | Core 2 | |

## Micro-tareas Core 1 (checklist desarrollo)

### Auth
- [x] Visitante vs autenticado alineado web/Android
- [x] Welcome solo primera visita; deeplink sin Welcome
- [x] Visitante no crea ventas
- [x] Topbar acceso login visitante + loading login/registro (UX)

### Sale
- [x] Cliente solo UNVERIFIED
- [x] Telegram desacoplado del éxito de guardado
- [x] SaleType en operador + DISCOUNT editable
- [x] Currency del cliente visible en operador (sin reconversión tasa)

### Warehouse soft-hold
- [x] existence + reserved en modelo/sync
- [x] reserved += en UNVERIFIED (idempotente, atómico)
- [x] existence -= y release reserved en VERIFIED
- [x] release reserved en DELETED
- [x] Compensación multi-línea best-effort Core 1
- [x] Tests parciales web/Android
- [x] UI badge available en catálogo/detalle (web + Android)
- [x] Realtime stock vía Appwrite (web + Android cliente)

### Realtime / clientes
- [x] Sale verification vía Appwrite Realtime (web + Android)
- [x] Pusher no requerido para stock ni sale en clientes migrados (promo residual)

### UI web
- [x] Carrito y Mis reservas modernizados (responsive + iconos + motion)

### QA
- [ ] Checklist manual Web (re-smoke A1/A2 + A3–A5 + RT)
- [ ] Checklist manual Android
- [ ] Checklist manual Operador (incl. C1.5 currency)
- [ ] E2E cruzado

Cuando el checklist QA esté en verde → **cerrar Core 1** formalmente.

## Impacto sobre ítems QA ya marcados

Los `[X]` de **A1 Auth** y **A2.1–A2.3** no se reabren por las alineaciones de RT/currency/UI.  
**A2.4** queda en código cumplido; requiere **re-verificación de UI** del carrito.  
Detalle en `QA_CORE1_CHECK_plan.md` §0.6 y §R.
