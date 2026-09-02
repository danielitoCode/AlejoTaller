# Core 5 — Supervisión y reportes (AlejoTaller)

**Estado:** abierto · rama `Core5` · 2026-09-02  
**Canónico:** [dash_alejo_taller/.roadmap/Core5](https://github.com/danielitoCode/dash_alejo_taller/tree/Core5/.roadmap/Core5)

## Rol de este monorepo

Core 5 es **casi exclusivo del panel**. Aquí solo se **protege la frontera** y se documenta qué **no** se implementa.

| Superficie | Core 5 |
|------------|--------|
| **alejotallerscan** | Sin KPIs de panel. Sigue confirm/reject (Core 4). Opcional futuro: lectura local mínima, no agregados globales |
| **web / app cliente** | **Sin** reportes staff ni lectura de `sale_finance_event` de back-office |
| **mcp** | **Sin** tools de reportes financieros staff |
| **docs** | Espejo de checklist / frontera |

## Documentos

| Doc | Rol |
|------|-----|
| [CORE5_UNIFIED_CHECKLIST.md](./CORE5_UNIFIED_CHECKLIST.md) | Ítems **AT** solamente |
| [MVP_CORE5_STATUS.md](./MVP_CORE5_STATUS.md) | Estado vivo |

## Criterio

No mergear cambios que abran write finance o reportes B2C bajo el nombre Core 5. Coordinar cierre con el PR de dash `Core5` → `master`.
