#!/usr/bin/env bash
# Restaura NestedNavigationWrapper desde commit bueno y aplica logout Auth0.
set -euo pipefail
cd "$(git rev-parse --show-toplevel)"

git show dce23cce:web/src/core/infrastructure/presentation/navigation/NestedNavigationWrapper.svelte \
  > web/src/core/infrastructure/presentation/navigation/NestedNavigationWrapper.svelte

python3 - <<'PY'
from pathlib import Path
p = Path("web/src/core/infrastructure/presentation/navigation/NestedNavigationWrapper.svelte")
n = p.read_text()
if "performLogout" not in n:
    n = n.replace(
        'import {authContainer} from "../../../feature/auth/di/auth.container";',
        'import {authContainer} from "../../../feature/auth/di/auth.container";\n    import { performLogout } from "../../../feature/auth/presentation/util/performLogout";',
        1,
    )
    old = """    async function logout() {
        try { await authContainer.useCases.sessions.closeSession.execute(); }
        finally { clearSessionBoundState({ clearCart: true }); navController.resetTo("welcome-update"); }
    }"""
    new = """    async function logout() {
        await performLogout({
            closeAppwriteSession: async () => {
                await authContainer.useCases.sessions.closeSession.execute();
            },
            onLocalCleanup: () => {
                clearSessionBoundState({ clearCart: true });
                navController.resetTo("welcome-update");
            },
        });
    }"""
    if old in n:
        n = n.replace(old, new, 1)
    p.write_text(n)
print("OK: NestedNav restaurado + logout Auth0")
PY
