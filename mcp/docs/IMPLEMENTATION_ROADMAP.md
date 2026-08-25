# Roadmap de implementación MCP Cliente

**Fases 0–3:** ✅  
**Fase 4:** ✅ 2026-08-25 — JWT / rate-limit / CORS  
**Siguiente:** Fase 5–6 (CI smoke + agente) → cierre formal

---

## Fase 4 — Seguridad borde ✅

- [x] `verifyAppwriteJwt` vía `Account.get()` + `Client.setJWT`
- [x] Auth modes: `header` | `jwt` | `jwt_or_header` (`MCP_AUTH_MODE`)
- [x] CORS allowlist (`MCP_CORS_ORIGINS`, reject browser origin no listado)
- [x] Rate limit por IP (`MCP_RATE_LIMIT_RPM`, default 60/min)
- [x] AuthResolver async (JWT network call)
- [x] Tests: cors, rate-limit, resolver header paths

**Env (CF secrets / vars):**

| Variable | Uso |
|----------|-----|
| `MCP_AUTH_MODE` | `jwt` recomendado prod; `jwt_or_header` migración |
| `MCP_CORS_ORIGINS` | `https://host-agente,...` (evitar `*` con credenciales) |
| `MCP_RATE_LIMIT_RPM` | default 60 |

---

## Fase 5–7

- [ ] CI estable + smoke agente
- [ ] Marca **MCP cliente cerrado**
