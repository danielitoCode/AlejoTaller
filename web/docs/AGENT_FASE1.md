# Agent Fase 1 — Mistral (Web)

## Seguridad (importante)

**No usamos** el paquete npm `@mistralai/mistralai`.

En mayo 2026, versiones **2.2.2–2.2.4** fueron publicadas en una campaña de supply-chain (Mini Shai-Hulud).  
El cliente web habla con `https://api.mistral.ai/v1` solo con **`fetch`** (igual filosofía que Android).

Si llegaste a instalar el SDK:

```bash
cd web
rm -rf node_modules package-lock.json
npm cache clean --force
npm install
```

Y **rota** desde otro equipo limpio: Mistral API key, tokens npm/GitHub, Appwrite keys, etc.

## Env

```env
VITE_MISTRAL_API_KEY=...
VITE_MISTRAL_AGENT_ID=...
VITE_MISTRAL_MODEL_ID=mistral-medium-latest
```

## Smoke

```bash
cd web
npm run smoke:agent
```

1. `GET /v1/models/{model_id}`  
2. `POST /v1/agents/completions`
