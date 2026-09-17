import { ENV } from "../../env";

/**
 * Publica eventos a Pusher Channels vía REST API.
 * KEY + SECRETS + APP_ID + cluster. Secret en cliente = temporal sin backend Pulse.
 */

function hexFromBuffer(buf: ArrayBuffer): string {
    return Array.from(new Uint8Array(buf))
        .map((b) => b.toString(16).padStart(2, "0"))
        .join("");
}

function md5Hex(message: string): string {
    function cmn(q: number, a: number, b: number, x: number, s: number, t: number) {
        a = (a + q + x + t) | 0;
        return (((a << s) | (a >>> (32 - s))) + b) | 0;
    }
    function ff(a: number, b: number, c: number, d: number, x: number, s: number, t: number) {
        return cmn((b & c) | (~b & d), a, b, x, s, t);
    }
    function gg(a: number, b: number, c: number, d: number, x: number, s: number, t: number) {
        return cmn((b & d) | (c & ~d), a, b, x, s, t);
    }
    function hh(a: number, b: number, c: number, d: number, x: number, s: number, t: number) {
        return cmn(b ^ c ^ d, a, b, x, s, t);
    }
    function ii(a: number, b: number, c: number, d: number, x: number, s: number, t: number) {
        return cmn(c ^ (b | ~d), a, b, x, s, t);
    }
    const bytes = new TextEncoder().encode(message);
    const n = bytes.length;
    const words: number[] = [];
    for (let i = 0; i < n; i++) words[i >> 2] |= bytes[i] << ((i % 4) * 8);
    words[n >> 2] |= 0x80 << ((n % 4) * 8);
    const bitLen = n * 8;
    const size = (((n + 8) >> 6) + 1) * 16;
    while (words.length < size) words.push(0);
    words[size - 2] = bitLen & 0xffffffff;
    words[size - 1] = (bitLen / 0x100000000) | 0;
    let a0 = 0x67452301, b0 = 0xefcdab89, c0 = 0x98badcfe, d0 = 0x10325476;
    for (let i = 0; i < words.length; i += 16) {
        let a = a0, b = b0, c = c0, d = d0;
        const x = words.slice(i, i + 16);
        a = ff(a, b, c, d, x[0], 7, -680876936); d = ff(d, a, b, c, x[1], 12, -389564586);
        c = ff(c, d, a, b, x[2], 17, 606105819); b = ff(b, c, d, a, x[3], 22, -1044525330);
        a = ff(a, b, c, d, x[4], 7, -176418897); d = ff(d, a, b, c, x[5], 12, 1200080426);
        c = ff(c, d, a, b, x[6], 17, -1473231341); b = ff(b, c, d, a, x[7], 22, -45705983);
        a = ff(a, b, c, d, x[8], 7, 1770035416); d = ff(d, a, b, c, x[9], 12, -1958414417);
        c = ff(c, d, a, b, x[10], 17, -42063); b = ff(b, c, d, a, x[11], 22, -1990404162);
        a = ff(a, b, c, d, x[12], 7, 1804603682); d = ff(d, a, b, c, x[13], 12, -40341101);
        c = ff(c, d, a, b, x[14], 17, -1502002290); b = ff(b, c, d, a, x[15], 22, 1236535329);
        a = gg(a, b, c, d, x[1], 5, -165796510); d = gg(d, a, b, c, x[6], 9, -1069501632);
        c = gg(c, d, a, b, x[11], 14, 643717713); b = gg(b, c, d, a, x[0], 20, -373897302);
        a = gg(a, b, c, d, x[5], 5, -701558691); d = gg(d, a, b, c, x[10], 9, 38016083);
        c = gg(c, d, a, b, x[15], 14, -660478335); b = gg(b, c, d, a, x[4], 20, -405537848);
        a = gg(a, b, c, d, x[9], 5, 568446438); d = gg(d, a, b, c, x[14], 9, -1019803690);
        c = gg(c, d, a, b, x[3], 14, -187363961); b = gg(b, c, d, a, x[8], 20, 1163531501);
        a = gg(a, b, c, d, x[13], 5, -1444681467); d = gg(d, a, b, c, x[2], 9, -51403784);
        c = gg(c, d, a, b, x[7], 14, 1735328473); b = gg(b, c, d, a, x[12], 20, -1926607734);
        a = hh(a, b, c, d, x[5], 4, -378558); d = hh(d, a, b, c, x[8], 11, -2022574463);
        c = hh(c, d, a, b, x[11], 16, 1839030562); b = hh(b, c, d, a, x[14], 23, -35309556);
        a = hh(a, b, c, d, x[1], 4, -1530992060); d = hh(d, a, b, c, x[4], 11, 1272893353);
        c = hh(c, d, a, b, x[7], 16, -155497632); b = hh(b, c, d, a, x[10], 23, -1094730640);
        a = hh(a, b, c, d, x[13], 4, 681279174); d = hh(d, a, b, c, x[0], 11, -358537222);
        c = hh(c, d, a, b, x[3], 16, -722521979); b = hh(b, c, d, a, x[6], 23, 76029189);
        a = hh(a, b, c, d, x[9], 4, -640364487); d = hh(d, a, b, c, x[12], 11, -421815835);
        c = hh(c, d, a, b, x[15], 16, 530742520); b = hh(b, c, d, a, x[2], 23, -995338651);
        a = ii(a, b, c, d, x[0], 6, -198630844); d = ii(d, a, b, c, x[7], 10, 1126891415);
        c = ii(c, d, a, b, x[14], 15, -1416354905); b = ii(b, c, d, a, x[5], 21, -57434055);
        a = ii(a, b, c, d, x[12], 6, 1700485571); d = ii(d, a, b, c, x[3], 10, -1894986606);
        c = ii(c, d, a, b, x[10], 15, -1051523); b = ii(b, c, d, a, x[1], 21, -2054922799);
        a = ii(a, b, c, d, x[8], 6, 1873313359); d = ii(d, a, b, c, x[15], 10, -30611744);
        c = ii(c, d, a, b, x[6], 15, -1560198380); b = ii(b, c, d, a, x[13], 21, 1309151649);
        a = ii(a, b, c, d, x[4], 6, -145523070); d = ii(d, a, b, c, x[11], 10, -1120210379);
        c = ii(c, d, a, b, x[2], 15, 718787259); b = ii(b, c, d, a, x[9], 21, -343485551);
        a0 = (a0 + a) | 0; b0 = (b0 + b) | 0; c0 = (c0 + c) | 0; d0 = (d0 + d) | 0;
    }
    function toHex(n: number) {
        let s = "";
        for (let j = 0; j < 4; j++) s += ((n >> (j * 8)) & 0xff).toString(16).padStart(2, "0");
        return s;
    }
    return toHex(a0) + toHex(b0) + toHex(c0) + toHex(d0);
}

