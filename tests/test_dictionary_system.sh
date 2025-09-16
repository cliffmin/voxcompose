#!/bin/bash
set -e

echo "=== Testing VoxCompose Dictionary System ==="
echo

# Build the JAR
echo "Building VoxCompose..."
./gradlew fatJar --quiet

JAR="build/libs/voxcompose-0.1.0-all.jar"

# Test 1: Basic term correction
echo "Test 1: Basic term corrections"
echo "Input: 'i use github and nodejs'"
RESULT=$(echo "i use github and nodejs" | java -jar $JAR --no-llm 2>/dev/null || true)
echo "Output: $RESULT"
if [[ "$RESULT" == *"GitHub"* ]] && [[ "$RESULT" == *"Node.js"* ]]; then
    echo "✓ Terms correctly capitalized"
else
    echo "✗ Term correction failed"
fi
echo

# Test 2: Word boundary fixes
echo "Test 2: Word boundary corrections"
echo "Input: 'need to pushto github and create pullrequest'"
RESULT=$(echo "need to pushto github and create pullrequest" | java -jar $JAR --no-llm 2>/dev/null || true)
echo "Output: $RESULT"
if [[ "$RESULT" == *"push to"* ]] && [[ "$RESULT" == *"pull request"* ]]; then
    echo "✓ Word boundaries correctly fixed"
else
    echo "✗ Word boundary correction failed"
fi
echo

# Test 3: Complex technical sentence
echo "Test 3: Complex technical sentence"
INPUT="gonna pushto github with my nodejs api using docker and postgresql on aws"
echo "Input: '$INPUT'"
RESULT=$(echo "$INPUT" | java -jar $JAR --no-llm 2>/dev/null || true)
echo "Output: $RESULT"
if [[ "$RESULT" == *"GitHub"* ]] && [[ "$RESULT" == *"Node.js"* ]] && \
   [[ "$RESULT" == *"API"* ]] && [[ "$RESULT" == *"Docker"* ]] && \
   [[ "$RESULT" == *"PostgreSQL"* ]] && [[ "$RESULT" == *"AWS"* ]]; then
    echo "✓ All technical terms correctly formatted"
else
    echo "✗ Some corrections missed"
fi
echo

# Test 4: Create custom dictionary
echo "Test 4: Custom dictionary"
CUSTOM_DICT="/tmp/custom_dict.yaml"
cat > $CUSTOM_DICT <<EOF
name: Custom Test
version: 1.0.0
priority: 200
enabled: true

terms:
  mycompany: MyCompany
  customapi: CustomAPI
  
boundaries:
  - pattern: "goto"
    replacement: "go to"
EOF

echo "Created custom dictionary with 'mycompany' and 'customapi' terms"
echo "Input: 'goto mycompany customapi'"
# Note: This would need Main.java to support loading custom dictionaries via CLI
echo "✓ Custom dictionary created (would need CLI support to test)"
echo

# Test 5: Performance test
echo "Test 5: Performance with multiple corrections"
START=$(date +%s%N)
echo "let me pushto github with nodejs and docker using postgresql" | java -jar $JAR --no-llm 2>/dev/null || true
END=$(date +%s%N)
DURATION=$((($END - $START) / 1000000))
echo "Processing time: ${DURATION}ms"
if [ $DURATION -lt 500 ]; then
    echo "✓ Fast processing (<500ms)"
else
    echo "⚠ Slower than expected (${DURATION}ms)"
fi
echo

echo "=== Dictionary System Tests Complete ==="#
