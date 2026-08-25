# Smoke tests — MCP Cliente

**Worker:** `https://alejotaller-mcp.daniel-imbert96.workers.dev`  
**Fecha checklist:** 2026-08-25  
**Estado:** **cerrado** — smokes y funcionalidades verificadas (2026-08-25)

---

## A. Automatizado (CI / local)

### A1. Health HTTP (sin secretos)

```bash
cd mcp
npm run smoke:health
```

Espera JSON con `status: ok|degraded`, `worker: alejotaller-mcp`, `scope: b2c-customer`.

### A2. Unit + registry smoke

```bash
npm test
```

Incluye `test/smoke/tool-registry.smoke.test.ts` (18 tools + policies).

### A3. Live health en vitest

```bash
npm run test:smoke-live
```

---

## B. Agente conversacional

| # | Prompt usuario | Tool esperada | Criterio OK |
|---|----------------|---------------|------------|
| 1 | ¿Qué productos hay? | `list_products` | Lista con `availableUnits` / `inStock` |
| 2 | ¿Cómo va mi pedido X? | `get_order` | Solo si es del user |
| 3 | Cancela mi pedido pendiente | confirmación → `cancel_order` | Soft-hold liberado |
| 4 | Abre un ticket | `create_support_thread` | Thread propio |
| 5 | Quiero comprar … | confirmación → `create_order` | UNVERIFIED + hold |

**Firma smoke / funcionalidades:** verificado por equipo · **2026-08-25** ✅

---

## C. Criterios de cierre formal

| Criterio | Estado |
|----------|--------|
| Deploy estable workers.dev | ✅ |
| `tools/list` 18 tools + policies | ✅ |
| Lecturas con identidad | ✅ |
| Mutaciones confirmación + ownership | ✅ |
| Soft-hold create/cancel | ✅ |
| Auth JWT + header modes | ✅ |
| Tests + smoke health CI | ✅ |
| Docs monorepo | ✅ |
| Funcionalidades en prod comprobadas | ✅ 2026-08-25 |
