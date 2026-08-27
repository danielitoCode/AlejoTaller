# Roadmap AlejoTaller

Estructura por núcleo de entrega:

| Directorio | Alcance | Estado |
|------------|---------|--------|
| [`Core1/`](./Core1/) | Auth visitante, Sale UNVERIFIED + soft-hold, Warehouse (existence/reserved/available), operador confirma/rechaza + SaleType, tests, QA checklist | **Cerrado** (2026-08-12) |
| [`Core2/`](./Core2/) | `salida_venta` + finance al VERIFIED (operador); factura entrada / movements / COGS / reservas taller en **dash** | **Cerrado** (2026-08-24) |

**Cómo usar**
- Marca checkboxes `[x]` cuando verifiques.
- Core 1 y Core 2 están **cerrados** a nivel ecosistema.
- Detalle canónico de cierre dash: `dash_alejo_taller/.roadmap/Core2/`.

**Políticas de producto:** [`.policies/`](../.policies/)

### Post–Core 2 (no bloqueante)

- Ajuste de inventario (UI) — futura implementación
- Reserva taller desde cliente web (E2E)
- Smoke opcional en dispositivo físico operador
