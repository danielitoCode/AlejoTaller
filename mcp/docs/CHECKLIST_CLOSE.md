# Checklist cierre MCP Cliente

**Última actualización:** 2026-08-24  
**Worker:** `alejotaller-mcp` · **URL:** `https://alejotaller-mcp.daniel-imbert96.workers.dev`  
**Rama / merge docs:** progreso §0–§1 + CI

---

## 0. Baseline ✓

- [x] Carpeta canónica: `AlejoTaller/mcp/`
- [x] Worker name: `alejotaller-mcp` (`mcp/wrangler.json`)
- [x] Wrangler raíz alineado al mismo name (proxy → `mcp/src/worker.ts`); deploy canónico desde `mcp/`
- [x] Secretos CF confirmados en cuenta:
  - `APPWRITE_ENDPOINT`
  - `APPWRITE_PROJECT_ID`
  - `APPWRITE_API_KEY`
  - `APPWRITE_DATABASE_ID`
- [x] `.dev.vars` / `.env` gitignored; plantilla `.dev.vars.example`
- [x] Alcance solo B2C: [SCOPE_B2C.md](./SCOPE_B2C.md) (no staff, no operador, no movements/purchase/finance)

---

## 1. Transporte y despliegue ✓ (código + CI)

- [x] `GET /` y `GET /health` — JSON health sin round-trip Appwrite
- [x] CORS preflight + headers MCP (`Mcp-Session-Id`, `X-Customer-*`)
- [x] Streamable HTTP en Worker (`WebStandardStreamableHTTPServerTransport`)
- [x] Docs: [TRANSPORT.md](./TRANSPORT.md)
- [x] Observability CF en `wrangler.json`
- [x] **CI MCP** (`.github/workflows/ci-mcp.yml`): typecheck + vitest en `mcp/`
- [x] **CI Gate** incluye las 4 superficies: Web · Android Cliente · Android Operador · **MCP**
- [ ] **Check manual (opcional si deploy lag):** `curl …/health` → `status: ok`, `appwriteConfigured: true`
- [ ] **Check manual:** MCP Inspector / agente → `tools/list` en la URL del Worker

---

## 2. Auth e identidad — pendiente

- [ ] Identidad solo por contexto/host (`X-Customer-Id`); nunca como arg de tool de usuario (revisar código + tests)
- [ ] Rechazar requests sin identidad en tools de datos del cliente
- [ ] JWT Appwrite en `auth/resolver.ts` (prod)
- [ ] Rate limit básico (customerId / IP)
- [ ] API key Appwrite con scopes mínimos documentados

---

## 3. Tools — habilitar y verificar

### 3.1 Sistema
- [ ] Smoke `ping_customer_mcp`
- [ ] Smoke `get_server_info`

### 3.2 Perfil
- [ ] `get_my_profile` / `update_my_profile` + ownership

### 3.3 Catálogo
- [ ] `list_products` / `get_product` con `available = max(0, existence − reserved)`
- [ ] `list_categories` / `get_category` / `list_active_promotions`

### 3.4 Pedidos (Core 1 soft-hold)
- [ ] `get_my_orders` / `get_order`
- [ ] `create_order` / `cancel_order` + `requiresConfirmation` + reserved

### 3.5 Soporte
- [ ] Threads / mensajes / create / post + ownership

---

## 4. Policies — pendiente

- [ ] Matriz tool → auth → confirmation → mutación
- [ ] Errores seguros; sin tools staff

---

## 5. Tests — parcial (CI verde pendiente de run en Actions)

- [x] Scripts `typecheck` + `test` en `package.json`
- [x] Workflow CI MCP en gate
- [ ] Unit ownership / stock en create_order (ampliar si faltan)
- [ ] CI MCP verde en `master` tras último push a `mcp/**`

---

## 6. Integración agente — pendiente

- [ ] Config URL remota en host (Mistral Medium u otro)
- [ ] System prompt + smoke conversacional

---

## 7. Documentación final — pendiente

- [x] README MCP + SCOPE + TRANSPORT + este checklist (parcial)
- [ ] Enlace desde README raíz monorepo / AGENTS.md
- [ ] Marca formal **MCP cliente cerrado** + fecha

---

## Resumen de avance

| Bloque | Estado |
|--------|--------|
| 0 Baseline | ✓ |
| 1 Transporte + CI MCP + Gate | ✓ código; smokes manuales opcionales |
| 2 Auth | Pendiente |
| 3 Tools smoke | Pendiente |
| 4 Policies | Pendiente |
| 5 Tests / CI verde | CI definido; validar run |
| 6 Agente | Pendiente |
| 7 Docs cierre | Parcial |

```text
Hecho:  0 → 1 (+ CI)
Siguiente: 2 Auth  o  3.1–3.3 lecturas + smoke tools/list
```
