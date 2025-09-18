# VoxCompose Long-Term CLI Integration Plan

> Conceptual Reference
> This plan outlines a potential future CLI. This repository is documentation-only and does not include an implementation.

Status: Draft
Author: VoxCompose maintainers
Date: 2025-09-18

Overview
- Objective: Provide an official VoxCompose CLI that applies corrections and persists learning to user data directories (XDG/macOS) and integrates cleanly with macOS PTT Dictation.
- Motivation: Current repo is resource-only; learning data is not updated during dictation. This plan reintroduces a CLI with correct data semantics.

Goals
- Real-time corrections with deterministic latency and no external dependencies
- Persistent self-learning updates to learned_profile.json at XDG_DATA_HOME (or macOS Application Support)
- Backwards-compatible invocation interface for macos-ptt-dictation
- Clear configuration precedence and env overrides (VOXCOMPOSE_DATA_DIR, XDG_DATA_HOME, VOXCOMPOSE_STATE_DIR)

Design
- CLI Command: voxcompose (binary or script)
  - Input: transcript via stdin
  - Output: corrected transcript via stdout
  - Flags:
    - --duration <seconds> (optional): used for downstream heuristics
    - --profile <name> (optional): selects user profile variant
    - --data-dir <path> (optional): overrides data directory (default: VOXCOMPOSE_DATA_DIR > XDG_DATA_HOME/voxcompose > ~/Library/Application Support/VoxCompose > ~/.local/share/voxcompose)
    - --state-dir <path> (optional): overrides state directory (default: VOXCOMPOSE_STATE_DIR > XDG_STATE_HOME/voxcompose > ~/Library/Application Support/VoxCompose/state > ~/.local/state/voxcompose)
    - --learn <on|off> (default: on): enable/disable persistence
    - --dry-run: no writes
    - --stats: emit sidecar JSON to stderr

- Learning store
  - File: learned_profile.json
  - Schema: wordCorrections, capitalizations, technicalVocabulary, phrasePatterns, metadata
  - Update strategy: apply corrections; on differences, register new items and write-through atomically

- Integration
  - macOS PTT: update ~/.hammerspoon/ptt_config.lua to pipe transcript into voxcompose
    - Example: transcript | voxcompose --duration {{DURATION}} > corrected
  - Optional LLM step remains separate and can be composed if desired

- Telemetry & Privacy
  - No network; no telemetry
  - All data local-only

Migration
- From legacy ~/.config/voxcompose/learned_profile.json to data dir handled by tools/migrate_learning_data.sh
- Tools updated to refuse legacy paths

Open Questions
- Packaging (Homebrew formula vs. standalone script)
- Windows support
- Formal plugin architecture for custom dictionaries

Milestones
1) Prototype CLI in a dedicated repo or subdirectory (bin/voxcompose) with simple rules
2) Wire PTT integration docs and sample changes
3) Expand learning coverage and tests
4) Package distribution (brew tap)
