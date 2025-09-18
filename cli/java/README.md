# VoxCompose Java CLI

Status: WIP (long-term)

Goals
- Stable CLI (stdin→stdout) that applies corrections and persists learning
- Flags: --duration, --data-dir, --state-dir, --profile, --learn on|off, --dry-run, --stats
- Own XDG/macOS data/state paths and migration
- Emit JSON stats to stderr when --stats is set

Plan
1) Bootstrap minimal Java project here (gradle wrapper in cli/java only)
2) Implement file-based learning store (learned_profile.json) with atomic writes
3) Integrate correction pipeline (capitalization, splits) as first pass
4) Tests for flags, I/O, data-dir precedence, and learning updates
5) CI job to build and run tests
6) Release artifacts (fat JAR) + Homebrew formula

Notes
- Keep Lua/Hammerspoon simple; this CLI is the contract.
- Avoid introducing top-level build configs; confine gradle wrapper to cli/java/.
