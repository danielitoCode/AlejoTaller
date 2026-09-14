# Auth0 + Google: registro (vs Appwrite One Tap)

## Appwrite (antes)

1. Google One Tap → credential (id_token)
2. App **no** tenía cuenta → `createAccount(email, password=sub)`
3. Luego `createSession(email, sub)`

Google y Appwrite eran **dos sistemas**. Había que **materializar** el usuario en Appwrite.

## Auth0 (ahora)

1. `loginWithRedirect({ connection: "google-oauth2" })` → Universal Login
2. Google OAuth en **Auth0**
3. Auth0 **crea el usuario en el primer login** (connection Google)
4. Redirect `?code=&state=` → `handleRedirectCallback()` → tokens en localStorage
5. `getSession()` → `AuthSession` (sub, email, roles)

**No** hace falta registrar con email+sub en tu app. **No** pedir Google otra vez.

| Concepto | Dónde vive |
|----------|------------|
| Identidad (quién eres) | Auth0 User |
| Sesión SPA | Auth0 SDK (localStorage) |
| Perfil de negocio (carrito, ventas) | Fase DB (Turso) — más adelante |

## Auth0 Dashboard (obligatorio)

1. **Authentication → Social → Google** habilitado  
2. Application **web-spa** → connections → Google **on**  
3. **Allow Sign Ups** / no bloquear usuarios nuevos  
4. Users → tras primer Google login debe aparecer `google-oauth2|…`

## Logs esperados (éxito)

```
[Auth0] loginWithRedirect connection=google-oauth2
[Auth0] handleRedirectCallback: procesando code/state…
[Auth0] callback OK
[Auth0] session sub=google-oauth2|… email=…
[AuthCheck] auth-auth0 → home autenticado
[AuthCheck] auth0-nested-session
```

## Tu log actual

```
getSession: no autenticado  → aún no hay callback (no login completado)
AUTH0-LOCAL-GUEST           → correcto sin sesión
FORCE-VISITOR-NO-SESSION    → NestedNav aún pegaba a Appwrite (arreglar con script)
billing_limit_exceeded      → datos aún en Appwrite (Fase DB pendiente)
```

## Qué NO hacer

- No recrear “email + sub como password” en Auth0 desde el cliente SPA  
- No One Tap paralelo a Auth0  
- Management API create user solo si un backend lo necesita (no es el flujo Google Social)
