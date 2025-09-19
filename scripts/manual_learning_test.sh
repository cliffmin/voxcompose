#!/usr/bin/env bash
# Manual learning validation for VoxCompose
# - Baseline counts from learned_profile.json
# - Feed a controlled transcript that should trigger new learning
# - Show before/after counts and deltas
#
# Usage:
#   bash scripts/manual_learning_test.sh
#   bash scripts/manual_learning_test.sh --use-temp
#   bash scripts/manual_learning_test.sh --data-dir /custom/path
#
# Depends on:
#   - cli/voxcompose (preferred) OR tools/learn_from_text.py (fallback)
#   - python3 available in PATH

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

USE_TEMP=0
DATA_DIR_OVERRIDE=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --use-temp) USE_TEMP=1; shift ;;
    --data-dir) shift; DATA_DIR_OVERRIDE="${1:-}"; shift || true ;;
    -h|--help)
      sed -n '1,40p' "$0"
      exit 0 ;;
    *) echo "Unknown option: $1" >&2; exit 2 ;;
  esac
done

resolve_data_dir() {
  if [[ -n "${VOXCOMPOSE_DATA_DIR:-}" ]]; then
    printf "%s\n" "$VOXCOMPOSE_DATA_DIR"
    return
  fi
  if [[ -n "${XDG_DATA_HOME:-}" ]]; then
    printf "%s\n" "$XDG_DATA_HOME/voxcompose"
    return
  fi
  local os; os="$(uname -s 2>/dev/null || true)"
  if [[ "$os" == "Darwin" ]]; then
    printf "%s\n" "$HOME/Library/Application Support/VoxCompose"
  else
    printf "%s\n" "$HOME/.local/share/voxcompose"
  fi
}

# Set up data dir selection
TMPDIR_CREATED=""
if [[ $USE_TEMP -eq 1 ]]; then
  TMPDIR_CREATED="$(mktemp -d)"
  export VOXCOMPOSE_DATA_DIR="$TMPDIR_CREATED/data"
elif [[ -n "$DATA_DIR_OVERRIDE" ]]; then
  export VOXCOMPOSE_DATA_DIR="$DATA_DIR_OVERRIDE"
fi

DATA_DIR="$(resolve_data_dir)"
PROFILE="$DATA_DIR/learned_profile.json"

count_profile() {
  local p="$1"
  python3 - "$p" <<'PY'
import json, sys, os
p = sys.argv[1]
wc = cap = vocab = 0
try:
    with open(p, 'r') as f:
        d = json.load(f)
    wc = len(d.get('wordCorrections', {}) or {})
    cap = len(d.get('capitalizations', {}) or {})
    vocab = len(d.get('technicalVocabulary', []) or [])
except Exception:
    pass
print(f"{wc} {cap} {vocab}")
PY
}

mtime_of() {
  local p="$1"
  if [[ -f "$p" ]]; then
    # macOS stat format
    stat -f "%Sm" -t "%Y-%m-%d %H:%M:%S %Z" "$p" 2>/dev/null || echo "n/a"
  else
    echo "n/a"
  fi
}

# Baseline counts
read -r WC_BEFORE CAP_BEFORE VOCAB_BEFORE < <(count_profile "$PROFILE")
MTIME_BEFORE="$(mtime_of "$PROFILE")"

echo "VoxCompose manual learning validation"
echo "Profile path: $PROFILE"
echo "Before -> wordCorrections=$WC_BEFORE, capitalizations=$CAP_BEFORE, technicalVocabulary=$VOCAB_BEFORE"
echo "Before mtime: $MTIME_BEFORE"
echo

# Choose runner
RUN_CLI="$ROOT_DIR/cli/voxcompose"
USE_CLI=0
if [[ -x "$RUN_CLI" ]]; then
  USE_CLI=1
fi

SAMPLE="i need to pushto github and update the json api"

echo "Feeding controlled transcript to trigger learning:"
echo "  \"$SAMPLE\""
if [[ $USE_CLI -eq 1 ]]; then
  echo "$SAMPLE" | "$RUN_CLI" --data-dir "$DATA_DIR" >/dev/null
else
  echo "$SAMPLE" | python3 "$ROOT_DIR/tools/learn_from_text.py" >/dev/null
fi

# Wait for profile to appear/update
for i in {1..50}; do
  [[ -f "$PROFILE" ]] && break
  sleep 0.1
done
sleep 0.1

read -r WC_AFTER CAP_AFTER VOCAB_AFTER < <(count_profile "$PROFILE")
MTIME_AFTER="$(mtime_of "$PROFILE")"

DW=$(( WC_AFTER - WC_BEFORE ))
DC=$(( CAP_AFTER - CAP_BEFORE ))
DV=$(( VOCAB_AFTER - VOCAB_BEFORE ))
TOTAL_DELTA=$(( DW + DC + DV ))

echo
echo "After  -> wordCorrections=$WC_AFTER, capitalizations=$CAP_AFTER, technicalVocabulary=$VOCAB_AFTER"
echo "After mtime:  $MTIME_AFTER"
echo "Deltas -> +wordCorrections=$DW, +capitalizations=$DC, +technicalVocabulary=$DV, total=+$TOTAL_DELTA"
echo

if [[ $TOTAL_DELTA -gt 0 ]]; then
  echo "SUCCESS: Learning events were recorded."
else
  echo "WARNING: No new learning detected. Consider:"
  echo "  - Ensure the sample text is novel for your profile"
  echo "  - Try --use-temp to isolate from existing data"
  echo "  - Inspect with: python3 tools/show_learning.py --summary"
fi

if [[ -n "$TMPDIR_CREATED" ]]; then
  echo
  echo "Temporary data dir preserved for inspection:"
  echo "  $VOXCOMPOSE_DATA_DIR"
  echo "Remove it when done: rm -rf \"$TMPDIR_CREATED\""
fi
