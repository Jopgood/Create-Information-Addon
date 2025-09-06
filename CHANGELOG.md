# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.5.0] - 2025-01-XX

### Added
- Universal tank detection for any fuel/water capable wearable items (no hardcoded lists)
- Sprite scale factor configuration option (0.5x to 5.0x scaling)
- Overlay opacity control (0-100% transparency)
- Overlay position selection (5 positions: corners + center)
- Notification messages toggle for setting changes
- Enhanced configuration comments and descriptions

### Changed
- **BREAKING**: Migrated from Forge to NeoForge platform
- **BREAKING**: Requires Minecraft 1.21+ and NeoForge
- Updated from legacy NBT system to Data Components
- Renamed classes and methods from "Jetpack" to "Tank" terminology
- Updated mod description to reflect broader tank support
- Improved sprite rendering system using pose stack scaling

### Fixed
- **CRITICAL**: Server crash when mod accidentally installed on dedicated servers (Issue #1)
- Sprite duplication issue when using scale factors > 1.0
- Memory usage optimization and performance improvements
- Server compatibility issues with sprite overlays
- Configuration synchronization problems

### Technical
- Refactored codebase for better maintainability
- Implemented generic helper methods for tank detection
- Updated to modern Minecraft data handling systems
- Improved error handling and debugging capabilities

---

## [1.4.0] and earlier - Forge Era
Previous versions were built for Minecraft Forge. See individual release files in `docs/releases/` for detailed information about older versions.

---

**Note**: For detailed release notes with user-friendly descriptions, see the corresponding files in `docs/releases/`.