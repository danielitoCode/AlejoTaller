# Roadmap AlejoTaller

Estructura por núcleo de entrega:

| Directorio | Alcance | Estado |
|------------|---------|--------|
| [`Core1/`](./Core1/) | Auth visitante, Sale UNVERIFIED + soft-hold, Warehouse, operador confirm/reject, QA | **Cerrado** (2026-08-12) |
| [`Core2/`](./Core2/) | `salida_venta` + finance al VERIFIED (operador); factura/movements/COGS/reservas en **dash** | **Cerrado** (2026-08-24) |
| [`Core3/`](./Core3/) | Compras y abastecimiento (espejo; UI en dash) | **En curso** · rama `Core3` |

**Cómo usar**
- Marca checkboxes `[x]` cuando verifiques.
- Core 1 y Core 2 cerrados a nivel ecosistema.
- **Core 3:** trabajar en rama `Core3`; merge a `master` solo bajo criterio B6 del checklist.

**Políticas de producto:** [`.policies/`](../.policies/)

### Post–Core 2 (no bloqueante, fuera de Core 3 UI)

- Ajuste de inventario (UI) — futura implementación
- Reserva taller desde cliente web (E2E)
- Smoke opcional en dispositivo físico operador
