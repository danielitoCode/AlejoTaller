# Core 2 — Inventario formal, UI stock, reportes y endurecimiento

**Estado:** backlog tras cierre QA de Core 1.  
**No bloquea** el pase a QA ni el uso del soft-hold de Core 1.

## Alcance previsto

| Tema | Descripción |
|------|-------------|
| stock_movements | Collection + dominio + escritura en confirmación/ajustes |
| UI stock cliente | Mostrar available / existence en catálogo y detalle (web + Android) |
| Ajustes y entradas | Operador/admin: entrada, ajuste, devolución |
| Reportes mínimos | Listado movimientos, alertas stock bajo |
| Contabilidad | Hora exacta en Sale, precio unitario por línea, conciliación |
| Observabilidad | Sentry / Crashlytics; logs publisher |
| Backend | Function atómica confirmSale + deduct stock (opcional pero recomendado) |
| Seguridad | Endurecer publisher API key / permisos Appwrite |

## Índice

| Archivo | Descripción |
|---------|-------------|
| [MVP_CORE2_BACKLOG.md](./MVP_CORE2_BACKLOG.md) | Fases y micro-tareas Core 2 |
| [DESIGN_STOCK_MOVEMENTS.md](./DESIGN_STOCK_MOVEMENTS.md) | Diseño movimientos (ex Fase 2.1 parcial) |
| [APPWRITE_STOCK_MOVEMENTS.md](./APPWRITE_STOCK_MOVEMENTS.md) | Schema collection movimientos |

Core 1 (cerrado en desarrollo, en QA): [../Core1/](../Core1/)
