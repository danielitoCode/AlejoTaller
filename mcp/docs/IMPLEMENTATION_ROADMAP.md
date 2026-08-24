# Roadmap de implementación MCP Cliente

**Fase 0:** cerrada 2026-08-24 (contrato + baseline).  
**Siguiente:** Fase 1 — capa datos net (product reserved atómico).

Arquitectura objetivo:

```text
Agente → MCP tool → Auth + Policy → Service (= case use web) → Repo interface → Appwrite net
```

No importar `web/` completo (Dexie/Vite). Portar reglas y net repos.

---

## Fase 0 — Contrato y baseline ✅

- [x] Carpeta `mcp/`, worker `alejotaller-mcp`, secretos Appwrite
- [x] Alcance B2C (`SCOPE_B2C.md`)
- [x] Matriz tools + policies (`TOOL_MATRIX.md`, `tool-policy.ts`, `TOOLS_SPEC.md`)
- [x] Contrato de collections/campos (`DATA_CONTRACT.md`)
- [x] Health live: `status=ok`, `appwriteConfigured=true`
- [x] Enlace monorepo (README raíz / AGENTS.md)

---

## Fase 1 — Capa datos net (product + order primitives)

- [ ] `ProductRepository`: `refreshFromRemote`, `incrementReserved`, `decrementReserved`
- [ ] Implementación Appwrite con `incrementDocumentAttribute` / `decrementDocumentAttribute`
- [ ] `OrderRepository`: create, listByUser, getById, updateVerified, updateStockHoldApplied
- [ ] Mappers de campos (`buy_state`, `user_id`, `products` JSON, `stock_hold_applied`, …)
- [ ] Tests unitarios de mappers / reserved clamp

**DoD:** se puede incrementar/decrementar `reserved` desde código MCP sin read-modify-write inseguro.

---

## Fase 2 — Domain services (create / cancel = web)

- [ ] `OrderService.createOrder` ≈ `RegisterNewSaleCaseUse` (hold + compensación + flag)
- [ ] `OrderService.cancelOrder` ≈ `CancelUnverifiedSale` + `ReleaseSoftHold`
- [ ] Validación stock con `availableStock` antes del hold
- [ ] Ownership en todas las lecturas/escrituras de pedidos

**DoD:** create/cancel no dejan `reserved` inconsistente en staging.

---

## Fase 3 — Tools adaptadores

- [ ] Verificar Zod + policy en cada tool de la matriz
- [ ] Identidad solo desde auth context
- [ ] Errores seguros al agente

---

## Fase 4 — Seguridad borde

- [ ] JWT Appwrite en prod
- [ ] Deny sin identidad en tools `requiresAuth`
- [ ] Rate limit + CORS allowlist
- [ ] Scopes API key documentados

---

## Fase 5 — Tests y CI

- [ ] Tests soft-hold (espejo web)
- [ ] `package-lock` alineado workers-types v5
- [ ] CI MCP verde en master

---

## Fase 6 — Agente

- [ ] Host remoto + system prompt
- [ ] Smoke conversacional (catálogo → pedido → cancel → soporte)

---

## Fase 7 — Cierre formal

- [ ] README final + checklist con fecha **MCP cliente cerrado**
- [ ] Nota: no es Core 3; es superficie cliente vía agente
