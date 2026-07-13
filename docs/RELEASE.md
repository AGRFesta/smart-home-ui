# Release Process

Checklist for preparing and publishing a release `vX.Y.Z`.

## 1. Bump the version

The version is defined in **three places** that must stay in sync:

| File | Field |
|---|---|
| `androidApp/build.gradle.kts` | `versionName = "X.Y.Z"` and `versionCode` (**increment by 1**) |
| `desktopApp/build.gradle.kts` | `packageVersion = "X.Y.Z"` |
| `shared/src/commonMain/kotlin/org/agrfesta/sh/ui/AppInfo.kt` | `APP_VERSION = "X.Y.Z"` |

## 2. Update the changelog

In `CHANGELOG.md`:

- Rename `[Unreleased]` to `[X.Y.Z] - YYYY-MM-DD` and add a fresh empty
  `## [Unreleased]` section above it.
- Update the compare links at the bottom: add the `[X.Y.Z]` link and point
  `[unreleased]` at `vX.Y.Z...HEAD`.

## 3. Re-record screenshot baselines

The home screen footer displays `APP_VERSION`, so every version bump changes the
`HomeContentScreenshotTest` baselines:

```bash
./gradlew :androidApp:recordRoborazziDebug
./gradlew :androidApp:verifyRoborazziDebug
```

Commit the updated PNGs under `androidApp/src/test/snapshots/` together with the bump.

## 4. Verify

```bash
./gradlew :shared:jvmTest
```

## 5. Commit and tag

- Commit everything with the message `bump version to vX.Y.Z`.
- Tag the release commit: `git tag vX.Y.Z`.

## 6. Publish

Push the branch and the tag:

```bash
git push origin <branch> vX.Y.Z
```

Pushing the tag triggers `.github/workflows/release-android.yml`, which builds the
signed release APK and attaches it to a GitHub Release with generated notes.

Desktop distribution is **not** automated; build packages locally with
`./gradlew :desktopApp:packageDistributionForCurrentOS` if needed.
