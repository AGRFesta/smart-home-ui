# Architecture

## Principles

1. **Shared-first:** all logic and UI lives in `shared/commonMain`. Code moves to a platform-specific source set only when it must (e.g., Android permissions, desktop file dialogs).
2. **Stateless composables:** Composables receive state and emit events — they contain no business logic and no direct API calls.
3. **ViewModel owns state:** each screen has exactly one ViewModel that holds a `StateFlow<UiState>` and is the single source of truth for that screen.
4. **Depend on interfaces at boundaries:** ViewModels depend on interfaces (`ApiClient`, `Repository`), never on concrete HTTP or storage implementations.

---

## Module Dependencies

```
androidApp  ──┐
              ├──▶  shared  ──▶  (external libs only)
desktopApp  ──┘
```

- `androidApp` and `desktopApp` depend on `shared`. They are thin shells: entry point + dependency wiring only.
- `shared` has no knowledge of `androidApp` or `desktopApp`.
- No circular dependencies between modules.

---

## Layer Structure (inside `shared/commonMain`)

```
org.agrfesta.sh.ui
├── <feature>/          one package per feature (e.g., devices, rooms)
│   ├── <Feature>Screen.kt       — Composable, stateless
│   ├── <Feature>ViewModel.kt    — state holder, event handler
│   └── <Feature>UiState.kt      — sealed class for this screen's state
├── api/                API client interfaces + request/response models
├── navigation/         nav graph definition and route constants
└── theme/              MaterialTheme setup, colors, typography
```

Platform-specific implementations (e.g., `api/KtorDeviceApiClient.kt`) live in `androidMain` / `jvmMain` and are injected at the entry point.

---

## Screen Anatomy

Every screen follows this exact structure:

### UiState
```kotlin
sealed class DevicesUiState {
    data object Loading : DevicesUiState()
    data class Success(val devices: List<Device>) : DevicesUiState()
    data class Error(val message: String) : DevicesUiState()
}
```

### ViewModel
```kotlin
class DevicesViewModel(
    private val apiClient: DeviceApiClient,
    private val scope: CoroutineScope,
) {
    private val _uiState = MutableStateFlow<DevicesUiState>(DevicesUiState.Loading)
    val uiState: StateFlow<DevicesUiState> = _uiState.asStateFlow()

    fun loadDevices() {
        scope.launch { /* ... */ }
    }
}
```

- The `CoroutineScope` is **always injected** — never created inside the ViewModel — so tests can pass a `TestScope`.
- No `viewModelScope` from AndroidX: using an injected scope keeps the ViewModel testable in `commonTest`.

### Composable screen
```kotlin
@Composable
fun DevicesScreen(viewModel: DevicesViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DevicesContent(uiState = uiState, onRetry = viewModel::loadDevices)
}

@Composable
private fun DevicesContent(uiState: DevicesUiState, onRetry: () -> Unit) {
    // pure rendering, no ViewModel reference
}
```

The `Content` composable is a private rendering function that takes only plain values and lambdas — this is what `@Preview` and Compose UI tests target.

---

## Naming Conventions

| Artifact | Convention | Example |
|---|---|---|
| Screen composable | `<Feature>Screen` | `DevicesScreen` |
| Rendering composable | `<Feature>Content` (private) | `DevicesContent` |
| ViewModel | `<Feature>ViewModel` | `DevicesViewModel` |
| UI state | `<Feature>UiState` | `DevicesUiState` |
| API client interface | `<Feature>ApiClient` | `DeviceApiClient` |
| API client implementation | `Ktor<Feature>ApiClient` | `KtorDeviceApiClient` |
| Test class | `<Subject><MethodOrBehavior>Test` | `DevicesViewModelLoadDevicesTest` |

---

## System Boundaries

The following are the **only** places where mocks are used in tests:

| Boundary | Interface location | What crosses it |
|---|---|---|
| Smart Home backend API | `api/` | HTTP calls (Ktor or similar) |
| Platform APIs | `platform/` | Camera, filesystem, OS permissions |
| Navigation | `navigation/` | Screen transitions triggered by ViewModel events |

All other collaborators (domain logic, UiState transformations, ViewModel methods) are tested with real implementations.

---

## Application Flows

### Startup Flow

```
Entry point (MainActivity / main.kt)
  │
  ├── creates: TokenRepository, KtorHomeApiClient
  ├── creates: StartupViewModel, AuthViewModel, HomeViewModel  (manual wiring, no DI framework)
  └── calls:   startupViewModel.checkToken()
                        │
                        ▼
              StartupUiState (StateFlow)
              ├── Loading        → PikestaApp shows CircularProgressIndicator
              ├── TokenPresent   → AppNavGraph starts at Routes.HOME
              └── TokenAbsent    → AppNavGraph starts at Routes.AUTH
```

`collectAsState()` is called in the top-level entry-point Composable and the resolved `UiState` is passed down as a parameter, so child Composables receive plain state rather than collecting the `StateFlow` themselves.

---

### Auth Flow

```
AuthScreen
  └── AuthContent (expect/actual — platform-specific)
        ├── jvmMain:     TextField + Button  (user pastes token)
        └── androidMain: QR scanner via CameraX + ML Kit

User submits token
  │
  ▼
AuthViewModel.saveToken(token)
  ├── TokenRepository.saveToken(token)   [IO dispatcher]
  └── emits navigationEvent (SharedFlow)
        │
        ▼
AppNavGraph collects navigationEvent
  └── navigate to Routes.HOME, popUpTo(AUTH) inclusive
```

When auth is reached after an unauthorized error, `tokenInvalid = true` is passed to `AuthScreen`
so it can show a contextual message to the user.

---

### Unauthorized Redirect Flow

```
HomeViewModel.loadHome()
  └── HomeApiClient.fetchHome(token)
        └── HomeApiResult.Unauthorized
              │
              ▼
        HomeViewModel emits unauthorizedEvent (SharedFlow)
              │
              ▼
        AppNavGraph collects unauthorizedEvent
          ├── tokenInvalid = true
          └── navigate to Routes.AUTH, popUpTo(HOME) inclusive
```

---

### Dependency Wiring (entry points)

Both entry points follow the same pattern — they are the only place where concrete implementations are instantiated:

| Artifact | Android | Desktop |
|---|---|---|
| `TokenRepository` | `AndroidTokenRepository` (EncryptedSharedPreferences) | `DesktopTokenRepository` (`~/.pikesta/token`) |
| `HomeApiClient` | `KtorHomeApiClient(BuildConfig.BASE_URL)` | `KtorHomeApiClient(BuildConfig.BASE_URL)` |
| `CoroutineScope` | `lifecycleScope` | `MainScope()` (via `remember`) |

---

## Open Decisions

The following are **not yet decided** and will be documented here once resolved:

- ~~Navigation library~~ → **`org.jetbrains.androidx.navigation:navigation-compose`** (multiplatform port of Compose Navigation)
- HTTP client library (Ktor assumed, not yet implemented)
- Dependency injection approach (manual wiring vs. a DI framework)
- Offline/caching strategy
