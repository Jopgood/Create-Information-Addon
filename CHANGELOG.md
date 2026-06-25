# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.9.1] - 2026-06-25
### Fixed
- **Jetpacks and the hand drill no longer read as empty** on current Create: Stuff 'N Additions builds. CS&A 2.1.4 moved fuel/water out of item NBT (`tagFuel`/`tagWater`) and into a NeoForge fluid handler (fuel = lava, water = water), so the overlay drew every jetpack, exoskeleton and portable drill as empty regardless of their actual level ([#19](https://github.com/Jopgood/Create-Information-Addon/issues/19)). The overlay now reads the fluid handler directly and scales to each tank's reported capacity, while still falling back to the old NBT for pre-2.1.4 CS&A.

### Changed
- Bumped the bundled Create: Stuff 'N Additions test dependency to 2.1.4.a.

## [1.9.0] - 2026-06-04
### Added
- **Position the detailed overlay, not just the sprite**: the in-game overlay editor (keybind `O`) now previews and positions whichever view you are using. In detailed (text) mode it shows the real tooltip while you place it, instead of only ever previewing the simplified sprite. The sprite-only scale slider is hidden in detailed mode.

### Fixed
- **Held filling/fueling tanks now show their real level**: standalone Create: Stuff 'N Additions tanks (small/medium/large filling and fueling tanks, and the creative filling tank) store their contents differently from jetpacks, so the overlay previously drew them as empty even when full. They now read correctly in both the sprite and text overlays, scaled to each tank's (configurable) capacity. The creative tank reads as full.

### Changed
- **License metadata corrected to MIT** to match `LICENSE.txt`; the build previously still declared the template default "All Rights Reserved".

## [1.8.0] - 2026-06-03
### Added
- **Drag-to-position overlay editor**: a new keybind (default `O`) opens an in-game editor where you can drag the overlay anywhere on screen. The preview matches the live HUD exactly, and the controls hide while dragging for an unobstructed view.
- **Preset positions in the editor**: Top Left / Top Right / Bottom Left / Bottom Right buttons preview a corner before you commit, instead of applying immediately.
- **Tank scale slider**: adjust the sprite size (0.5x–5.0x) live in the editor, with a tooltip noting it stacks on top of Minecraft's GUI Scale. The new position and scale are saved together on Save and reverted on Cancel.

### Changed
- **Unified overlay layout for all positions** (chest tank left, tool tank right, item icons below). Corner positions now account for the full composite size so nothing clips at the screen edge.

## [1.7.0] - 2026-06-03
### Fixed
- **Overlay mode now persists across restarts**: toggling simplified/detailed mode (and the overlay on/off) is now saved to the config file. Previously the change only applied in-memory and reset to the default on the next launch.
- **Held non-tank items no longer appear in the simplified overlay**: previously any item in your main hand (e.g. bone meal) was drawn next to the tank. The held item is now only shown when it is itself a tank.
- **Empty tanks now display**: a worn tank with zero fuel and water no longer disappears. Tank capability is detected by item identity (known Create: Stuff 'N Additions items) as well as by stored contents, so empty/freshly-crafted tanks are still recognised.
- **Fixed water-capability detection**: `isHoldingWaterCapableItem` was mistakenly checking fuel capability instead of water.
- **Robust single/dual layout**: the overlay now picks its layout based on which slot actually holds a tank, avoiding a phantom empty tank when only a held item is a tank.

### Changed
- **Updated to Create 6.0.11**: Bumped Create `6.0.6-98` → `6.0.11-292` and refreshed the rest of the platform — NeoForge `21.1.206` → `21.1.233`, Ponder `1.0.59` → `1.0.85`, Flywheel `1.0.4` → `1.0.6`, Registrate `+62` → `+67`, JEI `19.22.1.316` → `19.27.0.340`
- **Create dependency now uses the full jar**: Create stopped publishing the `:slim` classifier at 6.0.11, so the build depends on the full jar (still `transitive = false`). No change to the shipped mod.
- **Ponder Maven coordinate change**: Create 6.0.10+ requires Ponder `1.0.82+`, which is published under the new `net.createmod.ponder:ponder-neoforge` artifact (the old `Ponder-NeoForge-1.21.1` artifact stopped at `1.0.69`). Updated the dependency accordingly.

## [1.6.1] - 2026-06-03
### Fixed
- **Config Crash**: Prevent the "trying to get config values before these are loaded to memory" crash by guarding all config reads and writes behind a load check, falling back to default values until the config is loaded (reported on CurseForge)

## [1.6.0] - 2025-09-08
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

## [1.5.0] - 2025-09-01

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

[Unreleased]: https://github.com/Jopgood/Create-Information-Addon/compare/v1.9.0...HEAD
[1.9.0]: https://github.com/Jopgood/Create-Information-Addon/compare/v1.8.0...v1.9.0
[1.8.0]: https://github.com/Jopgood/Create-Information-Addon/compare/v1.7.0...v1.8.0
[1.7.0]: https://github.com/Jopgood/Create-Information-Addon/compare/v1.6.1...v1.7.0
[1.6.1]: https://github.com/Jopgood/Create-Information-Addon/compare/v1.6.0...v1.6.1
[1.6.0]: https://github.com/Jopgood/Create-Information-Addon/compare/v1.5.0...v1.6.0
[1.5.0]: https://github.com/Jopgood/Create-Information-Addon/releases/tag/v1.5.0
