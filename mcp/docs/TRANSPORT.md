# Transporte MCP — Cloudflare Worker

**Worker:** `alejotaller-mcp`  
**Base URL:** `https://alejotaller-mcp.daniel-imbert96.workers.dev`

## Endpoints HTTP

| Método | Ruta | Uso |
|--------|------|-----|
| `GET` | `/` o `/health` | Health JSON (sin llamar Appwrite; verifica bindings de secretos) |
| `OPTIONS` | `*` | CORS preflight |
| `POST` | `/` (y rutas que maneje el transport) | **Streamable HTTP** MCP (`tools/list`, `tools/call`, …) |

## Smoke health

```bash
curl -sS https://alejotaller-mcp.daniel-imbert96.workers.dev/health | jq .
```

Esperado:

```json
{
  "status": "ok",
  "worker": "alejotaller-mcp",
  "appwriteConfigured": true,
  "transport": "streamable-http"
}
```

Si `status` es `degraded` o `appwriteConfigured: false`, faltan secretos CF.

## Cliente MCP (agente)

Configurar el host del agente con URL remota Streamable HTTP apuntando a la base del Worker.

Headers de identidad (Fase 1):

- `X-Customer-Id` (obligatorio para tools de usuario)
- opcionales: `X-Customer-Name`, `X-Customer-Email`

## Observability

`wrangler.json` tiene `observability.enabled: true`. Revisar logs en dashboard Cloudflare → Workers → alejotaller-mcp.

## Deploy

```bash
cd mcp && npm run deploy
```

Tras deploy, repetir `curl .../health`.
