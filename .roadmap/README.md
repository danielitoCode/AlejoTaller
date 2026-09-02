# Roadmap AlejoTaller

Estructura por núcleo de entrega:

| Directorio | Alcance | Estado |
|------------|---------|--------|
| [`Core1/`](./Core1/) | Auth visitante, Sale UNVERIFIED + soft-hold, Warehouse, operador confirm/reject, QA | **Cerrado** (2026-08-12) |
| [`Core2/`](./Core2/) | `salida_venta` + finance al VERIFIED (operador); factura/movements/COGS/reservas en **dash** | **Cerrado** (2026-08-24) |
| [`Core3/`](./Core3/) | Compras y abastecimiento (espejo; UI en dash) | **Listo para merge** · rama `Core3` |
| [`Core4/`](./Core4/) | Finanzas de ventas — paridad operador (snapshot COGS / líneas) + frontera cliente | **Abierto** · rama `Core4` |

**Cómo usar**
- Marca checkboxes `[x]` cuando verifiques.
- Core 1 y Core 2 cerrados a nivel ecosistema.
- **Core 3:** release mínimo del panel (B3.1) listo; este monorepo solo docs/frontera — PR `Core3` → `master`.
- **Core 4:** trabajo en `Core4`; canónico de política/checklist en dash; aquí B3 operador + frontera.

**Políticas de producto:** [`.policies/`](../.policies/)

### Post–Core 2 / Core 3 (no bloqueante)

- Ajuste de inventario (UI) — futura implementación
- B3.2 corrección parcial de entradas (solo dash)
- Reserva taller desde cliente web (E2E)
- Smoke opcional en dispositivo físico operador
