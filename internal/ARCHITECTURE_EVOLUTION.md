# VoxCompose Integration Evolution (Internal)

Status: Internal (not for external publication)
Owner: VoxCompose maintainers
Date: 2025-09-18

## Summary
We are evolving VoxCompose integration from local scripts (shim) to a packaged, versioned CLI distributed via Homebrew. The immediate goal was to restore learning growth quickly with minimal risk; the long‑term goal is a stable, reproducible contract for VoxCore and other callers.

## Chronology (What happened, when, and why)

1) Pre‑v0.3.x – Resource‑only repository
- Repo was documentation‑centric; no runnable tool shipped.
- Self‑learning described in docs, but the live PTT pipeline did not write to learned_profile.json.
- Learning profile default path was legacy (~/.config/voxcompose/learned_profile.json).

2) v0.3.0 – Data path corrections and tooling
- Migration to XDG data semantics: VOXCOMPOSE_DATA_DIR > XDG_DATA_HOME/voxcompose > macOS: ~/Library/Application Support/VoxCompose > Linux: ~/.local/share/voxcompose.
- Viewer tools added (show_learning.py/show_learning_data.sh), migration script and docs updated.
- Discovery: PTT pipeline still did not write learning → no growth visible.

3) Post‑v0.3.0 – Minimal learning hook (shim)
- Implemented tools/learn_from_text.py (side‑effect only; reads stdin, updates learned_profile.json atomically).
- Added transparent pass‑through: tools/learn_passthrough.sh and a CLI shim cli/voxcompose (stdin→stdout, optional flags, learning side‑effect).
- Tests: run_checks, learning integration, CLI shim integration. Docs and warp updated.
- Outcome: Immediate restoration of learning growth with one‑line integration in VoxCore; Lua remains simple.

4) Next – Proper CLI (Java) and packaging
- Implement Java CLI with a stable contract (stdin→stdout, flags, exit codes). Heavy logic moves to Java; Lua stays minimal.
- CI builds, signed release via GitHub, Homebrew formula for reproducible installs.
- VoxCore switches from absolute path to PATH‑based voxcompose; remove the Python shim over time.

## Architectural Rationale

Why a staged evolution (shim → CLI):
- Risk isolation: Restore learning quickly without packaging or runtime churn.
- Stable interface: A CLI is the public contract VoxCore relies on; scripts are a bridge, not the API.
- Distribution & reproducibility: Homebrew enables versioned installs/upgrades/rollbacks; scripts do not.
- Clear data semantics: CLI owns XDG/macOS path rules and migration; users don’t guess where learning is stored.
- Maintainability & security: Single artifact, tested, signed; simpler than managing transient Python/sh shell glue.

## Current Interfaces

- Shim (temporary)
  - tools/learn_from_text.py: learning side‑effects only; no output transformation.
  - tools/learn_passthrough.sh: cat | tee … helper, keeps output identical.
  - cli/voxcompose: pass‑through + learning; flags: --duration, --data-dir, --state-dir, --profile, --learn, --dry-run, --stats.

- Planned CLI (Java)
  - Behavior: apply corrections + persist learning in one step.
  - Contract: stdin input, stdout output; JSON stats to stderr (optional).
  - Flags: --duration, --data-dir, --state-dir, --profile, --learn on|off, --dry-run, --stats.
  - Packaging: fat JAR or native image; Homebrew formula.

## Use Cases Solved by Each Phase

- Shim phase
  - Immediate: Learning growth during dictation with zero user‑visible changes.
  - Keeps Hammerspoon/Lua simple (one pipe). No Brew dependency.

- CLI phase
  - Stable integration for VoxCore and future tools (IDEs, scripts, CI).
  - Reproducible installs across machines; team adoption.
  - Better observability (--stats) and policy control (--learn/--profile).

## Trade‑offs & Decisions

- Don’t cut a Brew release for the shim.
  - Rationale: Would immediately churn when CLI lands (deps/flags/behavior);
    weakens trust in the released interface and increases support load.

- Ship the first Brew release for the Java CLI.
  - Rationale: Locks a durable interface, enables proper versioning and security posture (checksums/signatures).

## Rollout Plan

1) Short‑term (now)
- In VoxCore (Hammerspoon), pass transcript through the helper (choose one):
  - Option A (tee background):
    - ... | tee >(python3 /Users/$USER/code/voxcompose/tools/learn_from_text.py >/dev/null)
  - Option B (recommended):
    - ... | /Users/$USER/code/voxcompose/tools/learn_passthrough.sh
  - Option C (shim CLI):
    - ... | /Users/$USER/code/voxcompose/cli/voxcompose --duration <secs> --stats
- Verify growth via python3 tools/show_learning.py --growth.

2) Mid‑term
- Implement Java CLI on feat/long-term-cli-integration.
- Add CI job “tests”; extend to matrix as needed.
- Finalize flags and data semantics.

3) Long‑term
- Cut a GitHub release; publish Homebrew formula.
- Switch VoxCore to voxcompose from PATH.
- Remove (or deprecate) Python shim.

## Operational Notes

- Data path precedence: VOXCOMPOSE_DATA_DIR > XDG_DATA_HOME/voxcompose > macOS: ~/Library/Application Support/VoxCompose > Linux: ~/.local/share/voxcompose.
- State path precedence: VOXCOMPOSE_STATE_DIR > XDG_STATE_HOME/voxcompose > macOS: ~/Library/Application Support/VoxCompose/state > Linux: ~/.local/state/voxcompose.
- Security: No secrets in logs or docs; learning profile is user data and should be backed up like application support files.

## Open Questions
- CLI packaging form: fat JAR vs native image; jre/jlink footprint vs performance.
- Windows support and path semantics.
- Plugin architecture for dictionaries and advanced correction rules.

## Appendices
- Tests: tests/run_tests.sh aggregates repo checks, learning hook integration, CLI shim integration.
- Docs updated: SELF_LEARNING.md, MACOS_PTT_INTEGRATION.md, warp.md (/cli/ guidance).