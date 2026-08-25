/**
 * Simple fixed-window rate limiter (per isolate).
 * Suitable for Cloudflare Worker single-isolate protection against bursts.
 * Not a global multi-edge quota — pair with CF Rate Limiting rules in prod if needed.
 */

export interface RateLimitConfig {
  /** Max requests per window */
  maxRequests: number;
  /** Window length in ms */
  windowMs: number;
}

export interface RateLimitResult {
  allowed: boolean;
  remaining: number;
  resetAt: number;
}

interface Bucket {
  count: number;
  windowStart: number;
}

const buckets = new Map<string, Bucket>();

const DEFAULT_CONFIG: RateLimitConfig = {
  maxRequests: 60,
  windowMs: 60_000,
};

export function parseRateLimitRpm(raw: string | undefined): number {
  if (!raw) return 60;
  const n = Number.parseInt(raw, 10);
  if (!Number.isFinite(n) || n < 1) return 60;
  return Math.min(n, 10_000);
}

/**
 * Check and consume one request for `key` (e.g. client IP).
 */
export function checkRateLimit(
  key: string,
  config: RateLimitConfig = DEFAULT_CONFIG
): RateLimitResult {
  const now = Date.now();
  let bucket = buckets.get(key);

  if (!bucket || now - bucket.windowStart >= config.windowMs) {
    bucket = { count: 0, windowStart: now };
    buckets.set(key, bucket);
  }

  if (bucket.count >= config.maxRequests) {
    return {
      allowed: false,
      remaining: 0,
      resetAt: bucket.windowStart + config.windowMs,
    };
  }

  bucket.count += 1;
  return {
    allowed: true,
    remaining: Math.max(0, config.maxRequests - bucket.count),
    resetAt: bucket.windowStart + config.windowMs,
  };
}

/** Test helper — clear all buckets */
export function resetRateLimitState(): void {
  buckets.clear();
}

export function clientKeyFromRequest(request: Request): string {
  const cfIp = request.headers.get("cf-connecting-ip");
  if (cfIp) return `ip:${cfIp}`;
  const xff = request.headers.get("x-forwarded-for");
  if (xff) {
    const first = xff.split(",")[0]?.trim();
    if (first) return `ip:${first}`;
  }
  return "ip:unknown";
}
