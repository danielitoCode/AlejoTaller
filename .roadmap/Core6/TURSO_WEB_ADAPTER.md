# Turso web adapter (esqueleto lectura)

## Env (web/.env)

```env
VITE_DATA_PROVIDER=turso
VITE_TURSO_URL=libsql://TU-DB.turso.io
VITE_TURSO_AUTH_TOKEN=...
```

Con `VITE_DATA_PROVIDER=appwrite` (o ausente) se mantiene Appwrite.

## Install

```powershell
cd web
npm i @libsql/client
npm run dev
```

## Qué hace

| Pieza | Rol |
|-------|-----|
| `infrastructure/turso/turso.client.ts` | Singleton `@libsql/client/web` |
| `category.turso.repository.ts` | `getAll` / `getById` |
| `product.turso.repository.ts` | `getAll` / `getById` / `getByCategory` |
| containers | Eligen Turso vs Appwrite |

Offline-first (Dexie) se mantiene: Turso = “net”, Dexie = cache.

## Pendiente

- Escritura categorías/productos
- Soft-hold `incrementReserved` / `decrementReserved` en SQL transaccional
- Sales, purchases, stock_movements
- Token solo-lectura en el SPA (ideal: proxy backend; el token R/W en Vite es temporal para dev)

## Smoke

1. Al menos 1 fila en `categories` y `products` (status active).
2. Log: `[turso] client ready` / `[turso] Cargando productos`.
3. Home muestra catálogo sin 402 Appwrite.
