# Matriz de tools MCP Cliente — Fase 0

**Fecha:** 2026-08-24  
**Worker:** `alejotaller-mcp`  
**URL:** `https://alejotaller-mcp.daniel-imbert96.workers.dev`  
**Registro de políticas:** `mcp/src/policies/tool-policy.ts`  
**Schemas:** `mcp/docs/TOOLS_SPEC.md`

Principio: tool delgada → service (case use web) → repo net Appwrite.

| # | Tool | Auth | Confirm | Perm | Service / repo MCP | Case use web de referencia |
|---|------|------|---------|------|--------------------|----------------------------|
| 1 | `ping_customer_mcp` | no | no | READ | system | — |
| 2 | `get_server_info` | no | no | READ | system | — |
| 3 | `list_products` | no | no | READ | ProductService | GetAllProduct + availableStock |
| 4 | `get_product` | no | no | READ | ProductService | GetProductById |
| 5 | `list_categories` | no | no | READ | CategoryService | GetAllCategories |
| 6 | `get_category` | no | no | READ | CategoryService | GetCategoryById |
| 7 | `list_active_promotions` | no | no | READ | PromotionService | GetActivePromos + PromotionPolicy |
| 8 | `get_my_profile` | sí | no | READ | CustomerService | GetCurrentUser |
| 9 | `update_my_profile` | sí | no | WRITE | CustomerService | UpdateName / Phone / Photo |
| 10 | `get_my_orders` | sí | no | READ | OrderService | GetSales (por user_id) |
| 11 | `get_order` | sí | no | READ | OrderService | get + ownership |
| 12 | `create_order` | sí | **sí** | WRITE | OrderService | **RegisterNewSaleCaseUse** |
| 13 | `cancel_order` | sí | **sí** | WRITE | OrderService | **CancelUnverifiedSaleCaseUse** |
| 14 | `get_my_support_threads` | sí | no | READ | SupportService | ListMySupportThreads |
| 15 | `get_support_thread` | sí | no | READ | SupportService | get + ownership |
| 16 | `get_thread_messages` | sí | no | READ | SupportService | ListSupportMessages |
| 17 | `create_support_thread` | sí | no | WRITE | SupportService | CreateSupportThreadCaseUse |
| 18 | `post_support_message` | sí | no | WRITE | SupportService | PostSupportMessageCaseUse |

**Total: 18 tools** (registradas en `tool-policy.ts` y en los `register*Tools`).

---

## Gaps conocidos (post Fase 0 → trabajo Fase 1+)

| Área | Estado |
|------|--------|
| Soft-hold atómico `incrementReserved` / `decrementReserved` | Pendiente Fase 1 |
| `create_order` / `cancel_order` alineados a web | Pendiente Fase 2 |
| JWT Appwrite prod | Pendiente Fase 4 |
| CORS allowlist / rate limit | Pendiente Fase 4 |
| Tests soft-hold + CI lockfile estable | Pendiente Fase 5 |

---

## Smoke mínimo (manual)

```bash
curl -sS https://alejotaller-mcp.daniel-imbert96.workers.dev/health
# Esperado: status=ok, appwriteConfigured=true, scope=b2c-customer
```

Luego MCP Inspector / host: `tools/list` debe listar las 18 tools de la matriz.
