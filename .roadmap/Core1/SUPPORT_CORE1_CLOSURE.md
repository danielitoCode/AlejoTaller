# Support — Cierre Core 1 (cliente web)

**Política:** `.policies/support/SUPPORT_POLICY.md`  
**Transporte:** Appwrite Database + Realtime (sin Pulse en camino feliz)  
**Última actualización:** 2026-08-16

## Alcance de este cierre

| Superficie | Estado |
|------------|--------|
| **Web cliente** | Cerrado en desarrollo e integrado (este documento) |
| **Android cliente** | **Sprint paralelo** — no bloquea cierre web Core 1 |
| **Operador / dash** | Gobierno fuera de este monorepo (`dash_alejo_taller`) |

---

## S1 compartido (infra)

- [x] Colecciones `support_threads` / `support_messages` en Appwrite (consola)
- [x] Repo Appwrite en web (`support.appwrite.repository.ts`) — camino feliz sin Pulse
- [x] Modelo dominio alineado a policy (`SupportMessage`, threads + messages)
- [x] Case-uses: create thread, list mine, list messages, post reply, mark read, subscribe inbox
- [x] Tests unitarios web: `PostSupportMessageCaseUse`, `MarkThreadReadCaseUse` (contadores unread)

## S3 Cliente web

- [x] Rutas nested: `support` + `support-detail` (`nested.router.ts`, deeplink)
- [x] Montaje en `NestedNavigationWrapper` (inbox + detail)
- [x] Nav “Soporte” con badge de unread usuario
- [x] Inbox: lista ordenada, estados, crear consulta (motivo / asunto / cuerpo)
- [x] Guest → CTA login (no crea threads)
- [x] Detail: historial, envío reply, mark-as-read al abrir
- [x] Realtime Appwrite: mensajes entrantes y actualización de estado de hilo (p. ej. resuelto)
- [x] Event handlers Svelte 5 (`onclick`) — build producción OK
- [x] E2E manual: cliente ↔ backoffice (crear, responder, estado, contadores)

## Android (sprint paralelo — fuera de este cierre)

- [ ] Paridad inbox / detail / RT / unread (models + repo interface ya esbozados)
- [ ] DI + ViewModel + Compose screens
- [ ] Tests unitarios / instrumentados de contadores y RT

No marcar el cierre web como incompleto por Android.

---

## DoD web (policy)

| Criterio | Evidencia |
|----------|-----------|
| Usuario crea thread y ve reply staff en RT | E2E + `startRealtime` en store |
| Aislamiento por `userId` | Queries “mine” + permisos Appwrite |
| Sin Pulse en camino feliz | `support.appwrite.repository` en DI |
| Unread usuario baja al abrir hilo | `MarkThreadReadCaseUse` + tests |

**Veredicto web:** listo para QA de soporte en cliente web. Android se planifica en sprint paralelo.
