# Auth0 roles — app_metadata + Action (paridad labels Appwrite)

Documento compartido con `dash_alejo_taller`. Ver checklist y Action completa allí o debajo.

## Principio

- **Fuente de verdad en Auth0:** `app_metadata.roles` (análogo a labels Appwrite).
- **Única fuente en el cliente:** claim JWT `https://alejotaller.app/roles` (inyectado por Action).
- **Prohibido:** leer `app_metadata` en el browser o inventar roles en localStorage.

## Action Post-Login

```javascript
exports.onExecutePostLogin = async (event, api) => {
  const CLAIM = "https://alejotaller.app/roles";
  const fromMeta = event.user.app_metadata && event.user.app_metadata.roles;
  let roles = [];
  if (Array.isArray(fromMeta)) {
    roles = fromMeta.map(String).map((r) => r.trim().toLowerCase()).filter(Boolean);
  } else if (typeof fromMeta === "string" && fromMeta.trim()) {
    roles = fromMeta.split(/[\s,]+/).map((r) => r.trim().toLowerCase()).filter(Boolean);
  }
  const authz = event.authorization && event.authorization.roles;
  if (Array.isArray(authz)) {
    for (const r of authz) {
      const n = String(r).trim().toLowerCase();
      if (n && !roles.includes(n)) roles.push(n);
    }
  }
  if (roles.length === 0) return;
  api.idToken.setCustomClaim(CLAIM, roles);
  api.accessToken.setCustomClaim(CLAIM, roles);
};
```

Usuario staff ejemplo `app_metadata`:

```json
{ "roles": ["admin"] }
```

Tras editar metadata → **re-login**.

Web B2C: sin claim → `roles=[]` / viewer; no afecta guest local.
