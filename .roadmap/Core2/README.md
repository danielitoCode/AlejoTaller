# Core 2 — Inventario formal, UI stock, reportes y endurecimiento

**Estado:** checklist de implementación lista; código de movimientos aún no iniciado.  
**No bloquea** el uso del soft-hold de Core 1 ni el support web cerrado.

**Ejecución:** web + Appwrite + operador primero; Android cliente en sprints de paridad.

## Alcance previsto

| Tema | Descripción |
|------|-------------|
| stock_movements | Collection + dominio + escritura en confirmación/ajustes |
| UI stock cliente | Available / existence consistentes; stale indicator; clamp carrito (web first) |
| Ajustes y entradas | Operador/admin: entrada, ajuste, devolución |
| Reportes mínimos | Listado movimientos, alertas stock bajo |
| Contabilidad | Hora exacta en Sale, precio unitario por línea, conciliación |
| Observabilidad | Sentry / Crashlytics; logs publisher |
| Backend | Function atómica confirmSale + deduct stock (recomendado) |
| Seguridad | Endurecer publisher API key / permisos Appwrite |

## Índice

| Archivo | Descripción |
|---------|-------------|
| [**CHECKLIST_CORE2.md**](./CHECKLIST_CORE2.md) | **Checklist operativa** (marcar paso a paso; cada ítem con test) |
| [MVP_CORE2_BACKLOG.md](./MVP_CORE2_BACKLOG.md) | Vista resumida fases A–E |
| [DESIGN_STOCK_MOVEMENTS.md](./DESIGN_STOCK_MOVEMENTS.md) | Diseño movimientos |
| [APPWRITE_STOCK_MOVEMENTS.md](./APPWRITE_STOCK_MOVEMENTS.md) | Schema collection movimientos |

Support web Core 1: [../Core1/SUPPORT_CORE1_CLOSURE.md](../Core1/SUPPORT_CORE1_CLOSURE.md)  
Core 1: [../Core1/](../Core1/)
