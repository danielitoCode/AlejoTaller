# Core 4 — Finanzas de Ventas (AlejoTaller)

**Estado:** abierto · rama `Core4`  
**Dependencia:** Core 2 cerrado · Core 3 (espejo / costos en panel)  
**Canónico de orden / política:** [dash_alejo_taller/.roadmap/Core4](https://github.com/danielitoCode/dash_alejo_taller/tree/Core4/.roadmap/Core4)

## Rol de este monorepo

Core 4 es **paridad financiera** al confirmar ventas. El contrato lo define el panel; aquí se implementa el **write del operador** y se protege la frontera B2C.

| Superficie | Qué hacer en Core 4 |
|------------|---------------------|
| **alejotallerscan (operador)** | Al VERIFIED: `sale_finance_event` con **snapshot por línea** (mismo contrato que el panel); idempotente por `sale_id` |
| **web / app (cliente)** | **Nada** de write finance; solo crea `UNVERIFIED` |
| **mcp** | Sin tools que escriban `sale_finance_event` |
| **docs / policies** | Espejo de checklist y notas de paridad |

## Qué ya resolvió Core 2 (no repetir desde cero)

- Operador: `salida_venta` + `sale_finance_event` agregado (revenue / cogs / margin) al confirm
- COGS = Σ `last_unit_cost × qty` leído en el momento del confirm
- Idempotencia básica por `sale_id`
- DELETED sin finance

## Objetivo de este espejo

1. Alinear el write del operador al contrato Core 4 (líneas + `unit_cost_snapshot`)
2. Garantizar que un reintento o un cambio posterior de `last_unit_cost` **no** duplique ni reescriba el evento
3. Mantener cliente/MCP sin capacidad de crear ingresos

## Documentos

| Doc | Rol |
|------|-----|
| [CORE4_UNIFIED_CHECKLIST.md](./CORE4_UNIFIED_CHECKLIST.md) | Ítems **AT** del orden B0–B6 |
| [MVP_CORE4_STATUS.md](./MVP_CORE4_STATUS.md) | Estado vivo de este monorepo |

## Criterio de merge a `master`

- Código operador + frontera + docs alineados al checklist AT.
- Coordinar con el PR de `dash_alejo_taller` `Core4` → `master`.
- No merge a producción hasta CI verde en ambos.
