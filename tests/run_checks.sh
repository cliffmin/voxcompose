#!/usr/bin/env bash
set -euo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'

function ok() { echo -e "${GREEN}✔${NC} $1"; }
function fail() { echo -e "${RED}✖${NC} $1"; exit 1; }

# 1) Ensure repo is resource-only (no build system / code dirs)
test ! -e build.gradle.kts || fail "build.gradle.kts should not exist"
test ! -e settings.gradle.kts || fail "settings.gradle.kts should not exist"
test ! -d gradle || fail "gradle/ should not exist"
test ! -d src || fail "src/ should not exist"
test ! -d packaging || fail "packaging/ should not exist"
test ! -d bin || fail "bin/ should not exist"
ok "No build system or source code detected"

# 2) README clearly indicates resource-only nature and avoids install/build commands
grep -qi "resource dump" README.md || fail "README should indicate 'resource dump' usage"
! grep -qiE "gradle|java -jar|brew install|fatJar|--model|voxcompose --help" README.md || fail "README contains build/CLI/install commands"
ok "README messaging is resource-only"

# 3) Docs folder exists
test -d docs || fail "docs/ directory missing"
ok "docs directory present"

echo -e "\n${GREEN}All checks passed.${NC}"

