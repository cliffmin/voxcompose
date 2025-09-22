#!/usr/bin/env bash
set -Eeuo pipefail
IFS=$'\n\t'

# Optional CLI version test: runs only if voxcompose is on PATH
if ! command -v voxcompose >/dev/null 2>&1; then
  echo "SKIP: voxcompose not found on PATH; skipping version test" >&2
  exit 0
fi

VER="$(voxcompose --version 2>&1 | tr -d '\r')"
if [[ -z "$VER" ]]; then
  echo "FAIL: voxcompose --version returned empty output" >&2
  exit 1
fi

if ! [[ "$VER" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  echo "FAIL: version '$VER' does not look like semver X.Y.Z" >&2
  exit 1
fi

echo "PASS: voxcompose --version => $VER"