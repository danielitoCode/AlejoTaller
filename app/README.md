# TallerAlejo Android Cliente

[<- Volver al README general](../README.md)

<p align="center">
  <img src="https://img.shields.io/badge/Surface-Android%20Cliente-0A7C66?style=for-the-badge" alt="Android Cliente" />
  <img src="https://img.shields.io/badge/Stack-Kotlin%20%7C%20Compose%20%7C%20Room-1B3A57?style=for-the-badge" alt="Stack Android" />
  <img src="https://img.shields.io/badge/Mode-Offline--First-FF9F1C?style=for-the-badge" alt="Offline First" />
</p>

## Rol Dentro Del Monorepo

Esta aplicacion representa la experiencia Android del cliente final dentro del ecosistema **TallerAlejo**.

Responsabilidades:

- autenticacion y sesion del usuario
- experiencia de catalogo y compra
- reservaciones con o sin pago online
- persistencia local para continuidad de uso
- recepcion de eventos realtime de verificacion de ventas

## Puntos Tecnicos Clave

- Kotlin
- Jetpack Compose
- Material 3
- Room
- Koin
- Coroutines
- StateFlow
- Appwrite SDK
- Pusher

## Arquitectura Aplicada

La app sigue el esquema del monorepo:

- `data`
- `domain`
- `presentation`

Y reutiliza modulos compartidos para evitar divergencias con otras superficies:

- `shared-auth`
- `shared-core`
- `shared-data`
- `shared-sale`

## Lo Mas Importante Del Flujo

1. El cliente crea compra o reservacion.
2. La venta se persiste local y remotamente segun el flujo disponible.
3. El sistema operador procesa la reserva.
4. La app escucha eventos de verificacion desde Pusher.
5. La UI local se reconcilia con el estado confirmado o rechazado.

## Ejecucion

Desde la raiz del repositorio:

```bash
./gradlew :app:assembleDebug
```

Compilacion rapida:

```bash
./gradlew :app:compileDebugKotlin
```

## Configuracion Esperada

La app depende de configuracion inyectada desde `local.properties` y `BuildConfig`, incluyendo:

- Appwrite endpoint
- Appwrite project id
- claves publicas necesarias para realtime

## Estado Actual

La superficie Android cliente ya forma parte del MVP funcional y esta alineada con:

- la estrategia offline-first del producto
- la verificacion por operadora
- el consumo realtime comun con web

## Navegacion Relacionada

- [README general del monorepo](../README.md)
- [Android Operador](../alejotallerscan/README.md)
- [Web Cliente](../web/README.md)
