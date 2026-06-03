# CLAUDE.md — Create: Fuel & Water Information (`cfwinfo`)

Project context for AI assistants. Keep this file concise and current.

## What this is

A **Create addon** for Minecraft that renders **fuel and water tank levels on the HUD**,
so the player doesn't have to open inventories to check. It reads tank contents from
**Create Stuff 'N Additions** items (jetpacks, hand drill, etc.). Published on CurseForge
as "Create: Fuel & Water Information". Author: Jopgood. Artist: WindValley.

There are two display modes:
- **Simplified / sprite mode** — graphical tank sprites (`TankSpriteOverlay`).
- **Text / tooltip mode** — textual readout (`TankTooltipOverlay`).

## The one constraint that governs everything

**This is an addon, so it can only target Minecraft versions that Create itself ships
for.** It compiles and runs against Create's (and Create Stuff 'N Additions') API.

As of mid-2026, **Create's latest release is 6.0.10 for Minecraft 1.21.1**, and Create
only supports **1.21.1, 1.20.1, 1.19.2, 1.18.2, 1.18.1+** — nothing between 1.21.1 and the
new calendar-versioned 26.x line. **Therefore `cfwinfo` cannot target 1.21.2+, 26.1, or
Java 25 until Create does.** When users request "newer versions", the only real options are
staying current on 1.21.1 or backporting to 1.20.1 (see Roadmap). Do not attempt to bump
`minecraft_version` past what Create supports.

## Stack (current, pinned)

| | |
|---|---|
| Loader | **NeoForge** |
| Minecraft | **1.21.1** |
| NeoForge | `21.1.206` (check projects.neoforged.net for newer 21.1.x) |
| Java | **21** (toolchain pinned in `build.gradle`) |
| Build plugin | ModDevGradle `net.neoforged.moddev` `2.0.107` |
| Gradle | `8.14.3` (wrapper) |
| Mappings | Parchment `1.21.1` / `2024.11.17` |
| Mod id / version | `cfwinfo` / `1.6.0` |
| Group | `com.jopgood.cfwinfo` |
| License | MIT (per `LICENSE.txt`) — note `gradle.properties` `mod_license` still has the template default "All Rights Reserved"; reconcile. |

### Dependencies (`gradle.properties`)

- `create_version = 6.0.11-292` — from `maven.createmod.net`, full jar, `transitive = false` (the `:slim` classifier was dropped by Create at 6.0.11)
- `ponder_version = 1.0.85+mc1.21.1` — **note the artifact moved**: use `net.createmod.ponder:ponder-neoforge` (MC encoded in version), NOT the old `Ponder-NeoForge-1.21.1` (which stopped at 1.0.69). Create 6.0.10+ requires Ponder 1.0.82+.
- `flywheel_version = 1.0.6` — API `compileOnly` + `neoforge` `runtimeOnly`
- `registrate_version = MC1.21-1.3.0+67` — from `maven.ithundxr.dev/snapshots`
- `jei_version = 19.27.0.340` — `compileOnly` API + `localRuntime` full
- **Create Stuff 'N Additions** — `curse.maven:create-stuff-additions-466792:6448012` (Curse Maven; bump by replacing the trailing file id)

Repositories: `maven.createmod.net`, `maven.ithundxr.dev/snapshots`, `cursemaven.com`,
`maven.blamejared.com` (JEI), `modmaven.dev` (fallback).

## Codebase map (`src/main/java/com/jopgood/cfwinfo/`)

- `CfwInfo.java` — main `@Mod` class (common entry).
- `CfwInfoClient.java` — client entry.
- `client/ClientSetup.java` — client-side setup / overlay & layer registration.
- `client/KeyBinding.java` — keybind(s).
- `client/gui/TankSpriteOverlay.java` — **simplified sprite HUD**, a `LayeredDraw.Layer`.
  Sprite sheet `textures/gui/sprites/tank_sprite_sheet.png`, 18 frames, frame 15×32; row 0
  = fuel, row 1 = water, row 2 = tank outline; `MAX_LEVEL = 1600`. Handles single vs dual
  (chest + held) tank layouts and 4 corner positions + centre default.
- `client/gui/TankTooltipOverlay.java` — text/tooltip readout overlay.
- `client/gui/TankGuiHelper.java` — shared GUI helpers; `canDisplayTankInfo()` gates rendering.
- `client/gui/PositionedTooltipRenderer.java` — tooltip positioning.
- `common/config/CommonConfig.java` — config. Known accessors: `isInfoEnabled()`,
  `isSimplifiedEnabled()`, `getOverlayPosition()` (enum `OverlayPosition`:
  TOP_LEFT/TOP_RIGHT/BOTTOM_LEFT/BOTTOM_RIGHT + centre default), `getOverlayOpacity()` (0–100),
  `getSpriteScaleFactor()`.
