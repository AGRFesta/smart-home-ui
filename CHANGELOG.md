# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Area alert indicator on the home dashboard: each area card shows a red alert icon
  when at least one alert is active (with count when more than one), and a warning
  glyph when the alert state cannot be retrieved (#30).

### Changed

- Removed the hardcoded API version from the home screen footer; only the app version
  is shown.

### Fixed

- Dashboard crash when the home stream contains an area without sensors: omitted
  `heating`/`humidity` measurement keys no longer fail deserialization, leaving the
  screen stuck in Error or in an endless Reconnecting loop.
- SSE stream request now sends the `Accept: text/event-stream` header.

## [1.1.1] - 2026-06-02

### Fixed

- Recover from EncryptedSharedPreferences keyset corruption on startup (#28).

### Changed

- MainActivity dependencies built via a dedicated factory, covered by startup
  integration tests (#25).

## [1.1.0] - 2026-06-01

### Added

- Home screen data streamed via Server-Sent Events with automatic reconnection (#17).
- Custom app icons for Android and Desktop (#167).
- Roborazzi screenshot testing for Android UI (#168).

### Fixed

- QR scanner: centered square viewfinder with dark overlay (#22), instruction label
  overlaid on a scrim band for legibility (#23), permission UI centered vertically (#21).
- System bar insets handled at HomeContent column level (#18).

## [1.0.2] - 2026-05-23

### Fixed

- GitHub token permission in the release pipeline.

## [1.0.1] - 2026-05-23

### Fixed

- Release GitHub Action.

## [1.0.0] - 2026-05-23

### Added

- Home dashboard displaying global heating state and per-area measurements (#6).
- Authentication flow: QR scan on mobile, token paste form on desktop.
- App version and API version shown in the home screen footer (#8).
- Android release pipeline via GitHub Actions (#10).
