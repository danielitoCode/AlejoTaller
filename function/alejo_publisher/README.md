# TallerAlejo Function Publisher

[<- Volver al README general](../../README.md)

<p align="center">
  <img src="https://img.shields.io/badge/Surface-Function%20Publisher-E53935?style=for-the-badge" alt="Function Publisher" />
  <img src="https://img.shields.io/badge/Stack-Node%20%7C%20TypeScript%20%7C%20Express-1B3A57?style=for-the-badge" alt="Stack Publisher" />
  <img src="https://img.shields.io/badge/Purpose-Secure%20Realtime%20Publishing-FF9F1C?style=for-the-badge" alt="Secure Realtime Publishing" />
</p>

## Rol Dentro Del Monorepo

`alejo_publisher` es un servicio HTTP pequeno cuyo objetivo es publicar eventos a Pusher desde un entorno mas confiable que el frontend movil.

Responsabilidades:

- recibir solicitudes autenticadas de publicacion
- traducir la decision operativa a un evento Pusher
- publicar sobre el canal esperado por cliente Android y web
- aislar secretos de Pusher del APK

## Endpoints

- `GET /health`
- `POST /sale-verification/publish`

## Payload Esperado

```json
{
  "saleId": "69c9593b002b167bf4fb",
  "userId": "698f82750024bfe2dc52",
  "decision": "confirmed",
  "amount": 20.5,
  "productCount": 4,
  "cause": null
}
```

## Respuesta Esperada

```json
{
  "ok": true,
  "channel": "sale-verification-698f82750024bfe2dc52",
  "eventName": "sale:confirmed"
}
```

## Variables De Entorno

```env
PORT=3000
PUBLISHER_API_KEY=tallerAlejoTestApiKey
PUSHER_APP_ID=
PUSHER_KEY=
PUSHER_SECRET=
PUSHER_CLUSTER=mt1
ALLOW_ORIGIN=*
```

## Prueba Local

1. Copia `.env.example` a `.env`
2. Completa las credenciales reales de Pusher
3. Instala dependencias

```bash
npm install
```

4. Levanta el servicio

```bash
npm run dev
```

5. Verifica salud

```bash
curl http://localhost:3000/health
```

6. Prueba publicacion

```bash
curl -X POST http://localhost:3000/sale-verification/publish ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer tallerAlejoTestApiKey" ^
  -d "{\"saleId\":\"69c9593b002b167bf4fb\",\"userId\":\"698f82750024bfe2dc52\",\"decision\":\"confirmed\",\"amount\":20.5,\"productCount\":4}"
```

## Uso Desde Android Operador

El flujo esperado es:

1. la app operadora actualiza Appwrite
2. la app verifica que `buy_state` cambie realmente
3. la app llama a esta function
4. la function publica en Pusher

Esto evita:

- exponer secretos de Pusher en frontend
- depender del reloj del dispositivo para la firma
- seguir publicando cuando el estado remoto no fue confirmado

## Deploy En Render

- `Root Directory`: `function/alejo_publisher`
- `Build Command`: `npm install && npm run build`
- `Start Command`: `npm run start`

## Navegacion Relacionada

- [README general del monorepo](../../README.md)
- [Android Operador](../../alejotallerscan/README.md)
- [Web Cliente](../../web/README.md)
