#!/bin/bash
# Test: VoxCompose applies learned corrections even without LLM

set -e

echo "=== Testing VoxCompose Self-Learning Corrections ==="

# Build the JAR
echo "Building VoxCompose..."
(cd .. && ./gradlew fatJar >/dev/null 2>&1)

JAR="../build/libs/voxcompose-1.0.0-all.jar"

# Test cases for common corrections
declare -A TEST_CASES
TEST_CASES["pushto github"]="push to GitHub"
TEST_CASES["committhis code"]="commit this code"
TEST_CASES["use json api"]="use JSON api"
TEST_CASES["i wanna push"]="i wanna push"  # "wanna" stays as-is for now

echo ""
echo "Testing corrections (LLM will likely fail, but corrections should apply):"
echo ""

PASS_COUNT=0
FAIL_COUNT=0

for INPUT in "${!TEST_CASES[@]}"; do
    EXPECTED="${TEST_CASES[$INPUT]}"
    echo -n "Test: '$INPUT' → '$EXPECTED'... "
    
    # Run with disabled refinement to ensure we're testing corrections only
    ACTUAL=$(VOX_REFINE=false echo "$INPUT" | java -jar "$JAR" 2>/dev/null | tr -d '\n')
    
    if [[ "$ACTUAL" == "$EXPECTED" ]]; then
        echo "✅ PASS"
        ((PASS_COUNT++))
    else
        echo "❌ FAIL (got: '$ACTUAL')"
        ((FAIL_COUNT++))
    fi
done

echo ""
echo "=== Summary ==="
echo "Passed: $PASS_COUNT"
echo "Failed: $FAIL_COUNT"

if [[ $FAIL_COUNT -eq 0 ]]; then
    echo "✅ All corrections working!"
    exit 0
else
    echo "⚠️  Some corrections not working as expected"
    exit 1
fi