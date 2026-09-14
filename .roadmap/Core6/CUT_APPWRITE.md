# Corte Appwrite (Auth0 + Turso)

Con `VITE_AUTH_PROVIDER=auth0` y `VITE_DATA_PROVIDER=turso`:

| Área | Comportamiento |
|------|----------------|
| Account / Session Appwrite | **No** — `sessionStore.getCurrentUser` → Auth0 |
| Splash auth | Solo Auth0 / guest local |
| NestedNav sesión | Auth0 branch; legacy Appwrite solo si `!useAuth0` |
| Catálogo product/category | Turso |
| Promo / sale / support | **Skip** Appwrite (stores guardan Auth0) |
| Product RT Appwrite | Skip |

```env
VITE_AUTH_PROVIDER=auth0
VITE_DATA_PROVIDER=turso
VITE_TURSO_URL=...
VITE_TURSO_AUTH_TOKEN=...
VITE_AUTH0_DOMAIN=...
VITE_AUTH0_CLIENT_ID=...
```

Tras pull: hard refresh. No deberían aparecer `AppwriteException` ni `billing_limit` en login/home.
