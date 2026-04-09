# TallerAlejo Web Cliente

[<- Volver al README general](../README.md)

<p align="center">
  <img src="https://img.shields.io/badge/Surface-Web%20Cliente-0A7C66?style=for-the-badge" alt="Web Cliente" />
  <img src="https://img.shields.io/badge/Stack-Svelte%20%7C%20TypeScript%20%7C%20Dexie-1B3A57?style=for-the-badge" alt="Stack Web" />
  <img src="https://img.shields.io/badge/UI-M3%20Svelte%20%7C%20Lucide-FF9F1C?style=for-the-badge" alt="UI Web" />
</p>

## Rol Dentro Del Monorepo

Esta superficie representa la experiencia web del cliente final.

Responsabilidades:

- onboarding y experiencia comercial desde navegador
- compra o reservacion web
- persistencia offline en IndexedDB
- recepcion de eventos realtime de verificacion
- descarga guiada de APK para Android

## Principales Tecnologias

- Svelte
- TypeScript
- Vite
- Dexie
- Appwrite Web SDK
- Pusher JS
- M3 Svelte
- Lucide Svelte

## Filosofia Tecnica

La implementacion web sigue la misma linea del producto:

- capas por feature
- estado y logica separados de la UI
- sincronizacion local/remota
- suscripcion realtime al mismo contrato que Android

Esto evita que la version web sea una implementacion aislada o paralela sin reglas compartidas.

## Flujo De Venta

1. El cliente genera una compra o reservacion.
2. La app persiste la informacion y notifica a los canales correspondientes.
3. El operador valida la reserva.
4. La app web escucha el evento `sale:confirmed` o `sale:rejected`.
5. La UI se actualiza en funcion del estado final.

## Desarrollo

Dentro de `web/`:

```bash
pnpm install
pnpm dev
```

Chequeo de tipos:

```bash
npm run -s check
```

## Configuracion Esperada

Variables frecuentes:

- Appwrite endpoint
- Appwrite project id
- Pusher key y cluster
- URL de dashboard admin
- URLs de APK y releases
- Telegram bot settings si aplica el flujo comercial actual

## Estado Actual

La app web ya esta alineada con el MVP comercial:

- toasts unificados
- carga visual durante procesamiento de compra
- redirect administrativo controlado
- suscripcion realtime consistente con Android
- descarga de APK desde releases

## Navegacion Relacionada

- [README general del monorepo](../README.md)
- [Android Cliente](../app/README.md)
- [Android Operador](../alejotallerscan/README.md)
- [Function Publisher](../function/alejo_publisher/README.md)
