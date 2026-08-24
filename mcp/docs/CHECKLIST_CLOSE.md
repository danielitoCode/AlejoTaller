# Checklist cierre MCP Cliente

**Última actualización:** 2026-08-24

## 0. Baseline ✓

- [x] Carpeta canónica `mcp/` + Worker `alejotaller-mcp`
- [x] Wrangler unificado (mismo name)
- [x] Secretos CF: `APPWRITE_ENDPOINT`, `APPWRITE_PROJECT_ID`, `APPWRITE_API_KEY`, `APPWRITE_DATABASE_ID` (confirmados en cuenta)
- [x] `.dev.vars` gitignored + example
- [x] Alcance B2C: [SCOPE_B2C.md](./SCOPE_B2C.md)

## 1. Transporte y despliegue

- [x] `GET /health` y `GET /` — JSON health sin round-trip Appwrite
- [x] CORS preflight + headers MCP (`Mcp-Session-Id`, customer headers)
- [x] Streamable HTTP en Worker (`WebStandardStreamableHTTPServerTransport`)
- [x] Docs: [TRANSPORT.md](./TRANSPORT.md)
- [ ] **Check tuyo post-deploy:** `curl .../health` → `status: ok`, `appwriteConfigured: true`
- [ ] **Check tuyo:** MCP Inspector / agente → `tools/list` contra la URL del Worker
- [x] Observability CF habilitada en `wrangler.json`

## 2–7 (pendiente)

Auth JWT, verificación tool-by-tool, policies, tests, smoke agente, docs finales.
