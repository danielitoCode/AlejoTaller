# Restaurar NestedNavigationWrapper (obligatorio)

Los commits `5e264b0`, `9ffa721`, `681ae3d` dejaron NestedNav como stub.
El último bueno completo es **00da55b**.

## Comando (desde la raíz del repo)

```bash
git pull origin Core6
bash web/scripts/fix-nested-nav-auth0-logout.sh

# Verifica tamaño ~36k y que no diga PLACEHOLDER / RESTORED / SEE_FILE
wc -c web/src/core/infrastructure/presentation/navigation/NestedNavigationWrapper.svelte
grep -E 'NavHost|auth0-nested|PLACEHOLDER|SEE_FILE' web/src/core/infrastructure/presentation/navigation/NestedNavigationWrapper.svelte | head

git add web/src/core/infrastructure/presentation/navigation/NestedNavigationWrapper.svelte
git commit -m "fix(auth): NestedNav completo + Auth0 session hydrate"
git push origin Core6
```

## Qué hace el script

1. `git show 00da55b:…/NestedNavigationWrapper.svelte` → archivo completo
2. Parche: `useAuth0` + `getSession()` en onMount (sin Appwrite Accounts)
3. Mantiene `performLogout`

## Login

Login.svelte en Core6 está OK (UI completa + Auth0).
