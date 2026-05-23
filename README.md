This is a Kotlin Multiplatform project targeting Android, Desktop (JVM).

* [/shared](./shared/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./shared/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./shared/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./shared/src/jvmMain/kotlin)
    folder is the appropriate location.

### Developer setup

Before running the app, create a `local.properties` file in the project root (already git-ignored) and add:

```
smart_home.base_url=https://<your-server>
```

This value is injected at build time into `BuildConfig.BASE_URL` on both Android and Desktop.

### Environment variables (CI/CD)

In CI environments where `local.properties` is not available, the following environment variables can be used instead:

| Variable | Description |
|---|---|
| `SMART_HOME_BASE_URL` | Base URL of the Smart Home backend (fallback for `smart_home.base_url`) |
| `KEYSTORE_PATH` | Absolute path to the release `.jks` keystore file |
| `STORE_PASSWORD` | Password for the keystore store |
| `KEY_ALIAS` | Alias of the signing key inside the keystore |
| `KEY_PASSWORD` | Password for the signing key |

`KEYSTORE_PATH` is optional for local/debug builds. If set, all four signing variables must be provided or the build will fail explicitly.

### Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these commands and options:

- Android app: `./gradlew :androidApp:assembleDebug`
- Desktop app:
  - Hot reload: `./gradlew :desktopApp:hotRun --auto`
  - Standard run: `./gradlew :desktopApp:run`

### Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- Android tests: `./gradlew :shared:testAndroidHostTest`
- Desktop tests: `./gradlew :shared:jvmTest`

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…