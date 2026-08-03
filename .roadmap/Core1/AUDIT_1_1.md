# Auditoría 1.1 — Diagnóstico MVP (AlejoTaller)

**Fecha:** 2026-08-01  
**Alcance:** Micro-tareas 1.1.1 – 1.1.4  
**Fuente:** análisis estático del código en `master` + README del monorepo  
**Limitación:** no hay acceso a consola Appwrite, Sentry ni logs de producción en vivo.

> Documento histórico de Fase 1.1. Conservado en Core 1 como referencia de riesgos iniciales.

---

## 1.1.1 Revisar logs de errores de producción / Appwrite / Sentry

### Hallazgos

| Fuente | Estado en el repo |
|--------|-------------------|
| Sentry / crash reporting remoto | **No integrado** de forma visible en el código |
| Appwrite Console logs | No accesibles desde el conector GitHub |
| Logs locales de compilación | Existen archivos vacíos/stub en `.kotlin/errors/` (sin contenido útil de runtime) |
| Instrumentación en código | Hay logs en flujos críticos (sync, realtime, operador) pero sin agregación central |

### Conclusión
No es posible listar errores reales de los últimos 7–14 días sin acceso a:
1. Appwrite Console (Activity / Function logs)
2. Render logs de `alejo_publisher` y de la web
3. Un servicio de crash reporting (Sentry, Firebase Crashlytics, etc.)

### Acción recomendada (Core 2)
- Añadir Crashlytics o Sentry en `app` y `alejotallerscan`
- Revisar logs de Render de `alejo_publisher` y de la web desplegada
- Exportar últimos errores de Appwrite Functions / Auth

**Estado micro-tarea:** parcial — documentada la ausencia de observabilidad central; bloqueada la revisión de logs reales.

---

## 1.1.2 Top 5 riesgos / fallos que afectan compras o autenticación

| # | Riesgo | Impacto | Evidencia en código / docs |
|---|--------|---------|----------------------------|
| 1 | **Publisher con API key simple de MVP** | Un atacante con la key puede publicar eventos falsos `sale:confirmed` / `sale:rejected` | README deuda técnica + `function/alejo_publisher` |
| 2 | **Operaciones sensibles desde el cliente hacia Appwrite** | Reglas de permisos débiles pueden permitir writes indebidos | README |
| 3 | **`products` serializado como string en esquema remoto** | Parsing frágil entre plataformas | Mappers Sale |
| 4 | **Reconciliación offline incompleta / conflictos** | Pedidos locales vs remotos pueden diverger | README |
| 5 | **Auth / sesión multi-superficie no homogeneizada** | Google login, roles, sesión web distintos | shared-auth + apps |

> Varios de estos se mitigaron parcialmente en Core 1 (auth visitante, soft-hold, Telegram desacoplado). Publisher key y observabilidad siguen en Core 2.

---

## 1.1.3 Flujo actual documentado

```text
[Cliente Android / Web]
    | 1. Auth (visitante o autenticado)
    | 2. Catálogo offline-first
    | 3. Carrito
    | 4. Checkout autenticado → Sale UNVERIFIED + soft-hold (reserved += qty)
    v
[Appwrite Sale + Product.reserved]
    | 5. Operador escanea / busca
    | 6. Confirma (VERIFIED + SaleType) o rechaza (DELETED)
    |    VERIFIED: existence -= qty, reserved -= qty
    |    DELETED: reserved -= qty
    | 7. Publisher / realtime al cliente
```

---

## 1.1.4 Campos del pedido (Sale)

Cliente, productos, cantidades, monto, estado (UNVERIFIED/VERIFIED/DELETED), fecha.  
Gaps históricos (hora exacta, precio unitario por línea) → mejora Core 2 / contabilidad.

---

## Resumen

| Micro-tarea | Estado |
|-------------|--------|
| 1.1.1 Logs producción | Parcial (Core 2 observabilidad) |
| 1.1.2 Top 5 riesgos | Hecho |
| 1.1.3 Flujo | Hecho (actualizado con soft-hold en Core 1) |
| 1.1.4 Campos pedido | Hecho |
