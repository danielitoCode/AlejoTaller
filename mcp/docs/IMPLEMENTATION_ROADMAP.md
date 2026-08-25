# Roadmap de implementación MCP Cliente

| Fase | Estado | Fecha |
|------|--------|-------|
| 0 Contrato | ✅ | 2026-08-24 |
| 1 Product reserved | ✅ | 2026-08-25 |
| 2 Order soft-hold | ✅ | 2026-08-25 |
| 3 Tools audit | ✅ | 2026-08-25 |
| 4 JWT / CORS / RL | ✅ | 2026-08-25 |
| 5 CI + smoke health | ✅ | 2026-08-25 |
| 6 Smoke agente (guía) | ✅ docs | 2026-08-25 |
| 7 Cierre formal | ⏳ firma B | — |

---

## Fase 5 — Tests y CI ✅

- [x] `ci-mcp.yml`: typecheck + unit tests
- [x] Job / step `smoke:health` contra workers.dev
- [x] `test/smoke/tool-registry.smoke.test.ts` (18 tools)
- [x] Script `scripts/smoke-health.mjs`

## Fase 6 — Agente ✅ (guía)

- [x] [SMOKE.md](./SMOKE.md) escenarios conversacionales
- [ ] Ejecución manual + firma en checklist

## Fase 7 — Cierre

- [ ] `smoke:health` post-deploy OK
- [ ] Smoke B firmado
- [ ] Actualizar CHECKLIST → **MCP cliente cerrado**
