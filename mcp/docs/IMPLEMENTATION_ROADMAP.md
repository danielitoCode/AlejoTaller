# Roadmap de implementación MCP Cliente

**Fase 0–2:** ✅  
**Fase 3:** ✅ 2026-08-25 — tools audit (result/policy/Zod)  
**Siguiente:** Fase 4 (JWT / rate limit / CORS) o smoke staging

---

## Fase 3 — Tools adaptadores ✅

- [x] `result.ts`: `okJson` / `mapToolError` / `sanitizeErrorMessage`
- [x] `run-tool.ts`: `runTool` / `runAuthedTool` + `getToolPolicy.requiresAuth`
- [x] Descriptions con `confirmationHint` en `create_order` / `cancel_order`
- [x] `get_server_info` expone writeTools + confirmationRequiredTools desde policy
- [x] Todos los tools usan el wrapper (sin try/catch ad-hoc que filtre stacks)
- [x] Tests unit de mapToolError / sanitize

**DoD:** el agente recibe errores de dominio legibles; no stacks ni fugas Appwrite.

---

## Fase 4 — Seguridad borde

- [ ] JWT Appwrite, rate limit, CORS allowlist

## Fase 5–7

- [ ] CI estable, smoke agente, cierre formal
