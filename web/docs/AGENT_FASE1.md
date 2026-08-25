# Agent Fase 1 — Mistral (Web)

## Env (`.env` local, no commit)

```env
VITE_MISTRAL_API_KEY=...
VITE_MISTRAL_AGENT_ID=ag_...
VITE_MISTRAL_MODEL_ID=mistral-medium-latest
```

## Install

```bash
cd web
npm install
```

## Uso data-layer

```ts
import { agentContainer } from "./core/feature/agent/di/agent.container";

const conn = await agentContainer.useCases.checkConnection();
const reply = await agentContainer.useCases.sendMessage("Hola");
```

## Guest tools (Fase 2)

Ver `domain/policy/agent-tool-policy.ts` — invitados solo catálogo/sistema.
