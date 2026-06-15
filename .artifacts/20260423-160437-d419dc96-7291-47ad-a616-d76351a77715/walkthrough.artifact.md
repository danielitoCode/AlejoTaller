# Walkthrough - Exchange Feature Implementation (Android)

Se ha completado la implementación de la característica de tasa de cambio (`Exchange`) en el módulo Android, asegurando paridad funcional y técnica con el módulo Web.

## Cambios Realizados

### Capa de Dominio
- **Entidad `CupExchange`**: Actualizada para incluir validaciones estrictas y nomenclatura consistente (`updatedAt`).
- **Repositorio `ExchangeRepository`**: Definido con métodos `suspend` para operaciones asíncronas.
- **Casos de Uso**: Implementados `GetTodayExchangeCaseUse` y `GetCachedTodayExchangeCaseUse`.

### Capa de Datos
- **DTOs**: Creado `CupExchangeDTO` para la API y `CupExchangeLocalDto` para Room.
- **DAO**: Implementado `ExchangeDao` para persistencia local.
- **Repositorio Offline-First**: Implementado `ExchangeOfflineFirstRepository` que gestiona la lógica de caché y red.
- **Network**: `ExchangeNetRepository` configurado con Ktor para consultar `directoriocubano.info`.
- **Base de Datos**: Actualizada `AppBD` (versión 10) para incluir la tabla de tasas de cambio.

### Capa de Presentación
- **ViewModel**: `ExchangeViewModel` gestiona el estado de la moneda y el refresco de datos.
- **UI Component**: `CurrencySwitch` permite al usuario alternar entre CUP y USD en la interfaz.

### Inyección de Dependencias
- **Koin**: Creado `exchangeFeatureModule` y registrado en la aplicación principal.

## Verificación
- Se han creado todos los archivos necesarios siguiendo la arquitectura de 3 capas.
- Se ha verificado la configuración de DI para asegurar que todos los componentes se instancien correctamente.
- La estructura de archivos ahora es espejo de la versión Web.
