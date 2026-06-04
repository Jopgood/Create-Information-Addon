# Multi-version / backport plan

How `cfwinfo` plans to broaden version & loader coverage. **Decision of record**, so we don't
re-derive it each time.

## The binding constraint

`cfwinfo` is an addon to **Create** *and* reads **Create: Stuff 'N Additions** (CSA) items, so it
can only target a MC version where **both** ship. Verified coverage (Modrinth, 2026-06):

| MC | Create | CSA | CSA loader | Verdict |
|----|--------|-----|-----------|---------|
| 1.18.2 | ✅ Forge | ✅ (1 file) | Forge | Possible, but CSA support is minimal |
| 1.19.2 | ✅ Forge | ✅ (1 file) | Forge | Possible, but minimal |
| **1.20.1** | ✅ Forge+NeoForge | ✅ (8 files) | **Forge** | **Primary backport target** |
| **1.21.1** | ✅ NeoForge | ✅ | NeoForge | Current (`main`) |
| 1.21.2–1.21.5 | ❌ absent | ✅ | NeoForge | **Blocked — Create not published** |

Takeaways:
- **"Backport" and "Forge coverage" are the same effort** — there is no Forge on 1.21.1; Forge means
  going back to 1.20.1 and earlier.
- **1.20.1 is the highest-value target** (largest modded audience; CSA actively supported there). It
  is **Forge + Java 17**, because CSA on 1.20.1 is Forge-only.
- **Skip 1.19.2 / 1.18.2** unless asked — CSA barely supports them.
- **Never chase 1.21.2+** until Create ships there (see CLAUDE.md's governing constraint).

## Structure: branch-per-version (decided)

- `main` → 1.21.1 / NeoForge (current toolchain: ModDevGradle).
- `mc/1.20.1` → 1.20.1 / Forge / Java 17, maintained semi-independently.
- Fixes that apply to both are cherry-picked across branches.
- **Escalate to [Stonecutter](https://github.com/kikugie/stonecutter)** (single codebase, version-conditional)
  only if we end up maintaining 3+ versions and the cherry-pick tax hurts.
- **Architectury was rejected** — it suits multi-loader on one MC; our split is cross-MC-version,
  where the shareable "common" code shrinks to almost nothing.

## Versioning & tagging

- Keep the **same semantic version** across MC targets (e.g. `1.9.0` is the 1.9.0 feature set, built
  per MC). Release notes are written once.
- Encode MC in the artifact so files/tags never collide:
  - jar: `cfwinfo-1.9.0+mc1.20.1.jar`
  - tag: `v1.9.0+mc1.20.1` (the `v*` release trigger already matches; `main` keeps plain `v1.9.0`)
- **Both streams publish to the same CurseForge (1023209) and Modrinth (csA0cpEX) projects** — both
  host many files/loaders/versions under one listing.

## Releasing scales like this

- Each branch carries its own `build.yml` / `release.yml` with that target's literals
  (`loaders: forge`, `game-versions: '1.20.1'`). A tag push on the branch publishes that jar to the
  shared listings via mc-publish. The `release-readiness` gate stays per-branch.
- Canonical `CHANGELOG.md` + `docs/releases/` live on `main`; the backport branch reuses the same
  `vX.Y.Z` notes.
- (If we ever adopt Stonecutter, this collapses into a build **matrix** in one `release.yml`.)

## Required spike before porting

Do **not** start the port until these are verified — both are cheap and de-risk the whole effort:

1. **CSA 1.20.1 NBT keys.** Decompile the 1.20.1 Forge CSA jar (as was done for 1.21.1) and confirm
   the tanks store `tagFuel` / `tagWater` / `tagStock` in old-style NBT. The entire value prop depends
   on this. Note: 1.20.1 predates **Data Components** (1.20.5+), so `TankDataManager`'s
   `DataComponents.CUSTOM_DATA` / `CustomData` path must be reimplemented against `ItemStack.getTag()`.
2. **Toolchain.** Confirm the 1.20.1 Forge build path (likely ForgeGradle / legacy MDG, not the current
   NeoForge ModDevGradle), Java 17, and that Create + CSA 1.20.1 artifacts resolve.

Other known 1.20.1 deltas to budget for: HUD layering (`LayeredDraw`/`RegisterGuiLayersEvent` →
Forge `IGuiOverlay`/`RenderGuiOverlayEvent`), keybind/config registration, and Create/catnip package
differences (catnip may not exist on Create 0.5.1).

## Next action

Run the spike (item 1 + 2 above) on a throwaway branch; report findings; then scope the port.
