# Protección de rama `Core3` (AlejoTaller)

## CI ya cableado

En push/PR a **`Core3`** (con path filters):

| Workflow | Job check |
|----------|-----------|
| **CI Web** | Web install + build + tests |
| **CI Android Cliente** | Cliente tests + compile |
| **CI Android Operador** | Operador tests + compile |
| **CI MCP** | MCP typecheck + tests (+ smoke health) |
| **CI Gate** | All surfaces green (agrega tras los parciales) |

Los path filters limitan runs innecesarios; un push solo de docs de `.roadmap/` **no** dispara web/android/mcp. Para forzar gate: `workflow_dispatch` o tocar un path cubierto.

## Branch protection (Settings → Branches)

Aplicar en la UI de GitHub (API no disponible desde el agente):

1. **Settings** → **Branches** → Add rule / ruleset  
2. Pattern: `Core3`  
3. **Require status checks to pass before merging**  
4. Tras un run verde, marcar como required según superficie tocada:
   - `Web install + build + tests`
   - `Cliente tests + compile`
   - `Operador tests + compile`
   - `MCP typecheck + tests`
   - Opcional: `All surfaces green` (CI Gate)
5. Restrict pushes / require PR según tu flujo.

Merge **Core3 → master**: usar la protección de `master` con los mismos checks.

## Nota Core 3

La mayor parte del trabajo de compras es en **dash**. En este monorepo Core 3 es espejo (docs/permisos); el CI evita regresiones si se toca operador/web/mcp.
