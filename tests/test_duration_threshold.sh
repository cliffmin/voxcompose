#!/bin/bash
# Test duration threshold functionality

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR/.."

echo "=== Testing Duration Threshold Functionality ==="
echo ""

JAR="build/libs/voxcompose-1.0.0-all.jar"
if [[ ! -f "$JAR" ]]; then
    echo "Building VoxCompose..."
    ./gradlew --no-daemon fatJar >/dev/null 2>&1
fi

# Test 1: Short duration (should skip LLM)
echo "Test 1: Short duration (10s - below threshold)"
echo "Testing input with duration 10s" | java -jar "$JAR" --duration 10 2>&1 | grep -q "below threshold" && echo "✅ PASS - Correctly skipped LLM for short duration" || echo "❌ FAIL - Did not skip LLM"

# Test 2: Long duration (should attempt LLM - just verify no "below threshold" message)
echo ""
echo "Test 2: Long duration (30s - above threshold)"
OUTPUT=$(echo "Testing input with duration 30s" | java -jar "$JAR" --duration 30 2>&1 || true)
if echo "$OUTPUT" | grep -q "below threshold"; then
    echo "❌ FAIL - Incorrectly skipped LLM for long duration"
else
    echo "✅ PASS - Did not skip LLM for long duration (LLM may fail if Ollama not running)"
fi

# Test 3: Capabilities endpoint
echo ""
echo "Test 3: Capabilities endpoint"
CAPS=$(java -jar "$JAR" --capabilities 2>/dev/null)
if echo "$CAPS" | jq -r '.activation.long_form.min_duration' >/dev/null 2>&1; then
    THRESHOLD=$(echo "$CAPS" | jq -r '.activation.long_form.min_duration')
    echo "✅ PASS - Capabilities returned threshold: ${THRESHOLD}s"
else
    echo "❌ FAIL - Capabilities did not return valid JSON"
fi

# Test 4: Corrections without LLM
echo ""
echo "Test 4: Corrections applied even when LLM skipped"
INPUT="i wanna pushto github and committhis code"
OUTPUT=$(echo "$INPUT" | VOX_REFINE=0 java -jar "$JAR" 2>/dev/null)
if echo "$OUTPUT" | grep -q "push to"; then
    echo "✅ PASS - Corrections applied without LLM"
else
    echo "❌ FAIL - Corrections not applied: $OUTPUT"
fi

# Test 5: Test concatenation fixes in sentence context
echo ""
echo "Test 5: Concatenation fixes in context"
INPUT="you shouldhave seen it you couldhave helped"
OUTPUT=$(echo "$INPUT" | VOX_REFINE=0 java -jar "$JAR" 2>/dev/null)
if echo "$OUTPUT" | grep -q "should have" && echo "$OUTPUT" | grep -q "could have"; then
    echo "✅ PASS - Concatenations fixed in sentence context"
else
    echo "❌ FAIL - Concatenations not fixed: $OUTPUT"
fi

echo ""
echo "=== Duration Threshold Tests Complete ===
"