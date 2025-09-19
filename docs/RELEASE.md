# VoxCompose Release Guide

This document explains how to cut a tagged release that produces a fat JAR artifact and (optionally) a Homebrew formula update.

Scope
- Output: voxcompose-cli-all.jar attached to the GitHub Release
- Trigger: pushing a tag matching v*
- CI: .github/workflows/release.yml

Prerequisites
- PRs merged to main (no merge commits)
- CI green on main (Integration Tests + Java CLI)

Steps
1) Decide version bump (semver)
- First CLI release: v0.4.0
- Pre-release candidates: v0.4.0-rc.1, etc.

2) Tag and push
- Ensure main is up-to-date locally
- Tag and push (example):

```bash
# from repo root on main
NEW_TAG=v0.4.0
git tag -a "$NEW_TAG" -m "VoxCompose CLI $NEW_TAG"
git push origin "$NEW_TAG"
```

3) CI builds and publishes
- Release workflow runs:
  - Builds fat JAR: cli-java/build/libs/voxcompose-cli-all.jar
  - Attaches artifact to the GitHub Release for the tag

4) (Optional) Homebrew formula update
- After the release is published, compute sha256 and update your tap formula:

```bash
ASSET_URL="https://github.com/cliffmin/voxcompose/releases/download/$NEW_TAG/voxcompose-cli-all.jar"
SHA256=$(curl -L "$ASSET_URL" | shasum -a 256 | awk '{print $1}')
# Update your tap formula with url and sha256
```

Notes
- No root-level build files are introduced; all build artifacts and gradle config live under cli-java/
- The fat JAR contains all dependencies and can be run with:

```bash
echo "text" | java -jar voxcompose-cli-all.jar --stats
```
