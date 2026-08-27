# Core 3 — Compras y Abastecimiento (AlejoTaller)

**Estado:** en preparación · rama `Core3`  
**Dependencia:** Core 2 cerrado  
**Canónico de orden / UI:** [dash_alejo_taller/.roadmap/Core3](https://github.com/danielitoCode/dash_alejo_taller/tree/Core3/.roadmap/Core3)

## Rol de este monorepo

Core 3 es **panel-first**. Aquí **no** se implementa UI de proveedores ni facturas de compra.

| Superficie | Qué hacer en Core 3 |
|------------|---------------------|
| **web / app (cliente)** | Nada de compras; sin write a `supplier` / `purchase_*` |
| **alejotallerscan (operador)** | Seguir usando `last_unit_cost` para COGS al VERIFIED; no registrar entradas |
| **mcp** | Sin tools de abastecimiento (sigue scope B2C) |
| **docs / policies** | Espejo de política y checklist; verificar no regresión |

## Qué ya resolvió Core 2 (no repetir)

- Operador: `salida_venta` + `sale_finance_event` al confirm
- Dash: factura de entrada MVP + movements `entrada` + `last_unit_cost`

## Objetivo de este espejo

1. Documentar dependencias respecto al dash (quién bloquea a quién)
2. Garantizar que actualizaciones de costo en panel no rompan confirm del operador
3. Mantener frontera B2C: cliente nunca escribe abastecimiento

## Checklist

Ver [`CORE3_UNIFIED_CHECKLIST.md`](./CORE3_UNIFIED_CHECKLIST.md) (alineado al dash).

## Criterio de merge a `master`

- Si solo hubo docs: merge docs cuando el checklist AT esté marcado y no haya contradicción con dash.
- Si hubo código (permisos, tests operador): merge **después** de que dash tenga B1+B2 estables en su `Core3` o ya en `master`, y CI verde.
- **No** mergear cambios que asuman anulación de entradas (B3 dash) hasta que la política B3 esté implementada o explícitamente no usada.

Recomendación operativa: trabajar en `Core3`; abrir PR a `master` solo cuando el dash indique B6 o release parcial (B1+B2+B4) listo.
