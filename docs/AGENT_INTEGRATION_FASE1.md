# Integración Agente — Fase 1 (cerrada en código)

## Decisiones

| Tema | Valor |
|------|--------|
| Web | `@mistralai/mistralai` |
| Android | HTTP Ktor → `api.mistral.ai` |
| Android secrets | `local.properties` → `BuildConfig` |
| MCP auth | Abierto (cores futuros) |
| Guest | Solo tools catálogo (policy lista) |

## Config local

### Web (`web/.env`)

```env
VITE_MISTRAL_API_KEY=
VITE_MISTRAL_AGENT_ID=
VITE_MISTRAL_MODEL_ID=mistral-medium-latest
```

```bash
cd web && npm install && npm test
```

### Android (`local.properties` en raíz Gradle)

```properties
MISTRAL_API_KEY=
MISTRAL_AGENT_ID=
MISTRAL_MODEL_ID=mistral-medium-latest
```

## Capas

- **Web:** `web/src/core/feature/agent/`
- **Android:** `app/.../feature/agent/` + `agentFeatureModule` en Koin

## Smoke manual data-layer

1. `checkConnection` → status OK  
2. `sendMessage("Hola")` → texto assistant  

Sin UI todavía (Fase 3).
