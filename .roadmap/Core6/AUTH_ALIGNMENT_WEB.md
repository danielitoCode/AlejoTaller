# Alineación Auth — AlejoTaller web (Core6)

## Principio

| Capa | Web (cliente) | Dash (panel) |
|------|---------------|--------------|
| **Autenticación** | Auth0 (mismo tenant) | Auth0 |
| **Application SPA** | web-spa · `:5174` | dash-spa · `:5173` |
| **Autorización** | Cualquier sesión Auth0 → cliente | Solo `owner` / `admin` / `sales` |
| **Guest** | Flag local (sin Appwrite) | N/A → welcome |
| **Google** | Auth0 Social `google-oauth2` | Auth0 Social |
| **One Tap / Appwrite session** | **OFF** si `VITE_AUTH_PROVIDER=auth0` | OFF si auth0 |

## Env web

```bash
VITE_AUTH_PROVIDER=auth0
VITE_AUTH0_DOMAIN=…
VITE_AUTH0_CLIENT_ID=<web-spa>
VITE_AUTH0_REDIRECT_URI=http://localhost:5174
VITE_AUTH0_LOGOUT_RETURN_TO=http://localhost:5174
```

## Archivos clave

- `Splash.svelte` — Auth0 callback → home autenticado; sin sesión → guest local o welcome
- `Login.svelte` — capa Auth0 si provider=auth0; legacy email/Google One Tap solo si `appwrite`
- `LoginAuth0Actions.svelte` — Universal Login + Google connection
- `Auth0AuthAdapter.ts` + `logAuth0` (solo DEV)
- `performLogout.ts` — logout Auth0 o Appwrite
- `NestedNavigationWrapper.svelte` — **debe restaurarse** con `web/scripts/fix-nested-nav-auth0-logout.sh`

## Restore NestedNav (obligatorio si el archivo es PLACEHOLDER)

```bash
bash web/scripts/fix-nested-nav-auth0-logout.sh
git add web/src/core/infrastructure/presentation/navigation/NestedNavigationWrapper.svelte
git commit -m "fix(auth): NestedNav restaurado + logout Auth0"
git push origin Core6
```

## Qué no hacer

- No mezclar One Tap Google + sesión Appwrite con Auth0 activo
- No llamar `openGuestSession` Appwrite con Auth0 (billing)
- No usar `resetTo` en dash NavController (solo web tiene `resetTo`)
