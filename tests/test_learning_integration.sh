#!/usr/bin/env bash
set -euo pipefail

# Verifies that piping text through the transparent learner hook updates
# the learned_profile.json in the correct data directory.

ok() { echo "✔ $1"; }
fail() { echo "✖ $1"; exit 1; }

# Isolate from the user's real profile
TMPDIR=$(mktemp -d)
trap 'rm -rf "$TMPDIR"' EXIT
export VOXCOMPOSE_DATA_DIR="$TMPDIR/data"

SAMPLE="i need to pushto github and update the json api"

# Run through the pass-through pattern (simulate PTT post-processing)
echo "$SAMPLE" | tee >(python3 tools/learn_from_text.py >/dev/null) >/dev/null || true

PROFILE="$VOXCOMPOSE_DATA_DIR/learned_profile.json"

# Assert the profile was created
if [[ ! -f "$PROFILE" ]]; then
  fail "learned_profile.json not created at expected path: $PROFILE"
fi
ok "Profile created at expected path"

# Validate that expected learnings exist (splits + caps)
python3 - <<PY
import json, sys, os
p = os.environ.get('VOXCOMPOSE_DATA_DIR') + '/learned_profile.json'
with open(p, 'r') as f:
    data = json.load(f)
wc = data.get('wordCorrections', {})
cap = data.get('capitalizations', {})
errors = []
if wc.get('pushto') != 'push to':
    errors.append('wordCorrections.pushto')
for k, v in [('json','JSON'), ('api','API'), ('github','GitHub')]:
    if cap.get(k) != v:
        errors.append(f'capitalizations.{k}')
if errors:
    print('Missing/incorrect learnings:', ', '.join(errors))
    sys.exit(1)
PY
ok "Expected learnings persisted (pushto split, JSON/API/GitHub caps)"

echo "All tests passed."