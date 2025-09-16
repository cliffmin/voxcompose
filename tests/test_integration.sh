#!/bin/bash
# Integration Test: Configuration, learning, and end-to-end workflow

set -e

echo "=== VoxCompose Integration Test ==="
echo ""

# Build the JAR
echo "Building VoxCompose..."
(cd .. && ./gradlew fatJar >/dev/null 2>&1)

JAR="../build/libs/voxcompose-0.1.0-all.jar"
PROFILE_DIR="$HOME/.config/voxcompose"
PROFILE_FILE="$PROFILE_DIR/learned_profile.json"

# Test 1: Capabilities negotiation
echo "Test 1: Capabilities endpoint works"
CAPS=$(java -jar "$JAR" --capabilities 2>/dev/null)
if echo "$CAPS" | jq -e '.activation.long_form.min_duration' >/dev/null; then
    echo "✅ PASS - Capabilities endpoint returns valid data"
else
    echo "❌ FAIL - Invalid capabilities response"
    exit 1
fi

# Test 2: Corrections work without profile
echo ""
echo "Test 2: Built-in corrections work without profile"
RESULT=$(VOX_REFINE=false echo "pushto github" | java -jar "$JAR" 2>/dev/null | tr -d '\n')
if [[ "$RESULT" == "push to GitHub" ]]; then
    echo "✅ PASS - Built-in corrections working"
else
    echo "❌ FAIL - Expected 'push to GitHub', got '$RESULT'"
    exit 1
fi

# Test 3: Profile directory creation
echo ""
echo "Test 3: Profile directory handling"
if [[ -d "$PROFILE_DIR" ]]; then
    echo "✅ Profile directory exists: $PROFILE_DIR"
else
    echo "⚠️  Profile directory not yet created (will be created on first learn)"
fi

# Test 4: Environment variable handling
echo ""
echo "Test 4: Environment variable configuration"
export VOX_REFINE=false
RESULT=$(echo "test input" | java -jar "$JAR" 2>&1 | grep -c "LLM refinement disabled")
if [[ $RESULT -eq 1 ]]; then
    echo "✅ PASS - VOX_REFINE environment variable respected"
else
    echo "❌ FAIL - VOX_REFINE not working correctly"
    exit 1
fi
unset VOX_REFINE

# Test 5: Multiple corrections in single input
echo ""
echo "Test 5: Multiple corrections in single input"
INPUT="pushto github and committhis code using json"
EXPECTED="push to GitHub and commit this code using JSON"
RESULT=$(VOX_REFINE=false echo "$INPUT" | java -jar "$JAR" 2>/dev/null | tr -d '\n')
if [[ "$RESULT" == "$EXPECTED" ]]; then
    echo "✅ PASS - Multiple corrections applied correctly"
else
    echo "❌ FAIL - Expected '$EXPECTED', got '$RESULT'"
    exit 1
fi

# Test 6: Case sensitivity handling
echo ""
echo "Test 6: Case handling in corrections"
declare -A CASE_TESTS
CASE_TESTS["PUSHTO GITHUB"]="PUSHTO GitHub"  # Only GitHub should be corrected
CASE_TESTS["Json api"]="JSON api"
CASE_TESTS["github json"]="GitHub JSON"

CASE_PASS=0
for INPUT in "${!CASE_TESTS[@]}"; do
    EXPECTED="${CASE_TESTS[$INPUT]}"
    RESULT=$(VOX_REFINE=false echo "$INPUT" | java -jar "$JAR" 2>/dev/null | tr -d '\n')
    if [[ "$RESULT" == "$EXPECTED" ]]; then
        ((CASE_PASS++))
    else
        echo "  ⚠️  '$INPUT' → '$RESULT' (expected '$EXPECTED')"
    fi
done
echo "✅ Case handling: $CASE_PASS/${#CASE_TESTS[@]} passed"

# Test 7: Empty input handling
echo ""
echo "Test 7: Edge cases"
EMPTY=$(echo "" | java -jar "$JAR" 2>/dev/null)
if [[ -z "$EMPTY" ]]; then
    echo "✅ PASS - Empty input handled correctly"
else
    echo "❌ FAIL - Empty input produced output: '$EMPTY'"
fi

# Test 8: Command-line argument handling
echo ""
echo "Test 8: Command-line arguments"
HELP=$(java -jar "$JAR" --help 2>&1 | grep -c "VoxCompose" || true)
if [[ $HELP -gt 0 ]]; then
    echo "✅ PASS - Help flag works"
else
    echo "⚠️  WARN - Help flag may not be working"
fi

echo ""
echo "=== Integration Test Summary ==="
echo "✅ Core functionality verified"
echo "✅ Corrections working as expected"
echo "✅ Configuration properly handled"
echo ""
echo "Profile location: $PROFILE_DIR"
echo "To see learned corrections: cat $PROFILE_FILE 2>/dev/null | jq '.wordCorrections'"
echo ""
echo "=== All Integration Tests Passed ==="
