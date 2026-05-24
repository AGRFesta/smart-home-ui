# Screenshot Testing

This project uses [Roborazzi](https://github.com/takahirom/roborazzi) for automated Android
screenshot testing. Tests run on the JVM via Robolectric — no emulator or device required.

## How It Works

1. **Record** — run the record task to generate reference PNG screenshots and commit them.
2. **Verify** — run the verify task (or let CI run it) to compare the current render against the
   baseline; the task fails if pixels differ beyond the threshold.
3. **Update intentionally** — when a UI change is deliberate, re-record, review the diff, and
   commit the updated baselines together with the code change.

## Running Locally

```bash
# Verify current screenshots match committed baselines
./gradlew :androidApp:verifyRoborazziDebug

# Record new baselines after intentional UI changes
./gradlew :androidApp:recordRoborazziDebug
```

On Windows use `gradlew.bat` instead of `./gradlew`.

## Baseline Images

Baseline PNG files live under:

```
androidApp/src/test/snapshots/
```

Always commit baseline images together with the code change they correspond to.

## CI

`verifyRoborazziDebug` runs automatically on every push to `main` and on every pull request
targeting `main` (see `.github/workflows/screenshot-tests.yml`). A screenshot mismatch fails
the build.

## Covered Screens

| Test class | Tests |
|---|---|
| `HomeContentScreenshotTest` | `homeContent_loading`, `homeContent_error`, `homeContent_success` |
| `AuthContentScreenshotTest` | `authContent_default`, `authContent_tokenInvalid`, `qrAuthContent` |

The `HomeContent` tests in particular verify that the version footer remains visible above the
Android system navigation bar (the `navigationBarsPadding()` inset is applied in `HomeContent`).

## Updating Baselines for Intentional UI Changes

1. Make the UI change in code.
2. Run `./gradlew :androidApp:recordRoborazziDebug` to regenerate the PNGs.
3. Review the diffs with `git diff --stat` or an image diff tool.
4. Commit the updated baseline images together with the code change.
