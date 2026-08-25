# Checklist cierre MCP Cliente

**Última actualización:** 2026-08-25  
**Worker:** `alejotaller-mcp`  
**Fase 0:** ✅ · **Fase 1:** ✅  
**Siguiente:** Fase 2 (OrderService soft-hold)

- [PHASE0.md](./PHASE0.md) · [TOOL_MATRIX.md](./TOOL_MATRIX.md) · [DATA_CONTRACT.md](./DATA_CONTRACT.md) · [IMPLEMENTATION_ROADMAP.md](./IMPLEMENTATION_ROADMAP.md)

---

## 0. Baseline ✓

- [x] Carpeta, worker, secretos, SCOPE B2C, matriz tools, DATA_CONTRACT, health prod

## 1. Transporte ✓

- [x] Health, Streamable HTTP, CI MCP definido

## Fase 1 — Datos net ✓

- [x] `incrementReserved` / `decrementReserved` / `refreshFromRemote`
- [x] Order primitives: `updateVerified`, `updateStockHoldApplied`
- [x] Tests domain + reserved mocks

## 2. Auth — pendiente (Fase 4)

- [ ] JWT prod, deny sin identidad, rate limit, CORS

## 3. Tools

### 3.4 Pedidos (Fase 2 — en curso)
- [ ] `create_order` ≈ RegisterNewSaleCaseUse
- [ ] `cancel_order` ≈ CancelUnverified + ReleaseSoftHold
- [ ] Lecturas ownership (ya en service)

### Resto
- [ ] Smoke sistema / perfil / catálogo / soporte

## 4. Policies

- [x] Matriz en código + docs
- [ ] Audit errores seguros

## 5–7

- [ ] Tests soft-hold service, CI verde, agente, cierre formal

```text
Hecho:  Fase 0 → Fase 1
Siguiente: Fase 2 — OrderService.createOrder / cancelOrder con soft-hold
```
