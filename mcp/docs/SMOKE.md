# Smoke tests — MCP Cliente

**Worker:** `https://alejotaller-mcp.daniel-imbert96.workers.dev`  
**Fecha checklist:** 2026-08-25

---

## A. Automatizado (CI / local)

### A1. Health HTTP (sin secretos)

```bash
cd mcp
npm run smoke:health
```

Opcional URL:

```bash
# Windows PowerShell
$env:MCP_SMOKE_URL="https://alejotaller-mcp.daniel-imbert96.workers.dev"; npm run smoke:health

# Windows CMD
set MCP_SMOKE_URL=https://alejotaller-mcp.daniel-imbert96.workers.dev&& npm run smoke:health

# Unix
MCP_SMOKE_URL=https://alejotaller-mcp.daniel-imbert96.workers.dev npm run smoke:health
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

(Cross-platform: no uses `MCP_SMOKE_LIVE=1` delante en CMD de Windows.)

---

## B. Agente conversacional (manual / staging)

Configurar el host MCP con:

| Header / meta | Valor |
|---------------|--------|
| `Authorization` | `Bearer <appwrite-jwt>` **o** |
| `X-Customer-Id` | Appwrite user `$id` del cliente de prueba |

Escenarios DoD:

| # | Prompt usuario | Tool esperada | Criterio OK |
|---|----------------|---------------|------------|
| 1 | ¿Qué productos hay? | `list_products` | Lista con `availableUnits` / `inStock` |
| 2 | ¿Cómo va mi pedido X? | `get_order` | Solo si es del user; si no → no autorizado |
| 3 | Cancela mi pedido pendiente | confirmación → `cancel_order` | Soft-hold liberado; status DELETED |
| 4 | Abre un ticket | `create_support_thread` | Thread propio creado |
| 5 | Quiero comprar … | confirmación → `create_order` | UNVERIFIED + `stockHoldApplied` |

**Fallbacks:** si Appwrite falla, el agente recibe mensaje seguro (no stack).

---

## C. Criterios de cierre formal

| Criterio | Estado |
|----------|--------|
| Deploy estable workers.dev | [ ] verificar `smoke:health` post-deploy |
| `tools/list` 18 tools + policies | ✅ registry smoke |
| Lecturas con identidad | ✅ unit + auth resolver |
| Mutaciones confirmación + ownership | ✅ order service tests |
| Soft-hold create/cancel | ✅ Fase 2 tests |
| Auth JWT planificado + header/jwt modes | ✅ Fase 4 |
| Tests + smoke health CI | ✅ Fase 5 |
| Docs monorepo | ✅ README + CHECKLIST |

Firma smoke agente (B): ________________ fecha _______
