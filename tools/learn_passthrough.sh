#!/usr/bin/env bash
# Transparent pass-through that also updates VoxCompose learning.
# Reads stdin, writes to stdout unchanged, and feeds the same input to learn_from_text.py in the background.
# Usage: ... | tools/learn_passthrough.sh | ...
set -euo pipefail
SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
cat | tee >("${SCRIPT_DIR}/python3_wrapper.sh" "$SCRIPT_DIR/learn_from_text.py")