async function hmacSha256Hex(secret: string, data: string): Promise<string> {
    const enc = new TextEncoder();
    const key = await crypto.subtle.importKey(
        "raw",
        enc.encode(secret),
        { name: "HMAC", hash: "SHA-256" },
        false,
        ["sign"],
    );
    const sig = await crypto.subtle.sign("HMAC", key, enc.encode(data));
    return hexFromBuffer(sig);
}

export type PusherTriggerResult =
    | { ok: true; via: "pusher-rest" | "pulse-http" }
    | { ok: false; reason: string };

export async function triggerPusherEvent(
    channel: string,
    event: string,
    data: unknown,
): Promise<PusherTriggerResult> {
    const ch = String(channel || "").trim();
    const ev = String(event || "").trim();
    if (!ch || !ev) return { ok: false, reason: "channel/event vacíos" };

    const pulseBase = ENV.pulseBaseUrl?.trim().replace(/\/$/, "");
    const pulseKey = ENV.pulseApiKey?.trim();
    if (pulseBase && pulseKey) {
        try {
            const res = await fetch(`${pulseBase}/pulse/event`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${pulseKey}`,
                    "X-Api-Key": pulseKey,
                },
                body: JSON.stringify({ channel: ch, event: ev, data }),
            });
            if (res.ok) return { ok: true, via: "pulse-http" };
            console.warn(`[Pusher] Pulse HTTP ${res.status}, intentando REST…`);
        } catch (e) {
            console.warn(`[Pusher] Pulse HTTP failed: ${e instanceof Error ? e.message : String(e)}`);
        }
    }

    const key = ENV.pusherKey?.trim();
    const secret = ENV.pusherSecrets?.trim();
    const appId = ENV.pusherAppId?.trim();
    const cluster = ENV.pusherCluster?.trim() || "mt1";
    if (!key || !secret || !appId) {
        return {
            ok: false,
            reason: "Sin Pulse ni KEY+SECRETS+APP_ID. Solo fan-out local.",
        };
    }

    const bodyObj = {
        name: ev,
        channel: ch,
        data: typeof data === "string" ? data : JSON.stringify(data ?? {}),
    };
    const body = JSON.stringify(bodyObj);
    const bodyMd5 = md5Hex(body);
    const ts = Math.floor(Date.now() / 1000).toString();
    const path = `/apps/${appId}/events`;
    const query =
        `auth_key=${key}&auth_timestamp=${ts}&auth_version=1.0&body_md5=${bodyMd5}`;
    const stringToSign = `POST\n${path}\n${query}`;
    const signature = await hmacSha256Hex(secret, stringToSign);
    const url = `https://api-${cluster}.pusher.com${path}?${query}&auth_signature=${signature}`;

    try {
        const res = await fetch(url, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body,
        });
        if (!res.ok) {
            const text = await res.text().catch(() => "");
            return { ok: false, reason: `Pusher REST ${res.status}: ${text.slice(0, 200)}` };
        }
        return { ok: true, via: "pusher-rest" };
    } catch (e) {
        return {
            ok: false,
            reason: `Pusher REST network: ${e instanceof Error ? e.message : String(e)}`,
        };
    }
}
