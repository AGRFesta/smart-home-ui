# Smart Home UI

Kotlin Multiplatform application targeting Android and Desktop (JVM) using Compose Multiplatform.

## Development Workflow

This project follows strict TDD. **Read `docs/TDD.md` before writing any test or production code.** It defines the mandatory Red→Green→Refactor cycle, the phase barriers, and all test writing conventions.

**Read `docs/ARCHITECTURE.md` before adding new code.** It defines module boundaries, the screen anatomy (ViewModel + UiState + Composable), naming conventions, and system boundaries.

**Read `docs/SCREENSHOT_TESTING.md` before modifying Android UI composables.** It explains how to record new baselines and run screenshot verification locally.

## Module Structure

```
shared/          — all shared logic and UI (Compose Multiplatform)
  commonMain     — production code shared across all platforms
  commonTest     — tests runnable on all platforms (pure logic, Compose UI)
  androidMain    — Android-specific platform implementations
  jvmMain        — Desktop JVM-specific platform implementations
  jvmTest        — Desktop-only tests (e.g., Compose UI tests via compose-ui-test)
  androidHostTest — Android tests running on the host JVM
androidApp/      — Android application entry point (thin shell, delegates to shared)
desktopApp/      — Desktop JVM entry point (thin shell, delegates to shared)
```

New features go in `shared/commonMain`. Platform-specific code only when unavoidable.

## Key Technologies

| Library | Version | Purpose |
|---|---|---|
| Kotlin | 2.3.21 | Language |
| Compose Multiplatform | 1.11.0 | UI framework |
| Material3 | 1.11.0-alpha07 | Design system |
| AndroidX Lifecycle | 2.11.0-beta01 | ViewModel, `collectAsStateWithLifecycle` |
| kotlinx-coroutines | 1.11.0 | Async, StateFlow |
| JVM target | 21 | Compilation target |
| Android minSdk | 24 | Minimum Android version |

## Common Commands

```bash
# Run desktop app
./gradlew :desktopApp:run

# Run all tests
./gradlew :shared:allTests

# Run JVM tests only (faster, no Android toolchain)
./gradlew :shared:jvmTest

# Run Android host tests
./gradlew :shared:testDebugUnitTest

# Verify Android screenshot baselines
./gradlew :androidApp:verifyRoborazziDebug

# Record new screenshot baselines (after intentional UI changes)
./gradlew :androidApp:recordRoborazziDebug

# Build everything
./gradlew build
```

On Windows use `gradlew.bat` instead of `./gradlew`.

## Architecture

The project follows a ViewModel-driven architecture within Compose Multiplatform:

- **ViewModel** (`commonMain`) — holds `StateFlow<UiState>`, exposes events, calls API clients
- **UiState** — sealed class per screen (`Loading`, `Success`, `Error`)
- **Composable screens** — observe `uiState`, delegate events to ViewModel
- **API clients** — interfaces injected into ViewModels; implementations call the Smart Home backend

Composables must be stateless where possible: receive state, emit events, no business logic.

## Test Stack

| Tool | Purpose |
|---|---|
| `kotlin.test` | Test runner (`@Test`, `@BeforeTest`, `@AfterTest`) |
| Kotest assertions | `shouldBe`, `shouldBeInstanceOf<>`, `withClue { }` |
| MockK | Mocking at system boundaries |
| `compose-ui-test` | Composable rendering and interaction |
| `kotlinx-coroutines-test` | `TestScope`, `StandardTestDispatcher` for ViewModel tests |
