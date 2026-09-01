# Core 3 — Compras y Abastecimiento (AlejoTaller)

**Estado:** listo para merge · rama `Core3`  
**Dependencia:** Core 2 cerrado  
**Canónico de orden / UI:** [dash_alejo_taller/.roadmap/Core3](https://github.com/danielitoCode/dash_alejo_taller/tree/Core3/.roadmap/Core3)

## Rol de este monorepo

Core 3 es **panel-first**. Aquí **no** se implementa UI de proveedores ni facturas de compra ni anulación.

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

- Docs + frontera de permisos: merge cuando el checklist AT esté marcado y el dash tenga B3.1 listo (ya cumplido).
- **No** hay código de anulación en cliente/operador — la anulación vive solo en el panel.
- Coordinar con el PR de `dash_alejo_taller` `Core3` → `master`.
