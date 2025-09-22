#!/usr/bin/env bash
set -euo pipefail

# Aggregate test runner for VoxCompose repo

ROOT_DIR=$(cd "$(dirname "$0")/.." && pwd)

echo "[1/2] Repo checks"
bash "$ROOT_DIR/tests/run_checks.sh"

echo "[2/4] Learning hook integration"
bash "$ROOT_DIR/tests/test_learning_integration.sh"

echo "[3/4] CLI shim integration"
bash "$ROOT_DIR/tests/test_cli_shim.sh"

echo "[4/4] Optional: CLI version on PATH"
bash "$ROOT_DIR/tests/test_cli_version.sh"

echo "\nAll test suites passed."
