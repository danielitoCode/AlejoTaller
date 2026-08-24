# Fase 0 — Cerrada

**Fecha:** 2026-08-24  
**Objetivo:** dejar contrato y baseline listos para implementar Fase 1 (datos net / soft-hold).

---

## Entregables

| Documento | Rol |
|-----------|-----|
| [TOOL_MATRIX.md](./TOOL_MATRIX.md) | 18 tools, auth, confirm, espejo web |
| [DATA_CONTRACT.md](./DATA_CONTRACT.md) | Collections y campos Appwrite = web |
| [IMPLEMENTATION_ROADMAP.md](./IMPLEMENTATION_ROADMAP.md) | Fases 1–7 |
| [SCOPE_B2C.md](./SCOPE_B2C.md) | Límites B2C |
| [TOOLS_SPEC.md](./TOOLS_SPEC.md) | JSON Schema por tool |
| [TRANSPORT.md](./TRANSPORT.md) | HTTP / health |
| [CHECKLIST_CLOSE.md](./CHECKLIST_CLOSE.md) | Seguimiento cierre |

Código de referencia ya existente:

- `src/policies/tool-policy.ts` — matriz runtime
- `src/infrastructure/appwrite/config.ts` — `COLLECTIONS`
- `src/mcp/tools/*` — registro de tools

---

## Health verificado (prod)

```json
{
  "status": "ok",
  "worker": "alejotaller-mcp",
  "scope": "b2c-customer",
  "appwriteConfigured": true
}
```

`GET https://alejotaller-mcp.daniel-imbert96.workers.dev/health`

---

## Criterio “listo para Fase 1”

- [x] Tools y policies documentadas y alineadas al código
- [x] Contrato de datos explícito (product.reserved, sale.buy_state, …)
- [x] Roadmap con DoD de soft-hold atómico
- [x] Health prod con Appwrite configurado
- [x] Enlaces desde README MCP y monorepo

**Siguiente tarea de código:** implementar en `product.appwrite.repository` / interfaz `ProductRepository` los métodos `incrementReserved` y `decrementReserved` como en `web/.../product.net.repository.ts`.
