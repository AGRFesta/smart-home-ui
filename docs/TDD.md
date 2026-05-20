# TDD Methodology (Test-Driven Development)

This project strictly follows TDD. You are not allowed to write tests and implementation at the same time. You must follow the incremental cycle (Phase 0 to Phase 3) respecting the established barriers.

**GOLDEN RULE:** Never proceed to the next phase without explicit user approval. To save execution resources and avoid environment issues, **never run tests automatically**. Always ask the user to run them or ask for explicit permission to execute them yourself.

## Test Writing Guidelines & Conventions

When generating or modifying tests, you must adhere to the following rules:

* **Behavior over Implementation:** Test the actual behavior and output of the component, not its internal implementation details. For Composables, test what the user sees and what happens when they interact — never assert on internal state variables or private lambdas.
* **Mocking Boundaries:** Use mocks and stubs ONLY for system boundaries (e.g., HTTP API clients, Platform APIs such as camera/filesystem/permissions, navigation controllers). **Never** mock ViewModel logic, pure functions, or domain models. Rely on Dependency Injection.
* **Test Organization:** Prefer one test class per method/behavior under test (e.g., `HomeViewModelLoadDevicesTest.kt`) ONLY when the methods have significantly different setup, or when the class grows beyond ~400 lines. If the shared setup dominates, keep tests together or extract a base class.
* **Visual Structure (Arrange-Act-Assert):** The body of every test MUST be visually separated into three distinct blocks using `// Given`, `// When`, `// Then` comments to clearly separate data setup, execution, and verification.
* **Explicit "Given" Phase:**
    * **Subject setup — always explicit:** Declare any value, mock behavior, or state that is the **primary subject** of the test directly in the test body, even if the same value is already provided as a default by an Object Mother, `init` block, or `@BeforeTest`. The subject must be visible at a glance.
    * **Non-subject setup — centralize, don't repeat:** Setup that is shared across multiple tests and is **not the subject** of any of them must be extracted to a single point (`@BeforeTest` or an `init` block). Repeating it in every test body is noise that obscures what each test is actually about.

> ❗ **Given phase checklist — apply at every Phase 3 (Refactor):**
> 1. Does any `@BeforeTest` / `init` block contain setup that IS the subject of at least one test? → move it into that test body.
> 2. Does any test body repeat setup that is shared and NOT the subject of any test? → extract it to `@BeforeTest` / `init`.
> 3. Is the SUT construction repeated in every test? → move it to a field, unless constructor args vary per test.
> 4. Could a reader understand *what* this test is about from the Given block alone, without reading the rest of the class? → if not, something is in the wrong place.

* **Descriptive Naming:** Test names must clearly state the behavior being verified and the context. Use backtick function names for readability (e.g., `` fun `should emit error state when API call fails`() ``).
* **Assertions on sealed UI state:** When asserting on a sealed `UiState` hierarchy, **never** just check the subtype. Assert the exact content of the state using `assertIs<>()` combined with property checks, so a wrong value fails as clearly as a wrong type:
    ```kotlin
    val state = assertIs<HomeUiState.Success>(viewModel.uiState.value)
    assertEquals(expectedDevices, state.devices)
    ```
* **Collections and complex objects:** Prefer `assertEquals` with a descriptive message argument over bare assertions when the diff output would be ambiguous.

---

## Test Infrastructure

### Test Levels

The project has three distinct test layers. Use the right one for the right scope:

| Level | Source set / Tool | Scope | When to use |
|---|---|---|---|
| **Unit** | `commonTest` — `kotlin.test` + MockK | Single class in isolation | ViewModels, domain models, mappers, pure functions |
| **Compose UI** | `commonTest` or `jvmTest` — `compose-ui-test` | Composable rendering and user interactions | Individual composables: correct rendering, user interaction triggers expected callback |
| **Integration** | `jvmTest` / `androidHostTest` — `compose-ui-test` + real ViewModel | Full screen flow | Happy path of a complete screen, navigation, API client stubbed at boundary |

**Rule of thumb for test placement:**
- Logic with no Compose dependency → `commonTest`
- Composable rendering that does not need a real device → `jvmTest`
- Android-specific behavior (permissions, intents) → `androidHostTest`

### Test Frameworks & Tools

