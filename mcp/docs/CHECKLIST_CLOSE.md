# Checklist cierre MCP Cliente

**Última actualización:** 2026-08-24

## 0. Baseline ✓ (en curso / hecho en repo)

- [x] Carpeta canónica: `AlejoTaller/mcp/`
- [x] Worker name: `alejotaller-mcp` (`mcp/wrangler.json`)
- [x] Wrangler raíz alineado al **mismo** name + entry `mcp/src/worker.ts` (proxy; deploy canónico desde `mcp/`)
- [x] Secretos CF documentados: `APPWRITE_ENDPOINT`, `APPWRITE_PROJECT_ID`, `APPWRITE_API_KEY`, `APPWRITE_DATABASE_ID`
- [x] `.dev.vars` / `.env` gitignored; plantilla `.dev.vars.example`
- [x] Alcance B2C: [SCOPE_B2C.md](./SCOPE_B2C.md)

## 1–7 (pendiente)

Ver checklist de trabajo en conversación / siguientes commits: transporte, auth, tools, policies, tests, agente, docs finales.
