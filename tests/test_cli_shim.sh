#!/usr/bin/env bash
set -euo pipefail

TMPDIR=$(mktemp -d)
trap 'rm -rf "$TMPDIR"' EXIT
export VOXCOMPOSE_DATA_DIR="$TMPDIR/data"

SAMPLE="i need to pushto github and update the json api"
OUT=$(echo "$SAMPLE" | ./cli/voxcompose --stats 2>/dev/null)

# Output should equal input (pass-through)
if [[ "$OUT" != "$SAMPLE" ]]; then
  echo "✖ CLI shim altered output"; exit 1
fi

PROFILE="$VOXCOMPOSE_DATA_DIR/learned_profile.json"
for i in {1..30}; do [[ -f "$PROFILE" ]] && break; sleep 0.1; done
[[ -f "$PROFILE" ]] || { echo "✖ CLI shim did not update profile"; exit 1; }

echo "✔ CLI shim pass-through and learning OK"