- `common/data/TankDataManager.java` — reads tank contents from `ItemStack`s:
  `getFuelLevel()`, `getWaterLevel()`, `isHoldingFuelCapableItem()`, `isHoldingWaterCapableItem()`.

**Overlay render preconditions** (from `TankSpriteOverlay`): renders only when
`isInfoEnabled() && isSimplifiedEnabled() && TankGuiHelper.canDisplayTankInfo()` (i.e.
player is wearing a tank). In simplified mode, Create goggles are NOT required. If you edit
overlay code and see nothing in-game, check these three conditions first.

## Dev environment (macOS / Apple Silicon)

Project lives at `~/Documents/Development/Minecraft Modding/Create-Information-Addon`.

- **JDK:** Temurin 21 (arm64) via SDKMAN. Must be aarch64, not Intel/Rosetta.
- **IDE:** IntelliJ IDEA Community + **Minecraft Development** plugin.
- ModDevGradle generates run configs: **Client**, **Data**, **GameTestServer**, **Server**
  (Application type, main `net.neoforged.devlaunch.Main`, args via generated
  `build/moddev/clientRunProgramArgs.txt`).
- The Apple Silicon "liblwjgl.dylib" crash does **not** apply on 1.21.1 — native arm64
  LWJGL ships and ModDevGradle pulls it. `runClient` runs natively, no workaround.

### Hot-reload loop

- Launch **Client via Debug** (not Run) — HotSwap only works in a debug session.
- Settings → Build, Execution, Deployment → Debugger → HotSwap →
  **"Reload classes after compilation" = Always**.
- Optional for near-automatic reload: Compiler → "Build project automatically" +
  Advanced Settings → "Allow auto-make to start even if developed application is currently running".
- Stock HotSwap swaps **method bodies only** (no add/remove methods/fields, no signature
  changes). Enhanced redefinition (JBR/DCEVM) was considered and **deliberately skipped** as
  not worth the toolchain friction — restart for structural changes instead.
- Assets need no rebuild: **F3+T** reloads client resources (textures/models/lang);
  **`/reload`** reloads datapack content.
- Good first hotswap smoke test: change the `RenderSystem.setShaderColor(...)` literal in
  `TankSpriteOverlay.renderTankSprites` to tint the overlay (requires standing in-world
  wearing a fueled Create tank with simplified mode on).

### Quick Play (skip menus, boot into a world) — works in Run AND Debug

In `build.gradle`, in the `neoForge { runs { client { … } } }` block:

```groovy
client {
    client()
    programArgument '--quickPlaySingleplayer'
    programArgument 'World Folder Name'   // exact folder name under run/saves/
    systemProperty 'neoforge.enabledGameTestNamespaces', project.mod_id
}
```

The world must already exist in `run/saves/`. Use the folder name, not the display name.
(Use `--quickPlayMultiplayer "host:port"` for a server.) Prefer editing `build.gradle` over
the IntelliJ run-config dialog — the generated config can be overwritten on Gradle sync.

## Roadmap / how to update

1. **Refresh on 1.21.1 (do this first).** Bump `create_version` to the latest
   `maven.createmod.net` build of 6.0.10, latest NeoForge `21.1.x`, latest JEI / Ponder /
   Flywheel / Registrate / Parchment, and the Create Stuff 'N Additions Curse file id.
   `./gradlew --refresh-dependencies build`, let the compiler surface API breaks, fix, test.
    - Watch Create API changes: `ProcessingRecipe` was reworked in Create 6.0.6 (already on
      `6.0.6-98`); check for further addon-relevant changes through 6.0.10.
    - Most version-sensitive code: HUD/overlay rendering (NeoForge GUI layering shifted
      across 1.21.x) and how tank contents are read off Create Stuff 'N Additions items.
2. **Optional 1.20.1 backport.** Separate `mc/1.20.1` branch. 1.20.1 Create is Forge (NeoForge
   on newer 6.x), uses **Java 17**, and APIs differ. Verify all deps exist on 1.20.1 first.
3. **Future MC versions** are blocked on Create — do not chase 26.1 until Create ports.

## Conventions & gotchas

- `org.gradle.jvmargs=-Xmx1G` is low for decompile/dev-client; `-Xmx4G` is comfortable on
  this Mac.
- `org.gradle.configuration-cache=true` is on; if it misbehaves after a version/plugin bump,
  run with `--no-configuration-cache` or delete `.gradle/configuration-cache`.
- On a fresh clone: `chmod +x gradlew`.
- Create dependency uses `transitive = false` (we declare Ponder/Flywheel/Registrate explicitly) — don't "fix" that. Create published a `:slim` classifier through 6.0.10 but **dropped it at 6.0.11**, so from 6.0.11+ we depend on the full jar (no `:slim`).
- Build output jar: `build/libs/cfwinfo-<version>.jar`. Bump `mod_version` before building.
- There is a GitHub Actions workflow under `.github/workflows` and release notes in
  `docs/releases`.