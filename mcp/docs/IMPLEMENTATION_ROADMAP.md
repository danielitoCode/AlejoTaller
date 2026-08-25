# Roadmap de implementación MCP Cliente

**Fase 0:** cerrada 2026-08-24  
**Fase 1:** cerrada 2026-08-25 — product reserved atómico + primitives order  
**Siguiente:** Fase 2 — OrderService create/cancel = web (RegisterNewSale / CancelUnverified)

Arquitectura:

```text
Agente → MCP tool → Auth + Policy → Service (= case use web) → Repo interface → Appwrite net
```

---

## Fase 0 — Contrato y baseline ✅

- [x] Matriz tools, DATA_CONTRACT, health, enlaces monorepo

---

## Fase 1 — Capa datos net ✅

- [x] `IProductRepository`: `refreshFromRemote`, `incrementReserved`, `decrementReserved`
- [x] `AppwriteProductRepository`: `incrementDocumentAttribute` / `decrementDocumentAttribute` (positional + fallback object)
- [x] Paginación `listAll`, mapper `existence`/`reserved`/`photo_url`
- [x] `IOrderRepository`: `updateVerified`, `updateStockHoldApplied` (+ create/list/get/cancel)
- [x] Guard: MCP no puede setear `VERIFIED`
- [x] Tests: `availableStock`, reserved atomic mocks, order ownership mock actualizado

**DoD:** código MCP puede mutar `reserved` atómicamente sin RMW inseguro.

---

## Fase 2 — Domain services (create / cancel = web)

- [ ] `OrderService.createOrder` ≈ `RegisterNewSaleCaseUse`:
  - validar stock (`availableStock`)
  - `create` sale UNVERIFIED
  - soft-hold por línea (`refreshFromRemote` + `incrementReserved`)
  - compensación si falla línea posterior
  - `updateStockHoldApplied(true)`
- [ ] `OrderService.cancelOrder` ≈ `CancelUnverifiedSale`:
  - ownership + solo UNVERIFIED
  - `updateVerified(DELETED)`
  - `decrementReserved` por línea
- [ ] Tests unit soft-hold (apply + compensate + stock insuficiente)

**DoD:** create/cancel no dejan `reserved` inconsistente.

---

## Fase 3 — Tools adaptadores

- [ ] Zod + policy + errores seguros auditados

## Fase 4 — Seguridad borde

- [ ] JWT, rate limit, CORS allowlist

## Fase 5 — Tests y CI

- [ ] CI verde estable (lockfile)

## Fase 6 — Agente

- [ ] Smoke conversacional

## Fase 7 — Cierre formal

- [ ] Marca **MCP cliente cerrado**
