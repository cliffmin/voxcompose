#!/usr/bin/env bash
set -euo pipefail

# tests/test_learning_path.sh
# Verifies path resolution precedence and migration script behavior

ok() { echo "✔ $1"; }
fail() { echo "✖ $1"; exit 1; }

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
REPO_DIR=$(cd "$SCRIPT_DIR/.." && pwd)
MIGRATE="$REPO_DIR/tools/migrate_learning_data.sh"
VIEWER="$REPO_DIR/tools/show_learning.py"

TMPDIR=$(mktemp -d)
trap 'rm -rf "$TMPDIR"' EXIT

export HOME="$TMPDIR/home"
mkdir -p "$HOME"/.config/voxcompose
mkdir -p "$HOME"/.local/share/voxcompose

# Create legacy profile
LEGACY="$HOME/.config/voxcompose/learned_profile.json"
# Compute expected new path using same precedence as scripts
if [[ -n "${XDG_DATA_HOME:-}" ]]; then
  NEW="$XDG_DATA_HOME/voxcompose/learned_profile.json"
else
  if [[ "$(uname -s)" == "Darwin" ]]; then
    NEW="$HOME/Library/Application Support/VoxCompose/learned_profile.json"
  else
    NEW="$HOME/.local/share/voxcompose/learned_profile.json"
  fi
fi
mkdir -p "$(dirname "$LEGACY")"
echo '{"wordCorrections": {"pushto": "push to"}}' > "$LEGACY"

# 1) Viewer should NOT read legacy when new not present (expect failure)
if python3 "$VIEWER" --summary >/dev/null 2>&1; then
  fail "Viewer unexpectedly read legacy profile"
else
  ok "Viewer correctly refuses legacy profile"
fi

# 2) Migrate
bash "$MIGRATE" >/dev/null || fail "Migration script failed"

# New file should exist
test -f "$NEW" || fail "New profile missing after migration (expected at $NEW)"
ok "Migration created new profile"

# 3) Viewer should read new path now
python3 "$VIEWER" --summary >/dev/null || fail "Viewer failed to read new profile"
ok "Viewer reads new profile after migration"

# 4) Precedence with VOXCOMPOSE_DATA_DIR
export VOXCOMPOSE_DATA_DIR="$HOME/custom_data"
mkdir -p "$VOXCOMPOSE_DATA_DIR"
echo '{"wordCorrections": {"committhis": "commit this"}}' > "$VOXCOMPOSE_DATA_DIR/learned_profile.json"
python3 "$VIEWER" --summary >/dev/null || fail "Viewer failed with VOXCOMPOSE_DATA_DIR override"
ok "Viewer respects VOXCOMPOSE_DATA_DIR"

echo "All tests passed."
