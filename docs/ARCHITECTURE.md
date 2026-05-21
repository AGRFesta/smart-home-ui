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

## Open Decisions

The following are **not yet decided** and will be documented here once resolved:

- Navigation library (Compose Navigation, Decompose, or other)
- HTTP client library (Ktor assumed, not yet implemented)
- Dependency injection approach (manual wiring vs. a DI framework)
- Offline/caching strategy
