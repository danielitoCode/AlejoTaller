# Roadmap de implementación MCP Cliente

**Fase 0:** ✅ 2026-08-24  
**Fase 1:** ✅ 2026-08-25 — product reserved atómico  
**Fase 2:** ✅ 2026-08-25 — OrderService create/cancel + soft-hold  
**Siguiente:** Fase 3–4 (tools audit / auth JWT) o smoke staging

```text
Agente → MCP tool → Auth + Policy → Service (= case use web) → Repo → Appwrite net
```

---

## Fase 0 — Contrato ✅
## Fase 1 — Datos net ✅

- `incrementReserved` / `decrementReserved` / `refreshFromRemote`
- Order primitives `updateVerified` / `updateStockHoldApplied`

---

## Fase 2 — Domain services ✅

- [x] `OrderService.createOrder` ≈ RegisterNewSaleCaseUse
  - validar stock (`availableStock`)
  - create sale con líneas enriquecidas + amount
  - soft-hold por línea + compensación
  - `stock_hold_applied=true`
- [x] `OrderService.cancelOrder` ≈ CancelUnverified + ReleaseSoftHold
- [x] DI: `new OrderService(orderRepo, productRepo)` en `index.ts` / `worker.ts`
- [x] Tests: stock insuficiente, hold OK, compensación multi-línea, cancel libera reserved

**DoD:** create/cancel coordinan reserved atómico; ownership B2C intacto.

---

## Fase 3 — Tools adaptadores

- [ ] Audit Zod + policy + mensajes de error seguros

## Fase 4 — Seguridad borde

- [ ] JWT Appwrite, rate limit, CORS allowlist

## Fase 5 — CI

- [ ] Lockfile estable + CI verde

## Fase 6 — Agente

- [ ] Smoke conversacional staging

## Fase 7 — Cierre formal

- [ ] Marca **MCP cliente cerrado**
