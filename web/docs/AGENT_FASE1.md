# Agent Fase 1 — Mistral (Web)

SDK oficial: `@mistralai/mistralai`

## Variables de entorno

En `web/.env` o `web/.env.local` (no commit):

```env
VITE_MISTRAL_API_KEY=...
VITE_MISTRAL_AGENT_ID=ag_...
VITE_MISTRAL_MODEL_ID=mistral-medium-latest
```

También acepta sin prefijo Vite (scripts Node):

```env
MISTRAL_API_KEY=...
MISTRAL_AGENT_ID=...
MISTRAL_MODEL_ID=mistral-medium-latest
```

## Install

```bash
cd web
npm install
```

## Smoke (SDK)

```bash
cd web
npm run smoke:agent
```

Pasos del script:

1. `client.models.retrieve({ modelId })` — conexión API + key
2. `client.agents.complete({ agentId, messages })` — agent id válido

Vitest live (opcional):

```bash
# PowerShell
$env:AGENT_SMOKE_LIVE="1"; npm test -- src/test/core/feature/agent/smoke/agent-mistral.live.test.ts
```

## Data layer app

```ts
import { agentContainer } from "./core/feature/agent/di/agent.container";

const conn = await agentContainer.useCases.checkConnection();
const reply = await agentContainer.useCases.sendMessage("Hola");
```

`agent.container` lee `ENV` (`VITE_MISTRAL_*`).
