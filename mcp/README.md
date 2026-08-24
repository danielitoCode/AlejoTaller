# AlejoTaller Customer MCP Server

Servidor **Model Context Protocol (MCP)** para **clientes B2C** de AlejoTaller  
(TypeScript + `@modelcontextprotocol/sdk` + Appwrite Server SDK).

| Ítem | Valor |
|------|--------|
| **Carpeta canónica** | `AlejoTaller/mcp/` |
| **Worker name** | `alejotaller-mcp` |
| **Config deploy** | `mcp/wrangler.json` |
| **URL prod** | `https://alejotaller-mcp.daniel-imbert96.workers.dev` |
| **Health** | `GET /health` |
| **Alcance** | Solo B2C — [docs/SCOPE_B2C.md](docs/SCOPE_B2C.md) |
| **Transporte** | [docs/TRANSPORT.md](docs/TRANSPORT.md) |
| **Fase 0** | ✅ [docs/PHASE0.md](docs/PHASE0.md) |

**No** es back-office, **no** es operador, **no** escribe `stock_movements` / `purchase_*` / `sale_finance_event`.

Dominio de referencia: **`web/src/core/feature/**`** (mismas reglas; MCP = solo capa net + tools).

---

## 1. Arquitectura

```text
Agente IA (p. ej. Mistral Medium)
    → MCP (Streamable HTTP en Workers)
        → Auth context + policies (cliente)
            → Services (= case uses web)
                → Repositories → Appwrite (net)
```

---

## 2. Tools (18)

Matriz completa: [docs/TOOL_MATRIX.md](docs/TOOL_MATRIX.md)  
Schemas: [docs/TOOLS_SPEC.md](docs/TOOLS_SPEC.md)  
Contrato Appwrite: [docs/DATA_CONTRACT.md](docs/DATA_CONTRACT.md)

| Grupo | Tools |
|-------|--------|
| Sistema | `ping_customer_mcp`, `get_server_info` |
| Catálogo | `list_products`, `get_product`, `list_categories`, `get_category`, `list_active_promotions` |
| Perfil | `get_my_profile`, `update_my_profile` |
| Pedidos | `get_my_orders`, `get_order`, `create_order`*, `cancel_order`* |
| Soporte | `get_my_support_threads`, `get_support_thread`, `get_thread_messages`, `create_support_thread`, `post_support_message` |

\*`requiresConfirmation` + auth.

---

## 3. Auth

**Fase 1 (actual):** header `X-Customer-Id` desde el host MCP.  
**Prod (planificado):** JWT Appwrite — ver roadmap Fase 4.

Nunca pasar `userId` como argumento de tool del usuario.

---

## 4. Local

```bash
cd mcp
npm install
cp .dev.vars.example .dev.vars
npm run dev
npm test
```

---

## 5. Cloudflare

```bash
cd mcp
npm run deploy
curl -sS https://alejotaller-mcp.daniel-imbert96.workers.dev/health
```

---

## 6. Roadmap e implementación

| Fase | Estado |
|------|--------|
| 0 Contrato / baseline | ✅ |
| 1 Product reserved atómico | ← siguiente |
| 2 create/cancel = web | pendiente |
| 3–7 Tools hardening, auth, tests, agente, cierre | [IMPLEMENTATION_ROADMAP.md](docs/IMPLEMENTATION_ROADMAP.md) |

Checklist: [docs/CHECKLIST_CLOSE.md](docs/CHECKLIST_CLOSE.md)
