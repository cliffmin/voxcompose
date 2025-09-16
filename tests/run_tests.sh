#!/bin/bash
# VoxCompose Test Suite
# Run all essential tests for self-learning and corrections

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR/.."

echo "=== VoxCompose Test Suite ==="
echo ""

# Build if needed
if [[ ! -f "build/libs/voxcompose-0.1.0-all.jar" ]]; then
    echo "Building VoxCompose..."
    ./gradlew --no-daemon clean fatJar >/dev/null 2>&1
fi

echo -e "${GREEN}1. Self-Learning Validation${NC}"
./tests/validate_self_learning.sh
echo ""

echo -e "${GREEN}2. Capabilities Test${NC}"
./tests/test_capabilities.sh
echo ""

echo -e "${GREEN}3. Duration Threshold Test${NC}"
./tests/test_duration_threshold.sh
echo ""

echo -e "${GREEN}4. Performance Metrics${NC}"
./tests/generate_metrics.sh
echo ""

echo -e "${GREEN}=== All Tests Complete ===${NC}"