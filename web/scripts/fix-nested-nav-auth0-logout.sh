#!/usr/bin/env bash
# Restaura NestedNavigationWrapper (corrupto por PLACEHOLDER) + logout Auth0.
set -euo pipefail
cd "$(git rev-parse --show-toplevel)"

GOOD=dce23cce259f126999d8dc74fc8afb6d4d902bd7
OUT=web/src/core/infrastructure/presentation/navigation/NestedNavigationWrapper.svelte

git show "${GOOD}:${OUT}" > "$OUT"

python3 - <<'PY'
from pathlib import Path
p = Path("web/src/core/infrastructure/presentation/navigation/NestedNavigationWrapper.svelte")
n = p.read_text()
if "SEE_LOCAL" in n or n.strip() == "PLACEHOLDER":
    raise SystemExit("restore failed — still placeholder")
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
    if old not in n:
        raise SystemExit("logout block not found after restore")
    n = n.replace(old, new, 1)
    p.write_text(n)
print("OK NestedNav restaurado + performLogout")
PY
