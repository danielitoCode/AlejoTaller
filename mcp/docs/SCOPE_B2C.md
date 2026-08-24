# Alcance del MCP Cliente — solo B2C

**Carpeta canónica:** `AlejoTaller/mcp/`  
**Worker Cloudflare:** `alejotaller-mcp`  
**Config deploy canónica:** `mcp/wrangler.json`  
**URL (prod):** `https://alejotaller-mcp.daniel-imbert96.workers.dev`

## Qué es

Capa **Model Context Protocol** para que un **agente de IA de atención a clientes**
(p. ej. Mistral Medium) invoque tools sobre el backend Appwrite **en nombre del cliente final**.

## Qué NO es

| Superficie | Motivo |
|------------|--------|
| Back-office / staff (`dash_alejo_taller`) | Roles owner/admin/sales/viewer; otra superficie |
| Operador de piso (`alejotallerscan`) | Confirm/reject VERIFIED, `salida_venta`, finance |
| Escritura de `stock_movements` | Solo staff/operador en flujos Core 2 |
| Escritura de `purchase_entry` / líneas | Factura de entrada = panel admin |
| Escritura de `sale_finance_event` | Solo al VERIFIED por operador/panel |
| Gestión de usuarios staff / roles | Fuera de B2C |
| Reservas de taller staff (`workshop_reservation` admin) | Panel dash; el cliente no opera como admin |

## Permitido (B2C)

- Lectura de **propio** perfil, pedidos, tickets de soporte
- Lectura de catálogo (productos, categorías, promos) con `available = max(0, existence − reserved)`
- Mutaciones de cliente con ownership + (donde aplique) `requiresConfirmation`:
  - actualizar perfil
  - crear / cancelar pedido **propio** en estados permitidos (soft-hold Core 1)
  - crear ticket / mensajes de soporte propios

## Secretos Cloudflare (obligatorios)

```bash
cd mcp
npx wrangler secret put APPWRITE_ENDPOINT
npx wrangler secret put APPWRITE_PROJECT_ID
npx wrangler secret put APPWRITE_API_KEY
npx wrangler secret put APPWRITE_DATABASE_ID
```

Local: copiar `.dev.vars.example` → `.dev.vars` (gitignored).

## Deploy

```bash
cd mcp
npm install
npm run deploy   # usa mcp/wrangler.json → Worker name alejotaller-mcp
```

El `wrangler.json` de la **raíz** del monorepo es un **proxy** con el **mismo** `name: alejotaller-mcp` y `main: mcp/src/worker.ts` para no diverger. Preferir siempre deploy desde `mcp/`.
