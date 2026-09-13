# Patch logout Auth0 en NestedNavigationWrapper (web)

El archivo `NestedNavigationWrapper.svelte` debe restaurarse si quedó corrupto:

```bash
git checkout dce23cce -- web/src/core/infrastructure/presentation/navigation/NestedNavigationWrapper.svelte
```

Luego aplicar:

## 1. Import

```ts
import { performLogout } from "../../../feature/auth/presentation/util/performLogout";
```

## 2. Reemplazar `logout()`

```ts
async function logout() {
    const result = await performLogout({
        closeAppwriteSession: async () => {
            await authContainer.useCases.sessions.closeSession.execute();
        },
        onLocalCleanup: () => {
            clearSessionBoundState({ clearCart: true });
            navController.resetTo("welcome-update");
        },
    });
    // Si result === "auth0-redirect", Auth0 ya redirige a returnTo.
}
```

`performLogout.ts` ya está en el repo.
