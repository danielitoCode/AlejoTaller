export type PulseRefreshTarget = "support" | "sales";

function toTargetsFromEventName(eventName: string): PulseRefreshTarget[] {
    const name = String(eventName ?? "").toLowerCase();
    if (name.includes("all")) return ["support", "sales"];
    const targets: PulseRefreshTarget[] = [];
    if (name.includes("support")) targets.push("support");
    if (name.includes("sales")) targets.push("sales");
    return targets;
}

function readTargetsFromPayload(payload: unknown): PulseRefreshTarget[] {
    if (!payload) return [];
    if (typeof payload === "string") return toTargetsFromEventName(payload);

    if (typeof payload !== "object") return [];
    const anyPayload = payload as any;

    const rawTargets = anyPayload.targets ?? anyPayload.target ?? anyPayload.resources ?? anyPayload.refresh;
    if (Array.isArray(rawTargets)) {
        const targets: PulseRefreshTarget[] = [];
        for (const t of rawTargets) {
            const v = String(t ?? "").toLowerCase();
            if (v === "support") targets.push("support");
            if (v === "sales" || v === "venta" || v === "ventas") targets.push("sales");
        }
        return targets;
    }

    const targets: PulseRefreshTarget[] = [];
    if (anyPayload.support === true) targets.push("support");
    if (anyPayload.sales === true || anyPayload.ventas === true) targets.push("sales");

    if (typeof rawTargets === "string") return [...targets, ...toTargetsFromEventName(rawTargets)];
    return targets;
}

export function pulseRefreshTargets(eventName: string, payload: unknown): PulseRefreshTarget[] {
    const fromName = toTargetsFromEventName(eventName);
    const fromPayload = readTargetsFromPayload(payload);
    const merged = new Set<PulseRefreshTarget>([...fromName, ...fromPayload]);
    return [...merged];
}

