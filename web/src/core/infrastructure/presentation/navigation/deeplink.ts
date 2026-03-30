import { home, login, register, splash, welcome } from "./router";
import { buy, buyConfirm, dashboard, profile, reservation, reservationDetail, settings } from "./nested.router";

type TopLevelPath = typeof splash.path | typeof welcome.path | typeof login.path | typeof register.path | typeof home.path;
type NestedPath = typeof dashboard.path | typeof buy.path | typeof buyConfirm.path | typeof reservation.path | typeof reservationDetail.path | typeof profile.path | typeof settings.path;

export type ParsedDeepLink = {
    top: TopLevelPath;
    nested?: NestedPath;
    args?: Record<string, string>;
};

const topPaths = new Set<TopLevelPath>([splash.path, welcome.path, login.path, register.path, home.path]);
const nestedPaths = new Set<NestedPath>([dashboard.path, buy.path, buyConfirm.path, reservation.path, reservationDetail.path, profile.path, settings.path]);

export function parseDeepLinkHash(hash: string): ParsedDeepLink | null {
    const raw = hash.startsWith("#") ? hash.slice(1) : hash;
    const normalized = raw.startsWith("/") ? raw : `/${raw}`;
    const url = new URL(normalized || "/", "http://local");
    const parts = url.pathname.split("/").filter(Boolean);

    if (!parts.length) return null;
    const [top, nested] = parts;
    if (!topPaths.has(top as TopLevelPath)) return null;

    const args = Object.fromEntries(url.searchParams.entries());
    if (top !== home.path) {
        return { top: top as TopLevelPath, args };
    }

    if (!nested || !nestedPaths.has(nested as NestedPath)) {
        return { top: home.path, nested: dashboard.path, args };
    }

    return {
        top: home.path,
        nested: nested as NestedPath,
        args
    };
}

export function buildTopLevelHash(top: TopLevelPath, args?: Record<string, string | undefined | null>): string {
    const params = new URLSearchParams();
    Object.entries(args ?? {}).forEach(([key, value]) => {
        if (value) params.set(key, value);
    });
    const query = params.toString();
    return `#/${top}${query ? `?${query}` : ""}`;
}

export function buildHomeHash(nested: NestedPath, args?: Record<string, string | undefined | null>): string {
    const params = new URLSearchParams();
    Object.entries(args ?? {}).forEach(([key, value]) => {
        if (value) params.set(key, value);
    });
    const query = params.toString();
    return `#/${home.path}/${nested}${query ? `?${query}` : ""}`;
}
