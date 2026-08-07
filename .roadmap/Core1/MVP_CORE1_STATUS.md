# Core 1 — Estado MVP (Auth + Sale + Soft-hold)

**Última actualización:** 2026-08-07  
**Veredicto desarrollo:** listo para QA (`QA_CORE1_CHECK_plan.md`).

## Resumen de alcance cerrado en código

| Área | Estado desarrollo | Evidencia |
|------|-------------------|-----------|
| Auth visitante (web + Android) | Hecho | Políticas AUTH + gates isGuest |
| Sale UNVERIFIED solo desde cliente | Hecho | RegisterNewSale + policy SALE |
| Soft-hold reserved en UNVERIFIED | Hecho | ApplySoftHold / RegisterNewSale |
| available = existence − reserved | Hecho | Check existencia antes de pedir |
| Operador VERIFIED consume stock | Hecho | ApplyOperatorStockDecision |
| Operador DELETED libera reserved | Hecho | Idem |
| SaleType NORMAL/DISCOUNT/GIFT | Hecho | UI chips + monto DISCOUNT |
| Tests parciales soft-hold | Hecho | Web + Android unit/instrumentation |
| UI badge available (listado + detalle) | Hecho | ProductCard / ProductDetail web; ProductItem / ProductDetail Android |
| stock_movements / reportes | **Core 2** | No bloquea QA Core 1 |

## Mapa respecto al roadmap original

| Fase original | Core | Notas |
|---------------|------|-------|
| Fase 1 estabilización | Core 1 | Auditoría + bugs críticos + validación vía QA |
| Fase 2 modelo existence (+ reserved) | Core 1 | reserved añadido por soft-hold |
| Fase 3 descuento en confirmación | Core 1 (soft-hold path) | Sin movimientos formales aún |
| Fase 3.2 reserva | Core 1 | Implementado como soft-hold |
| Fase 4 UI stock cliente | **Core 1** | Badges available cerrados 2026-08-07 |
| Fase 5 movimientos/reportes | Core 2 | |
| Fase 6 contabilidad | Core 2 | |

## Micro-tareas Core 1 (checklist desarrollo)

### Auth
- [x] Visitante vs autenticado alineado web/Android
- [x] Welcome solo primera visita; deeplink sin Welcome
- [x] Visitante no crea ventas

### Sale
- [x] Cliente solo UNVERIFIED
- [x] Telegram desacoplado del éxito de guardado
- [x] SaleType en operador + DISCOUNT editable

### Warehouse soft-hold
- [x] existence + reserved en modelo/sync
- [x] reserved += en UNVERIFIED (idempotente)
- [x] existence -= y release reserved en VERIFIED
- [x] release reserved en DELETED
- [x] Tests parciales web/Android
- [x] UI badge available en catálogo/detalle (web + Android)

### QA
- [ ] Checklist manual Web
- [ ] Checklist manual Android
- [ ] Checklist manual Operador
- [ ] E2E cruzado

Cuando el checklist QA esté en verde → **cerrar Core 1** formalmente.