- **Test runner:** [`kotlin.test`](https://kotlinlang.org/api/latest/kotlin.test/) — `@Test`, `@BeforeTest`, `@AfterTest`. Works in all source sets.
- **Assertions:** [Kotest assertions](https://kotest.io/docs/assertions/assertions.html) (`kotest-assertions-core`) — `shouldBe`, `shouldBeInstanceOf<>`, `withClue { }`. Multiplatform-compatible; used as assertion library only, not as test runner.
- **Mocking:** [MockK](https://mockk.io/) — `mockk<>()`, `every { }`, `verify { }`. Works in JVM-based source sets (`commonTest`, `jvmTest`, `androidHostTest` for this project).
- **Compose UI testing:** [`compose-ui-test`](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-test.html) — `ComposeUiTest`, `setContent { }`, `onNodeWithText`, `onNodeWithTag`, `performClick`, `assertIsDisplayed`.
- **Coroutines testing:** [`kotlinx-coroutines-test`](https://github.com/Kotlin/kotlinx.coroutines/tree/master/kotlinx-coroutines-test) — `TestScope`, `StandardTestDispatcher`, `advanceUntilIdle()`. Required for any test involving `StateFlow`, `suspend` functions, or time-based logic.

### ViewModel Testing Pattern

ViewModels must accept an injected `CoroutineScope` or `CoroutineDispatcher` to be testable. The standard test setup is:

```kotlin
private val testDispatcher = StandardTestDispatcher()
private val testScope = TestScope(testDispatcher)

@BeforeTest
fun setUp() {
    Dispatchers.setMain(testDispatcher)
}

@AfterTest
fun tearDown() {
    Dispatchers.resetMain()
}

@Test
fun `should emit success state when devices load correctly`() {
    // Given
    val fakeClient = mockk<DeviceApiClient>()
    every { fakeClient.getDevices() } returns listOf(fakeDevice)
    val viewModel = HomeViewModel(fakeClient, testScope)

    // When
    viewModel.loadDevices()
    testScope.advanceUntilIdle()

    // Then
    val state = assertIs<HomeUiState.Success>(viewModel.uiState.value)
    state.devices shouldBe listOf(fakeDevice)
}
```

### Compose UI Test Pattern

```kotlin
@Test
fun `should show device name after loading`() = runComposeUiTest {
    // Given
    val fakeViewModel = FakeHomeViewModel(initialState = HomeUiState.Success(listOf(fakeDevice)))

    // When
    setContent { HomeScreen(viewModel = fakeViewModel) }

    // Then
    onNodeWithText(fakeDevice.name).assertIsDisplayed()
}
```

Use `onNodeWithTag` (via `Modifier.testTag(...)`) when a node cannot be identified by visible text alone.

---

## Phase 0: PLANNING (The Test List)

1. Before writing any code, analyze the task and create a **strictly ordered bulleted list** of test cases you plan to write.
2. Order the list logically: start with the absolute simplest degenerate case (e.g., null/empty inputs, initial state, validation failures), then progress to base success cases, and finally complex edge cases or API failure scenarios.
3. For each test case, note in parentheses which test level it belongs to: `(Unit)`, `(Compose UI)`, or `(Integration)`.
4. **BARRIER - STOP AND ASK:** Present this list to the user and ask for approval. **Do not write any code** until the list is approved or amended.

## Phase 1: RED (Writing ONE Single Test)

1. Pick ONLY the **first uncompleted test** from the Phase 0 list.
2. Write the code for this **SINGLE test only**. Do not write tests for the other items on the list yet, strictly following the **Test Writing Guidelines** above. Do not touch production code beyond the bare minimum required to make the test compile. If you use Kotlin's `TODO()`, **always provide a descriptive message** (e.g., `TODO("Implement device loading from API")`) so the test fails with a specific `NotImplementedError`, confirming the correct execution path was hit.
3. **BARRIER - STOP AND ASK:** Ask the user: *"I have written the test for the first case. Could you please run it locally to verify it fails (RED) for the expected reason, or do you grant me permission to run it?"*
4. **Do not proceed** until the user confirms the single test is RED in the right way.

## Phase 2: GREEN (Minimal Implementation)

1. Once approved, write the production code to make **only that specific test pass**.
2. **Constraint:** Write *only* the simplest, minimal code necessary. Do not optimize, do not abstract, do not anticipate future test cases from your Phase 0 list.
   - Do **not** add `if/when` branches that are not exercised by the current test.
   - Do **not** implement multiple `UiState` transitions if the current test only verifies one — use a hardcoded return or `TODO()` for untested branches.
   - **Diagnostic check:** If the *next* test on the Phase 0 list is already GREEN before you write it, you over-implemented. Stop, revert the excess, and re-introduce it only when its test demands it.
3. **BARRIER - STOP AND ASK:** Ask the user: *"I have written the minimal implementation. Could you please run the tests to verify they pass (GREEN)?"*
4. **Do not proceed** until the user confirms the tests are GREEN.

## Phase 3: REFACTOR & LOOP (Cleanup and Next Steps)

1. Once GREEN, analyze both the newly written production code **and** the test code. Refactor to eliminate duplication and ensure compliance with the project architecture.
2. **Constraint (No Behavior Change):** During refactoring, you are **strictly forbidden** from adding new business logic, new UI states, or new conditional branches. You can only restructure existing code to improve readability and remove duplication.
3. Apply the **Given phase checklist** above to the test code.
4. Remind the user to run the test suite after modifications.
5. **LOOP:** Once refactoring is approved, explicitly **cross off the completed test** from the Phase 0 list, announce the next test on the list, and loop back to **Phase 1** for that specific test.
