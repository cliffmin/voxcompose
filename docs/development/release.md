# VoxCompose Release Guide

This document explains how to cut a tagged release that produces a fat JAR artifact.

## Scope

- **Output**: `voxcompose-<version>-all.jar` attached to the GitHub Release
- **Trigger**: Pushing a tag matching `v*`
- **CI**: `.github/workflows/release.yml`

## Prerequisites

- All PRs merged to `main` (no pending work)
- CI green on `main` (Build and Test + Integration Tests)
- Local `main` branch up-to-date

## Release Steps

### 1. Decide version bump (semver)

Follow semantic versioning:
- **Major** (1.0.0): Breaking changes
- **Minor** (0.2.0): New features, backwards compatible
- **Patch** (0.1.1): Bug fixes, backwards compatible
- **Pre-release**: (0.2.0-rc.1): Release candidates

### 2. Update version in build.gradle.kts

Edit `build.gradle.kts` and update the version:

```kotlin
version = "0.2.0"  // Update this
```

### 3. Update CHANGELOG.md

Add release notes under a new version heading:

```markdown
## [0.2.0] - 2025-10-14

### Added
- New feature X
- New feature Y

### Fixed
- Bug Z
```

### 4. Commit version bump

```bash
git add build.gradle.kts CHANGELOG.md
git commit -m "chore(release): bump version to 0.2.0"
git push origin main
```

### 5. Tag and push

```bash
NEW_TAG=v0.2.0
git tag -a "$NEW_TAG" -m "VoxCompose $NEW_TAG"
git push origin "$NEW_TAG"
```

### 6. CI builds and publishes

The release workflow will:
1. Build the fat JAR with all dependencies
2. Calculate SHA256 checksum
3. Create GitHub Release
4. Attach JAR artifact with checksum in release notes

### 7. Verify release

- Check [GitHub Releases](https://github.com/cliffmin/voxcompose/releases)
- Download JAR and verify:
  ```bash
  java -jar voxcompose-0.2.0-all.jar --version
  ```

## Troubleshooting

### Build fails in CI
- Check Java version (must be 21)
- Verify all tests pass locally: `./gradlew clean test`

### JAR not attached
- Check workflow run logs in GitHub Actions
- Verify `fatJar` task completes successfully

### Version mismatch
- Ensure `build.gradle.kts` version matches git tag (minus the `v` prefix)
