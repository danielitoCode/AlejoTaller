# AGENTS.md — TallerAlejo Development Guide

**TallerAlejo** is an offline-first mobile + web application for workshop management (inventory, sales, categories) with bidirectional sync to Appwrite and real-time updates via Pusher.

## Architecture Overview

### Three-Layer Design (Per Feature)
Each feature module (`auth`, `product`, `category`, `sale`, `notifications`) implements:
- **Data Layer**: DAOs (Room), DTOs, repositories (includes sync logic)
- **Domain Layer**: Entities, use cases, repository interfaces
- **Presentation Layer**: ViewModels (using `MutableStateFlow`), Compose screens

### Data Flow Pattern: Offline-First Reconciliation
```
Local (Room DAO) ← → Remote (Appwrite) 
             ↑ sync()
    ┌────────┴─────────────────────┐
    │ resolvePendingLocals()        │ Find local entries missing remotely
    │ pushWithRetry()               │ Upload pending with exponential backoff
    │ mergeSyncResult()             │ Merge remote after push with failed pending
    └───────────────────────────────┘
```
**Real use case**: `SaleOfflineFirstRepository.sync()` (see `feature/sale/data/repository/`):
- Fetches local & remote sales, separates by user
- Identifies pending (local-only), pushes with retries
- Merges results to maintain consistency

## Critical Workflows

### Build & Run Android
```bash
# Debug
./gradlew assembleDebug
# Run tests (includes reconciliation tests)
./gradlew test
# Instrumented tests (need device/emulator)
./gradlew connectedAndroidTest
```
**Key config**: `local.properties` must contain Appwrite/Pusher credentials (referenced in `BuildConfig`)

### Build & Run Web
```bash
cd web
pnpm install && pnpm dev
```
**Database**: Dexie (IndexedDB) for offline storage; mirrors Android Room schema

### DI Registration Pattern
All modules load in `TallerAlejoApp.onCreate()`:
```kotlin
startKoin {
    modules(infrastructureModule, authFeatureDiModule, ..., saleFeatureModule)
}
```
Each feature registers: Repositories → Use Cases → ViewModels (as `viewModel { ... }`)

## Project-Specific Conventions

### ViewModel State Management
- **Always use `MutableStateFlow` + `asStateFlow()`** for immutability
- **State classes for complex UI** (e.g., `ProfileUiState`, `CartUiState`)
- **Never expose private `MutableStateFlow`** directly; wrap with `.asStateFlow()`

### Sync & Reconciliation Testing
- **Unit tests** in `src/test/`: `SaleSyncReconciliationTest` validates `resolvePendingLocals()` logic
- **Instrumented tests** in `src/androidTest/`: integration with Room + real sync scenarios
- **Pattern**: Mock DAO/Net layers, assert correct pending ID selection & merge behavior

### DTO ↔ Domain Mapping
- **DTOs are serializable, schema-bound** (`@Serializable`, Room `@Entity`)
- **Domain entities are clean, business-logic focused** (no DB annotations)
- **Mappers in data layer**: `toDomain()` (DTO → Entity), constructor helpers for reverse
- **Example**: `SaleDto` (with `@SerialName` for Appwrite field names) → `Sale` entity

### Real-Time Event Handling
- `RealtimeSyncViewModel` watches Pusher for sale updates
- `InterpretSaleRealtimeEventCaseUse` validates events (belongs to active user, ID is pending)
- Updates trigger local reconciliation; failed reals-time pushes logged but don't fail sync

### Feature Modules Structure
Each feature has **exactly this layout**:
```
feature/{feature}/
  data/
    dao/        # Room @Dao interfaces
    dto/        # Serializable, schema-bound entities
    repository/ # OfflineFirst impl + Net impl
  domain/
    caseUse/    # One use case per file, `suspend` operations
    entity/     # Clean entities (no @Entity, @Serializable, etc.)
    repository/ # Interfaces only (data layer implements)
  presentation/
    viewmodel/  # State: MutableStateFlow, actions: suspend fun
    screen/     # Composable screen root components
    model/      # UI-specific data (e.g., `UiSaleItem`)
  di/
    {feature}FeatureModule.kt  # Koin module: DAO + Repo + UseCases + VMs
```

## Integration Points

### Appwrite Backend
- **Client SDK** initialized in `infrastructureDiModule`: `Client().setEndpoint(...).setProject(...)`
- **Collections**: Products, Categories, Sales, Promotions (schema in `app/schemas/`)
- **Credentials**: Injected from `BuildConfig` (from `local.properties`)

### Pusher Real-Time
- **Channels**: One per user (`sales__{userId}`); monitors incoming/updated sales
- **Manager**: `PusherManager` wraps raw Pusher client, exposed as `RealtimeSyncGateway`
- **Reconnection**: Automatic; handled by Pusher library

### Room Local Database
- **Single database**: `AppBD` with 4 DAOs (Categories, Products, Sales, Promotions)
- **Migrations**: `AppBDMigrations.ALL` registered; version 7 current
- **Type converters**: `DateTimeConverter` (kotlinx.datetime ↔ SQLite)
- **Transactions**: `replaceAll()` pattern (delete all + insert batch) for sync

### Web Frontend (Parallel Implementation)
- **Same 3-layer pattern** in TypeScript (SvelteKit + Vite)
- **Dexie + Appwrite** mirroring Android stack
- **Shared sync logic** (resolvePendingLocals also in `web/src/core/feature/...`)

## Key Files for Navigation

| Concept | File(s) |
|---------|---------|
| Offline-First Logic | `feature/sale/data/repository/SaleOfflineFirstRepository.kt` (resolvePendingLocals, pushWithRetry, mergeSyncResult) |
| DI Setup | `infraestructure/di/infrastructureDiModule.kt`, `feature/{feature}/di/{feature}FeatureModule.kt` |
| Room Schema | `feature/{feature}/data/dto/*.kt`, `feature/{feature}/data/dao/*.kt` |
| Sync Tests | `src/test/java/.../SaleSyncReconciliationTest.kt`, `src/androidTest/...` |
| Real-Time | `infraestructure/core/data/realtime/RealTimeManagerImpl.kt`, `feature/sale/domain/realtime/RealtimeSyncGateway.kt` |
| State Management | `feature/{feature}/presentation/viewmodel/*.kt` (always MutableStateFlow) |
| Compose UI Root | `infraestructure/core/presentation/navigation/InternalNavigationWrapper.kt` |

## Debugging Tips

1. **Offline-first sync failing**: Check `SaleOfflineFirstRepository.sync()` logs; verify Appwrite credentials in `BuildConfig`
2. **ViewModel state not updating**: Ensure using `.asStateFlow()` and `update { }` blocks
3. **Real-time events not received**: Verify Pusher cluster, subscription scope in `RealtimeSyncViewModel.updateSubscriptionScope()`
4. **Tests failing**: Run with `isReturnDefaultValues = true` (already set in `testOptions`); use Koin test runner for Android tests
5. **Build/sync credential issues**: Add valid `APPWRITE_PROJECT_ID`, `APPWRITE_PROJECT_ENDPOINT`, `PUSHER_API_KEY`, `PUSHER_CLUSTER` to `local.properties`

## MVP Roadmap Context

**Current status**: Core architecture solid; sales feature complete with offline-first sync; products/categories basic; hardening in progress.  
**Critical path**: Complete CRUD for products/categories → bidirectional sync for all features → robust error messaging → test coverage.

