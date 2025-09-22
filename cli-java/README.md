# VoxCompose Java CLI (active)

This is the active Java CLI module for VoxCompose. It produces the fat JAR that ships in releases and is used by the Homebrew formula.

Goals
- Stable CLI (stdin→stdout) that applies corrections and persists learning
- Flags: --version, --duration, --data-dir, --state-dir, --learn on|off, --dry-run, --stats
- Own XDG/macOS data/state paths and migration
- Emit JSON stats to stderr when --stats is set

Plan (current)
1) Maintain Gradle project here (cli-java/) with shadow JAR build
2) Persist learning to learned_profile.json with atomic writes
3) Apply correction pipeline (capitalization, common splits) as first pass
4) Tests for flags, I/O, data-dir precedence, and learning updates
5) CI jobs (Java CLI + Release) produce the versioned fat JAR

Notes
- This module supersedes the legacy scaffold in cli/java/ (now removed).
- Release asset name matches project.version: voxcompose-cli-<version>-all.jar
