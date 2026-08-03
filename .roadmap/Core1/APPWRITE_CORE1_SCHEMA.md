# Appwrite — Schema mínimo Core 1 (stock soft-hold)

## products

| Atributo | Tipo | Required | Default | Notas |
|----------|------|----------|---------|-------|
| `existence` | Integer | sí (recomendado) | `0` | Stock físico |
| `reserved` | Integer | sí (recomendado) | `0` | Soft-hold UNVERIFIED |

**Reglas**
- No persistir `existence < 0` ni `reserved < 0`.
- `available` no es atributo remoto; se calcula en clientes/operador.

**Acción consola (si falta algún atributo)**
1. Collection productos → crear `reserved` Integer min 0 default 0.
2. Backfill reserved = 0 en documentos existentes.
3. existence ya usado por web; alinear valores con inventario físico.

## sales (campos relevantes Core 1)

Además de los campos históricos de Sale:

| Atributo | Notas |
|----------|-------|
| `verified` / buy_state | UNVERIFIED \| VERIFIED \| DELETED |
| `sale_type` o equivalente | NORMAL \| DISCOUNT \| GIFT (operador) |
| `amount` | Editable en DISCOUNT |
| flag hold aplicado | p.ej. `stock_hold_applied` si se modeló en documento |

## No requerido en Core 1

Collection `stock_movements` → documentada en Core 2 (`APPWRITE_STOCK_MOVEMENTS.md`).
