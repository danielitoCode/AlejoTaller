# AlejoTaller Customer MCP Server

Servidor **Model Context Protocol (MCP)** para **clientes B2C** de AlejoTaller  
(TypeScript + `@modelcontextprotocol/sdk` + Appwrite Server SDK).

| Ítem | Valor |
|------|--------|
| **Carpeta canónica** | `AlejoTaller/mcp/` |
| **Worker name** | `alejotaller-mcp` |
| **Config deploy** | `mcp/wrangler.json` |
| **URL prod** | `https://alejotaller-mcp.daniel-imbert96.workers.dev` |
| **Alcance** | Solo B2C — [docs/SCOPE_B2C.md](docs/SCOPE_B2C.md) |

**No** es back-office, **no** es operador, **no** escribe `stock_movements` / `purchase_*` / `sale_finance_event`.

---

## 1. Arquitectura

```text
Agente IA (p. ej. Mistral Medium)
    → MCP (stdio local | Streamable HTTP en Workers)
        → Auth context + policies (cliente)
            → Services → Repositories → Appwrite
```

1. **Tools** — interfaz para la IA (sin lógica de BD directa).  
2. **Auth & policies** — identidad del cliente; confirmación en mutaciones sensibles.  
3. **Services** — ownership de pedidos/tickets.  
4. **Repositories** — `node-appwrite` con API key de servidor.  
5. **Appwrite** — fuente de verdad.

---

## 2. Tools

Especificación: [docs/TOOLS_SPEC.md](docs/TOOLS_SPEC.md).

### Sistema
- `ping_customer_mcp`, `get_server_info`

### Perfil
- `get_my_profile`, `update_my_profile`

### Pedidos
- `get_my_orders`, `get_order`
- `create_order` / `cancel_order` (`requiresConfirmation: true`; soft-hold)

### Catálogo
- `list_products`, `get_product`, `list_categories`, `get_category`, `list_active_promotions`

### Soporte
- `get_my_support_threads`, `get_support_thread`, `get_thread_messages`
- `create_support_thread`, `post_support_message`

---

## 3. Local

```bash
cd mcp
npm install
cp .dev.vars.example .dev.vars   # rellenar; no commitear
npm run dev                      # stdio
# npm run dev:http
npm test
```

Inspector:

```bash
npx @modelcontextprotocol/inspector tsx src/index.ts
```

### Variables (local = mismos nombres que secretos CF)

| Variable | Uso |
|----------|-----|
| `APPWRITE_ENDPOINT` | URL API Appwrite |
| `APPWRITE_PROJECT_ID` | Project ID |
| `APPWRITE_API_KEY` | API key servidor (scopes mínimos) |
| `APPWRITE_DATABASE_ID` | Database ID |
| `MCP_AUTH_SECRET` | Opcional Fase 1 |
| `ENVIRONMENT` | `development` / `production` |

---

## 4. Cloudflare Workers

**Deploy canónico (preferido):**

```bash
cd mcp
npx wrangler secret put APPWRITE_ENDPOINT
npx wrangler secret put APPWRITE_PROJECT_ID
npx wrangler secret put APPWRITE_API_KEY
npx wrangler secret put APPWRITE_DATABASE_ID
npm run deploy
```

El `wrangler.json` de la **raíz del monorepo** usa el **mismo** `name: alejotaller-mcp` y apunta a `mcp/src/worker.ts` para evitar un segundo Worker. No desplegar dos configs con nombres distintos.

---

## 5. Auth

**Fase 1:** identidad por `X-Customer-Id` (y opcionales name/email) desde el **MCP host**.  
La IA **no** pasa `customerId` como argumento de tools de usuario.

**Producción (siguiente):** JWT Appwrite validado en `src/auth/resolver.ts`.

---

## 6. Checklist de cierre

[docs/CHECKLIST_CLOSE.md](docs/CHECKLIST_CLOSE.md)
