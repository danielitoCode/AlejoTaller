# Auth0 — app_metadata.role (un rol) + Action

## app_metadata

```json
{
  "role": "admin"
}
```

Un solo string: `owner` | `admin` | `sales` | `operator` | `viewer`.

## Action Post-Login

```javascript
exports.onExecutePostLogin = async (event, api) => {
  const CLAIM = "https://alejotaller.app/roles";
  const raw = event.user.app_metadata && event.user.app_metadata.role;
  let role = "";
  if (typeof raw === "string") role = raw.trim().toLowerCase();
  else if (Array.isArray(raw) && raw.length) role = String(raw[0]).trim().toLowerCase();
  if (!role && event.authorization && Array.isArray(event.authorization.roles) && event.authorization.roles[0]) {
    role = String(event.authorization.roles[0]).trim().toLowerCase();
  }
  if (!role) return;
  api.idToken.setCustomClaim(CLAIM, role);
  api.accessToken.setCustomClaim(CLAIM, role);
};
```

Cliente: solo claim JWT. Sin claim → sin roles de staff (B2C / guest OK).
