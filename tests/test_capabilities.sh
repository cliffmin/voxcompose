#!/bin/bash
# Test: VoxCompose capabilities endpoint returns valid JSON with expected fields

set -e

echo "=== Testing VoxCompose Capabilities Endpoint ==="

# Build the JAR
echo "Building VoxCompose..."
(cd .. && ./gradlew fatJar >/dev/null 2>&1)

JAR="../build/libs/voxcompose-1.0.0-all.jar"

# Test 1: Capabilities endpoint returns valid JSON
echo -n "Test 1: Capabilities returns valid JSON... "
OUTPUT=$(java -jar "$JAR" --capabilities 2>/dev/null)
if echo "$OUTPUT" | jq . >/dev/null 2>&1; then
    echo "✅ PASS"
else
    echo "❌ FAIL - Invalid JSON"
    exit 1
fi

# Test 2: Check required fields exist
echo -n "Test 2: Required fields present... "
MIN_DURATION=$(echo "$OUTPUT" | jq -r '.activation.long_form.min_duration')
VERSION=$(echo "$OUTPUT" | jq -r '.version')
LEARNING_ENABLED=$(echo "$OUTPUT" | jq -r '.learning.enabled')

if [[ -n "$MIN_DURATION" && "$MIN_DURATION" != "null" && 
      -n "$VERSION" && "$VERSION" != "null" &&
      -n "$LEARNING_ENABLED" && "$LEARNING_ENABLED" != "null" ]]; then
    echo "✅ PASS"
else
    echo "❌ FAIL - Missing required fields"
    exit 1
fi

# Test 3: Verify default threshold
echo -n "Test 3: Default threshold is 21 seconds... "
if [[ "$MIN_DURATION" == "21" ]]; then
    echo "✅ PASS"
else
    echo "⚠️  WARN - Threshold is $MIN_DURATION (expected 21)"
fi

# Test 4: Learning is enabled
echo -n "Test 4: Learning system is enabled... "
if [[ "$LEARNING_ENABLED" == "true" ]]; then
    echo "✅ PASS"
else
    echo "⚠️  WARN - Learning is disabled"
fi

echo ""
echo "=== Capabilities Test Complete ==="
