# Roadmap AlejoTaller

Estructura por núcleo de entrega:

| Directorio | Alcance |
|------------|---------|
| [`Core1/`](./Core1/) | Auth visitante, Sale UNVERIFIED + soft-hold, Warehouse (existence/reserved/available), operador confirma/rechaza + SaleType, tests parciales, **QA checklist** |
| [`Core2/`](./Core2/) | stock_movements completos, UI stock cliente, ajustes/reportes, alineación contable, observabilidad, function atómica confirm+stock |

**Cómo usar**
- Marca checkboxes `[x]` cuando verifiques.
- Core 1 se cierra con QA manual desde posición de cliente + operador.
- Core 2 no bloquea el pase a QA de Core 1.

**Políticas de producto:** [`.policies/`](../.policies/)
