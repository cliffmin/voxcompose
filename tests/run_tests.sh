#!/usr/bin/env bash
set -euo pipefail

# Aggregate test runner for VoxCompose repo

ROOT_DIR=$(cd "$(dirname "$0")/.." && pwd)

echo "[1/2] Repo checks"
bash "$ROOT_DIR/tests/run_checks.sh"

echo "[2/2] Learning hook integration"
bash "$ROOT_DIR/tests/test_learning_integration.sh"

echo "\nAll test suites passed."