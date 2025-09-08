# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.6.0] - 2024-12-08
### Added
- **Handheld Tank Display**: Show tank information for tools held in your main hand (#4)
- **Dual Tank View**: Display both chest armor and handheld item tanks simultaneously
- **Smart Positioning**: Automatic layout adjustment to prevent screen clipping in all overlay positions
- **Enhanced Tooltips**: Separate sections for chest and handheld items with improved visual hierarchy
- **Position-Aware Rendering**: Tool tank renders on left for RIGHT positions, items render above tanks for BOTTOM positions
- **Configurable Padding**: 16-pixel padding for right-side positions to prevent clipping
- **New TankDataManager Methods**: Added `isHoldingFuelCapableItem()` and `isHoldingWaterCapableItem()` for handheld item detection

### Changed
- **Tooltip System**: Enhanced with multi-item support and color-coded sections
- **Overlay Positioning**: Removed CENTER position option, focusing on corner positions for better UX
- **Default Opacity**: Increased from 75% to 80% for better visibility
- **Default Position**: Changed from CENTER to TOP_LEFT for consistency
- **Sprite Rendering**: Improved dual tank rendering system with independent fuel/water level calculations

### Improved
- **Visual Layout**: Better spacing and organization in tooltip display
- **Performance**: Optimized rendering pipeline for dual tank displays
- **Code Organization**: Refactored rendering logic into separate methods for maintainability
- **User Experience**: Smart detection prevents unnecessary HUD elements when items aren't tank-capable

### Technical
- **Backward Compatibility**: All existing chest-only functionality maintained
- **Scale Awareness**: Padding calculations respect sprite scale factor
- **Memory Efficiency**: Optimized sprite rendering with reduced redundant calculations

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

[Unreleased]: https://github.com/Jopgood/Create-Information-Addon/compare/v1.6.0...HEAD
[1.6.0]: https://github.com/Jopgood/Create-Information-Addon/compare/v1.5.0...v1.6.0
[1.5.0]: https://github.com/Jopgood/Create-Information-Addon/releases/tag/v1.5.0