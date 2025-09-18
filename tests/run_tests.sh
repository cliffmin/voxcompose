#!/usr/bin/env bash
set -euo pipefail

# Aggregate test runner for VoxCompose repo

ROOT_DIR=$(cd "$(dirname "$0")/.." && pwd)

echo "[1/2] Repo checks"
bash "$ROOT_DIR/tests/run_checks.sh"

echo "[2/3] Learning hook integration"
bash "$ROOT_DIR/tests/test_learning_integration.sh"

echo "[3/3] CLI shim integration"
bash "$ROOT_DIR/tests/test_cli_shim.sh"

echo "\nAll test suites passed."
