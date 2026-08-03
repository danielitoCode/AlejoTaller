# Core 2 — Backlog

Marcar al planificar sprints post–Core 1.

## Fase A — Movimientos formales

- [ ] A.1 Collection `stock_movements` en Appwrite + permisos operador/admin
- [ ] A.2 Dominio + DTO + repo (operador primero)
- [ ] A.3 Al VERIFIED: escribir `salida_venta` con balance_after
- [ ] A.4 Al ajuste/entrada/devolución: tipos correspondientes
- [ ] A.5 Tests de no negativo e idempotencia con movimientos

## Fase B — UI stock cliente

- [ ] B.1 Web: badge o texto available en listado/detalle
- [ ] B.2 Android: paridad UI
- [ ] B.3 Sync offline-first no muestra available stale sin indicador
- [ ] B.4 Límites de carrito guiados por available visible

## Fase C — Ajustes y reportes

- [ ] C.1 Pantalla operador/admin entrada de mercancía
- [ ] C.2 Ajuste manual con motivo
- [ ] C.3 Listado filtrable de movimientos
- [ ] C.4 Alerta stock_min (atributo opcional)

## Fase D — Contabilidad y datos Sale

- [ ] D.1 Timestamp con hora en Sale
- [ ] D.2 Precio unitario por línea (SaleItem.unitPrice ya parcial en dominio)
- [ ] D.3 Export / reporte ventas ↔ salidas de stock

## Fase E — Endurecimiento

- [ ] E.1 Sentry o Crashlytics en app + operador + web
- [ ] E.2 Rotar / endurecer PUBLISHER_API_KEY
- [ ] E.3 Function Appwrite atómica confirm + stock (reduce trust en APK operador)
- [ ] E.4 Revisión permisos Appwrite en writes sensibles

## Criterio de entrada Core 2

Core 1 QA (Web + Android + Operador) sin bloqueantes.
