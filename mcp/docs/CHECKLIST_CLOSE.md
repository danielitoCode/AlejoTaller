# Checklist cierre MCP Cliente

**Última actualización:** 2026-08-24  
**Worker:** `alejotaller-mcp` · **URL:** `https://alejotaller-mcp.daniel-imbert96.workers.dev`  
**Fase 0:** ✅ cerrada — ver [PHASE0.md](./PHASE0.md)

Documentos de contrato:

- [TOOL_MATRIX.md](./TOOL_MATRIX.md) — 18 tools
- [DATA_CONTRACT.md](./DATA_CONTRACT.md) — Appwrite = web
- [IMPLEMENTATION_ROADMAP.md](./IMPLEMENTATION_ROADMAP.md) — Fases 1–7

---

## 0. Baseline ✓

- [x] Carpeta canónica: `AlejoTaller/mcp/`
- [x] Worker name: `alejotaller-mcp` (`mcp/wrangler.json`)
- [x] Wrangler raíz alineado; deploy canónico desde `mcp/`
- [x] Secretos CF: `APPWRITE_ENDPOINT`, `APPWRITE_PROJECT_ID`, `APPWRITE_API_KEY`, `APPWRITE_DATABASE_ID`
- [x] `.dev.vars` gitignored; plantilla `.dev.vars.example`
- [x] Alcance solo B2C: [SCOPE_B2C.md](./SCOPE_B2C.md)
- [x] Matriz tools + policies documentada: [TOOL_MATRIX.md](./TOOL_MATRIX.md)
- [x] Contrato collections/campos: [DATA_CONTRACT.md](./DATA_CONTRACT.md)
- [x] Health prod: `status=ok`, `appwriteConfigured=true` (2026-08-24)

---

## 1. Transporte y despliegue ✓ (código + CI)

- [x] `GET /` y `GET /health`
- [x] CORS preflight + headers MCP
- [x] Streamable HTTP en Worker
- [x] [TRANSPORT.md](./TRANSPORT.md)
- [x] Observability CF
- [x] CI MCP (typecheck + vitest) + gate monorepo
- [x] Health live verificado
- [ ] Smoke manual `tools/list` en Inspector / host (opcional, no bloquea Fase 1)

---

## 2. Auth e identidad — pendiente (Fase 4)

- [ ] Identidad solo por contexto/host; nunca arg de tool
- [ ] Rechazar sin identidad en tools `requiresAuth`
- [ ] JWT Appwrite en prod
- [ ] Rate limit básico
- [ ] API key scopes mínimos documentados

---

## 3. Tools — implementación por fase

### 3.1 Sistema
- [ ] Smoke `ping_customer_mcp` / `get_server_info`

### 3.2 Perfil
- [ ] `get_my_profile` / `update_my_profile` + ownership

### 3.3 Catálogo
- [ ] Lecturas con `available = max(0, existence − reserved)`
- [ ] Categories / promos

### 3.4 Pedidos (Fase 1–2 — prioritario)
- [ ] Product net: `incrementReserved` / `decrementReserved`
- [ ] `create_order` ≈ RegisterNewSaleCaseUse
- [ ] `cancel_order` ≈ CancelUnverifiedSale + ReleaseSoftHold
- [ ] `get_my_orders` / `get_order` + ownership

### 3.5 Soporte
- [ ] Threads / mensajes + ownership (alinear case uses web)

---

## 4. Policies

- [x] Matriz en `tool-policy.ts` + [TOOL_MATRIX.md](./TOOL_MATRIX.md)
- [ ] Errores seguros auditados en todos los handlers
- [ ] Sin tools staff (revisión final)

---

## 5. Tests / CI

- [x] Scripts typecheck + test
- [x] Workflow CI MCP
- [ ] Soft-hold unit tests
- [ ] CI verde estable (lockfile workers-types)

---

## 6. Integración agente — pendiente

- [ ] URL remota en host + system prompt
- [ ] Smoke conversacional

---

## 7. Documentación

- [x] Fase 0 docs (PHASE0, MATRIX, DATA_CONTRACT, ROADMAP)
- [x] README MCP + SCOPE + TRANSPORT
- [x] Enlace monorepo README / AGENTS.md
- [ ] Marca formal **MCP cliente cerrado** + fecha

---

## Resumen

| Bloque | Estado |
|--------|--------|
| 0 Baseline + contrato | ✅ |
| 1 Transporte + health | ✅ |
| 2 Auth | Pendiente |
| 3 Tools (soft-hold) | **Siguiente: Fase 1** |
| 4 Policies matriz | ✅ código; audit pendiente |
| 5 Tests soft-hold | Pendiente |
| 6 Agente | Pendiente |
| 7 Cierre formal | Parcial |

```text
Hecho:  Fase 0
Siguiente: Fase 1 — product.appwrite.repository increment/decrement reserved
```
