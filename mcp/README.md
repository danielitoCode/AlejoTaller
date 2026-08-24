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

**No** es back-office, **no** es operador, **no** escribe `stock_movements` / `purchase_*` / `sale_finance_event`.

---

## 1. Arquitectura

```text
Agente IA (p. ej. Mistral Medium)
    → MCP (stdio local | Streamable HTTP en Workers)
        → Auth context + policies (cliente)
            → Services → Repositories → Appwrite
```

---

## 2. Tools

Especificación: [docs/TOOLS_SPEC.md](docs/TOOLS_SPEC.md).

Sistema · Perfil · Pedidos · Catálogo · Soporte (ver README histórico / TOOLS_SPEC).

---

## 3. Local

```bash
cd mcp
npm install
cp .dev.vars.example .dev.vars
npm run dev
npm test
```

---

## 4. Cloudflare

```bash
cd mcp
# secretos (ya en cuenta si listaste los 4 APPWRITE_*)
npm run deploy
curl -sS https://alejotaller-mcp.daniel-imbert96.workers.dev/health
```

---

## 5. Auth

**Fase 1:** `X-Customer-Id` desde el MCP host.  
**Prod:** JWT Appwrite en `src/auth/resolver.ts` (pendiente checklist §2).

---

## 6. Checklist

[docs/CHECKLIST_CLOSE.md](docs/CHECKLIST_CLOSE.md)
