# SUPPORT_POLICY — Mensajería soporte ↔ usuario (Core 1 closure)

**Estado:** Web cliente integrado (cierre Core 1 web)  
**Alcance:** dash (gobierno) + AlejoTaller web  
**Android:** sprint paralelo (paridad web: inbox, detail, RT, unread) — no bloquea web  
**Transporte:** Appwrite Database + Realtime

Ver checklist: `.roadmap/Core1/SUPPORT_CORE1_CLOSURE.md`

## Modelo

- `support_threads`: userId, reason, subject, status, lastMessageAt, lastPreview, unread*
- `support_messages`: threadId, senderRole, senderId, body, createdAtIso

## UX cliente

Perfil → Soporte → mis consultas / nueva / detail con RT.

## DoD

Usuario crea y staff responde en < 3 s; aislamiento por userId; sin Pulse en camino feliz.
