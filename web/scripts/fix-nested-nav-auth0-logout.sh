#!/usr/bin/env bash
set -euo pipefail
cd "$(git rev-parse --show-toplevel)"
GOOD=00da55b5057c71ff963bd0fa45903b3a1060fa66
OUT=web/src/core/infrastructure/presentation/navigation/NestedNavigationWrapper.svelte
git show "${GOOD}:${OUT}" > "$OUT"
python3 - <<'PY'
from pathlib import Path
N = Path("web/src/core/infrastructure/presentation/navigation/NestedNavigationWrapper.svelte").read_text()
if "getAuthPort" not in N:
    N = N.replace(
        'import { performLogout } from "../../../feature/auth/presentation/util/performLogout";',
        'import { performLogout } from "../../../feature/auth/presentation/util/performLogout";\n'
        '    import { getAuthPort, resolveAuthProvider } from "../../../feature/auth/di/authPort.factory";\n'
        '    import { userLikeFromAuthSession } from "../../../feature/auth/domain/util/authSessionBridge";',
        1,
    )
if "const useAuth0" not in N:
    N = N.replace(
        "    const internalNavController = rememberNavController(dashboard.path);",
        '    const useAuth0 = resolveAuthProvider() === "auth0";\n'
        "    const internalNavController = rememberNavController(dashboard.path);",
        1,
    )
old = """        currentUser = authContainer.useCases.accounts.getCurrentUser()
            .then((user) => {
                if (!hasClearAuthenticatedProfile(user)) {
                    forceVisitorMode(user);
                    if (import.meta.env.DEV) logNavAuthCheck(false, true, "force-visitor-unclear-profile");
                    const parsedHash = parseDeepLinkHash(window.location.hash);
                    const hasProductDeeplink = parsedHash?.top === "home" && (parsedHash.nested === productDetail.path || !!parsedHash.args?.productId);
                    if (!hasProductDeeplink) internalNavController.resetTo(dashboard.path);
                    resolvedUser = { name: "Visitante" };
                    return { name: "Visitante", email: "" };
                }
                sessionStore.setAuthenticatedSession();
                resolvedUser = user;
                profileStore.hydrateFromUser(user as any);
                startSupportBadgePipeline();
                promotionStore.syncAll().catch(() => toastStore.error("Error al sincronizar promociones"));
                saleStore.syncAll().catch(() => toastStore.error("Error al sincronizar reservas"));
                if (shouldOfferAdminChoice(user)) {
                    const choice = getStoredAdminChoice();
                    if (choice === "admin") continueToAdmin();
                    else if (choice !== "client") adminChoicePending = true;
                }
                return user;
            })
            .catch(() => {
                forceVisitorMode();
                resolvedUser = { name: "Visitante" };
                if (import.meta.env.DEV) logNavAuthCheck(false, true, "force-visitor-no-session");
                return { name: "Visitante", email: "" };
            });"""
new = """        if (useAuth0) {
            currentUser = (async () => {
                const auth = getAuthPort();
                if (auth) {
                    try {
                        await auth.init();
                        await auth.handleRedirectCallback();
                        const session = await auth.getSession();
                        if (session) {
                            const user = userLikeFromAuthSession(session);
                            sessionStore.setAuthenticatedSession();
                            resolvedUser = { name: user.name, prefs: { picture: user.photo_url } };
                            profileStore.hydrateFromUser({
                                name: user.name, email: user.email, photo_url: user.photo_url,
                                $id: user.id, id: user.id,
                            } as any);
                            if (import.meta.env.DEV) logNavAuthCheck(true, false, "auth0-nested-session");
                            if (shouldOfferAdminChoice(user as any)) {
                                const choice = getStoredAdminChoice();
                                if (choice === "admin") void continueToAdmin();
                                else if (choice !== "client") adminChoicePending = true;
                            }
                            return user;
                        }
                    } catch {
                        if (import.meta.env.DEV) logNavAuthCheck(false, true, "auth0-nested-error");
                    }
                }
                if (!get(sessionStore).isGuest) forceVisitorMode();
                resolvedUser = { name: "Visitante" };
                if (import.meta.env.DEV) logNavAuthCheck(false, true, "auth0-nested-guest");
                return { name: "Visitante", email: "" };
            })();
        } else {
        currentUser = authContainer.useCases.accounts.getCurrentUser()
            .then((user) => {
                if (!hasClearAuthenticatedProfile(user)) {
                    forceVisitorMode(user);
                    if (import.meta.env.DEV) logNavAuthCheck(false, true, "force-visitor-unclear-profile");
                    const parsedHash = parseDeepLinkHash(window.location.hash);
                    const hasProductDeeplink = parsedHash?.top === "home" && (parsedHash.nested === productDetail.path || !!parsedHash.args?.productId);
                    if (!hasProductDeeplink) internalNavController.resetTo(dashboard.path);
                    resolvedUser = { name: "Visitante" };
                    return { name: "Visitante", email: "" };
                }
                sessionStore.setAuthenticatedSession();
                resolvedUser = user;
                profileStore.hydrateFromUser(user as any);
                startSupportBadgePipeline();
                promotionStore.syncAll().catch(() => toastStore.error("Error al sincronizar promociones"));
                saleStore.syncAll().catch(() => toastStore.error("Error al sincronizar reservas"));
                if (shouldOfferAdminChoice(user)) {
                    const choice = getStoredAdminChoice();
                    if (choice === "admin") continueToAdmin();
                    else if (choice !== "client") adminChoicePending = true;
                }
                return user;
            })
            .catch(() => {
                forceVisitorMode();
                resolvedUser = { name: "Visitante" };
                if (import.meta.env.DEV) logNavAuthCheck(false, true, "force-visitor-no-session");
                return { name: "Visitante", email: "" };
            });
        }"""
if old not in N:
    raise SystemExit("onMount block not found")
Path("web/src/core/infrastructure/presentation/navigation/NestedNavigationWrapper.svelte").write_text(N.replace(old, new, 1))
print("OK NestedNav + Auth0 hydrate")
PY
echo "Listo. Commit y push:"
echo "  git add $OUT && git commit -m 'fix(auth): NestedNav Auth0 session' && git push origin Core6"
