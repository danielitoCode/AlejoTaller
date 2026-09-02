# Core 4 — Paridad panel (dash) vs operador (AlejoTaller scan)

**Fecha:** 2026-09-02 · rama `Core4`  
**Canónico completo:** [dash PARITY](https://github.com/danielitoCode/dash_alejo_taller/blob/Core4/.roadmap/Core4/PARITY_PANEL_OPERATOR.md)

## Escritura operador

| Campo Appwrite | Origen AT |
|----------------|-----------|
| `sale_id` | `SaleFinanceWrite.saleId` |
| `revenue` | `sale.amount` |
| `cogs` | Σ `unitCostSnapshot × qty` |
| `margin` | `revenue − cogs` |
| `user_id` | operador actual |
| `at` | ISO now |
| `currency` | `sale.currency.name` |
| `lines_json` | JSON camelCase (`productId`, `unitCostSnapshot`, …) |

`createIdempotent`: si ya hay doc por `sale_id`, devolver existente (B4).

Cliente web / MCP: **sin** write a finance.
