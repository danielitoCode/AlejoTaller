# Checklist cierre MCP Cliente

**Última actualización:** 2026-08-25  
**Worker:** `alejotaller-mcp` → `https://alejotaller-mcp.daniel-imbert96.workers.dev`  
**Fases 0–6 código:** ✅  
**Cierre formal:** pendiente firma smoke agente (sección B de [SMOKE.md](./SMOKE.md))

Docs: [PHASE0](./PHASE0.md) · [TOOL_MATRIX](./TOOL_MATRIX.md) · [DATA_CONTRACT](./DATA_CONTRACT.md) · [IMPLEMENTATION_ROADMAP](./IMPLEMENTATION_ROADMAP.md) · [SMOKE](./SMOKE.md)

---

## Fases

| Fase | Contenido | Estado |
|------|-----------|--------|
| 0 | Contrato / baseline | ✅ |
| 1 | Product reserved atómico | ✅ |
| 2 | OrderService soft-hold | ✅ |
| 3 | Tools result/policy/Zod | ✅ |
| 4 | JWT / rate-limit / CORS | ✅ |
| 5 | CI + registry smoke + health script | ✅ |
| 6 | Guía smoke agente + checklist formal | ✅ código; ⏳ firma manual |
| 7 | Marca **MCP cliente cerrado** | tras smoke B + deploy verificado |

---

## Criterio “MCP cerrado”

| Criterio | OK |
|----------|-----|
| Deploy estable en workers.dev | [ ] `npm run smoke:health` |
| `tools/list` / policies 18 tools | [x] registry smoke |
| Lecturas con identidad correcta | [x] |
| Mutaciones con confirmación + ownership | [x] |
| Soft-hold respetado create/cancel | [x] |
| Auth Fase 1 header + JWT modes | [x] |
| Tests + smoke health CI | [x] |
| Docs enlazadas monorepo | [x] |
| Smoke conversacional agente (B) | [ ] |

---

## Nota de alcance

**No** es Core 3. Es habilitación de superficie **cliente B2C** vía agente.  
Staff / operador / finance / movements fuera de alcance.
