# AlejoTaller Customer MCP Server

Servidor **Model Context Protocol (MCP)** para **clientes B2C** de AlejoTaller  
(TypeScript + `@modelcontextprotocol/sdk` + Appwrite Server SDK).

| Ítem | Valor |
|------|--------|
| **Estado** | **Cerrado** (2026-08-25) — tools activas y verificadas |
| **Carpeta canónica** | `AlejoTaller/mcp/` |
| **Worker name** | `alejotaller-mcp` |
| **Config deploy** | `mcp/wrangler.json` |
| **URL prod** | `https://alejotaller-mcp.daniel-imbert96.workers.dev` |
| **Health** | `GET /health` |
| **Alcance** | Solo B2C — [docs/SCOPE_B2C.md](docs/SCOPE_B2C.md) |
| **Smoke** | [docs/SMOKE.md](docs/SMOKE.md) |
| **Cierre** | [docs/CHECKLIST_CLOSE.md](docs/CHECKLIST_CLOSE.md) |

**No** es back-office, **no** es operador, **no** escribe `stock_movements` / finance.

Dominio de referencia: **`web/src/core/feature/**`**.

---

## Arquitectura

```text
Agente IA → MCP (Workers Streamable HTTP)
  → Auth (JWT | header) + policy + rate-limit + CORS
    → Services (= case uses web)
      → Repositories → Appwrite net (soft-hold atómico)
```

---

## Tools (18)

[TOOL_MATRIX.md](docs/TOOL_MATRIX.md) · [DATA_CONTRACT.md](docs/DATA_CONTRACT.md)

| Grupo | Tools |
|-------|--------|
| Sistema | `ping_customer_mcp`, `get_server_info` |
| Catálogo | `list_products`, `get_product`, `list_categories`, `get_category`, `list_active_promotions` |
| Perfil | `get_my_profile`, `update_my_profile` |
| Pedidos | `get_my_orders`, `get_order`, `create_order`*, `cancel_order`* |
| Soporte | threads + messages |

\*`requiresConfirmation` + auth.

---

## Auth (Fase 4)

| Modo (`MCP_AUTH_MODE`) | Uso |
|------------------------|-----|
| `jwt` | `Authorization: Bearer <appwrite-jwt>` (recomendado prod) |
| `jwt_or_header` | JWT o `X-Customer-Id` (default migración) |
| `header` | Solo `X-Customer-Id` (host confiable) |

También: `MCP_CORS_ORIGINS`, `MCP_RATE_LIMIT_RPM`.

---

## Local / CI

```bash
cd mcp
npm install
cp .dev.vars.example .dev.vars   # no commit
npm run typecheck
npm test
npm run smoke:health
```

CI: `.github/workflows/ci-mcp.yml` → typecheck + tests + smoke health.

---

## Deploy

```bash
cd mcp && npm run deploy
npm run smoke:health
```

---

## Roadmap

Fases **0–7** cerradas. Superficie cliente B2C vía agente lista en prod.

Detalle: [IMPLEMENTATION_ROADMAP.md](docs/IMPLEMENTATION_ROADMAP.md)
