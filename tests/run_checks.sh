#!/usr/bin/env bash
set -euo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'

function ok() { echo -e "${GREEN}✔${NC} $1"; }
function fail() { echo -e "${RED}✖${NC} $1"; exit 1; }

# 1) Required directories/files exist
[ -d docs ] || fail "docs/ directory missing"
ok "docs directory present"

[ -f tools/learn_from_text.py ] || fail "tools/learn_from_text.py missing"
[ -x tools/learn_from_text.py ] || fail "tools/learn_from_text.py not executable"
ok "learning hook present and executable"

# 2) Documentation links present
grep -qi "Long-Term CLI Integration Plan" README.md || fail "README missing link to long-term CLI plan"
ok "README links long-term plan"

echo -e "\n${GREEN}All checks passed.${NC}"

