# TallerAlejo Android Operador

[<- Volver al README general](../README.md)

<p align="center">
  <img src="https://img.shields.io/badge/Surface-Android%20Operador-0D6EFD?style=for-the-badge" alt="Android Operador" />
  <img src="https://img.shields.io/badge/Stack-Kotlin%20%7C%20Compose%20%7C%20ML%20Kit-1B3A57?style=for-the-badge" alt="Stack Operador" />
  <img src="https://img.shields.io/badge/Focus-Scan%20%7C%20Verify%20%7C%20Sync-FF9F1C?style=for-the-badge" alt="Scan Verify Sync" />
</p>

## Rol Dentro Del Monorepo

Esta aplicacion es la superficie operativa del sistema.  
Su objetivo es permitir a un operador validar reservas y ventas en tiempo real, con respaldo local y sincronizacion remota controlada.

Responsabilidades:

- escaneo QR
- carga manual de ventas
- verificacion o rechazo de reservas
- historial local de acciones
- sincronizacion de pendientes locales
- emision del evento realtime una vez validado el cambio remoto

## Principales Tecnologias

- Kotlin
- Jetpack Compose
- Material 3
- CameraX
- ML Kit Barcode Scanning
- Room
- Koin
- Coroutines
- Appwrite SDK
- OkHttp

## Flujo Operativo

1. El operador escanea o carga una venta.
2. La app valida los datos del QR contra Appwrite.
3. El operador confirma o rechaza.
4. La app actualiza `buy_state` en Appwrite.
5. La app verifica que Appwrite refleje el estado esperado.
6. La app llama a `function/alejo_publisher`.
7. La function publica el evento a Pusher.
8. La app registra localmente la accion del operador.

## Puntos Tecnicos Relevantes

- la publicacion a Pusher ya no se firma desde el dispositivo
- el historial del operador se conserva solo localmente
- la UI esta montada sobre un scroll raiz unico para evitar errores de constraints en Compose
- el flujo se corta si Appwrite no confirma el cambio remoto

## Ejecucion

Desde la raiz del repositorio:

```bash
./gradlew :alejotallerscan:assembleDebug
```

Compilacion rapida:

```bash
./gradlew :alejotallerscan:compileDebugKotlin
```

## Configuracion Esperada

Se esperan propiedades para:

- Appwrite
- publisher HTTP
- API key del publisher

Ejemplo conceptual:

```properties
PUBLISHER_BASE_URL=https://alejotaller-publisher.onrender.com
PUBLISHER_API_KEY=tallerAlejoTestApiKey
```

## Estado Actual

La aplicacion operadora ya cubre el nucleo operativo del MVP:

- captura
- validacion
- confirmacion
- rechazo
- historial local
- sincronizacion de pendientes

## Navegacion Relacionada

- [README general del monorepo](../README.md)
- [Android Cliente](../app/README.md)
- [Function Publisher](../function/alejo_publisher/README.md)
