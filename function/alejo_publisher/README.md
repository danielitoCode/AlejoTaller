# alejo_publisher

Servicio HTTP pequeno para publicar eventos de verificacion de ventas en Pusher desde un entorno confiable.

## Endpoints

- `GET /health`
- `POST /sale-verification/publish`

## Variables de entorno

```env
PORT=3000
PUBLISHER_API_KEY=change-me
PUSHER_APP_ID=
PUSHER_KEY=
PUSHER_SECRET=
PUSHER_CLUSTER=mt1
ALLOW_ORIGIN=*
```

## Probar en localhost

1. Copia `.env.example` a `.env`
2. Completa las credenciales reales de Pusher
3. Instala dependencias:

```bash
npm install
```

4. Levanta el servicio:

```bash
npm run dev
```

5. Verifica salud:

```bash
curl http://localhost:3000/health
```

6. Prueba la publicacion:

```bash
curl -X POST http://localhost:3000/sale-verification/publish ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer change-me" ^
  -d "{\"saleId\":\"69c9593b002b167bf4fb\",\"userId\":\"698f82750024bfe2dc52\",\"decision\":\"confirmed\",\"amount\":20.5,\"productCount\":4}"
```

Si vas a llamar este servicio desde un emulador Android:

- usa `http://10.0.2.2:3000` en vez de `http://localhost:3000`

Si vas a llamar desde un dispositivo fisico:

- usa la IP local de tu PC, por ejemplo `http://192.168.1.20:3000`
- asegúrate de que firewall y red local lo permitan

## Payload esperado

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

## Respuesta

```json
{
  "ok": true,
  "channel": "sale-verification-698f82750024bfe2dc52",
  "eventName": "sale:confirmed"
}
```

## Flujo esperado

1. El operador actualiza Appwrite.
2. El operador verifica que `buy_state` cambio.
3. El operador llama este servicio.
4. Este servicio publica a Pusher.

## Deploy en Render

- Root Directory: `function/alejo_publisher`
- Build Command: `npm install && npm run build`
- Start Command: `npm run start`
