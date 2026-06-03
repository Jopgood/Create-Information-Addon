# Releasing `cfwinfo`

This repo automates everything mechanical about a release. You stay in charge of
the *narrative* (changelog + release notes); CI handles building, the GitHub
Release, and uploading to CurseForge and Modrinth.

## The flow

1. **Branch off `main`** and do the work as usual.
2. In the **release PR**, before merging:
   - Bump `mod_version` in [`gradle.properties`](gradle.properties).
   - Add a `## [X.Y.Z] - YYYY-MM-DD` section to [`CHANGELOG.md`](CHANGELOG.md).
   - Add the user-facing notes file `docs/releases/vX.Y.Z.md` (this becomes the
     GitHub Release / CurseForge / Modrinth changelog body verbatim, so write it
     for players).
   - The **`release-readiness`** CI job fails the PR if either of those is missing
     for the new `mod_version`.
3. **Merge to `main`** once CI is green.
4. Locally: `git checkout main && git pull`.
5. **Tag and push** — this is the only manual release step:
   ```bash
   git tag v1.9.0
   git push --tags
   ```
6. The **Release** workflow takes over: it verifies the tag matches `mod_version`,
   builds the jar, creates the GitHub Release (body = `docs/releases/v1.9.0.md`,
   jar attached), and publishes to CurseForge + Modrinth. No manual CurseForge
   upload needed.

> The tag must match `mod_version` (e.g. tag `v1.9.0` ⇄ `mod_version=1.9.0`),
> or the workflow fails fast.

## One-time setup

Add these under **Settings → Secrets and variables → Actions**:

| Kind | Name | Value |
|---|---|---|
| Secret | `CF_API_TOKEN` | CurseForge API token from <https://legacy.curseforge.com/account/api-tokens> |
| Secret | `MODRINTH_TOKEN` | Modrinth PAT (scope: *Create versions*) from <https://modrinth.com/settings/pats> — only once you have a Modrinth project |
| Variable | `MODRINTH_PROJECT_ID` | Your Modrinth project id/slug |

`GITHUB_TOKEN` is provided automatically — no setup.

Until `MODRINTH_TOKEN` / `MODRINTH_PROJECT_ID` are set, the Modrinth step is
**skipped automatically** (mc-publish skips any platform with missing
credentials); CurseForge and the GitHub Release still run.

The CurseForge project id (`1023209`), supported loader (`neoforge`), game
version (`1.21.1`), and dependency relations are set in
[`.github/workflows/release.yml`](.github/workflows/release.yml). Update them
there when the platform target changes (e.g. a Create/Minecraft version bump).
