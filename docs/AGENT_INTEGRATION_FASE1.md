# Integración Agente — Fase 1

## Prioridad

1. **Web primero** (SDK `@mistralai/mistralai`) — activa.
2. **Android después** (tú adaptas; código esqueleto puede existir bajo `app/.../feature/agent` pero **no** está registrado en Koin hasta que lo actives).

## Web (activa)

Env `web/.env`:

```env
VITE_MISTRAL_API_KEY=
VITE_MISTRAL_AGENT_ID=
VITE_MISTRAL_MODEL_ID=mistral-medium-latest
```

```bash
cd web && npm install && npm test
```

Feature: `web/src/core/feature/agent/`

## Android (diferido)

- `agentFeatureModule` **no** se carga en `TallerAlejoApp`.
- BuildConfig: `MISTRAL_API_KEY`, `MISTRAL_AGENT_ID`, `MISTRAL_MODEL_ID` listos en `local.properties` cuando retomes.
- CI Android: `cancel-in-progress: false` para no matar jobs por pushes seguidos.

## Guest tools (Fase 2)

Solo catálogo/sistema — ver `agent-tool-policy` en web.
