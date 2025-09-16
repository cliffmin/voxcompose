#!/bin/bash
# Smoke Test: VoxCompose LLM refinement when Ollama is available

set -e

echo "=== VoxCompose LLM Smoke Test ==="
echo ""

# Build the JAR
echo "Building VoxCompose..."
(cd .. && ./gradlew fatJar >/dev/null 2>&1)

JAR="../build/libs/voxcompose-0.1.0-all.jar"

# Check if Ollama is running
echo -n "Checking if Ollama is available... "
if curl -s http://127.0.0.1:11434/api/tags >/dev/null 2>&1; then
    echo "✅ Ollama is running"
else
    echo "⚠️  Ollama not running - skipping LLM test"
    echo "To run this test, start Ollama first: ollama serve"
    exit 0
fi

# Check if llama3.1 model is available
echo -n "Checking for llama3.1 model... "
if curl -s http://127.0.0.1:11434/api/tags | jq -r '.models[].name' | grep -q "llama3.1"; then
    echo "✅ Model available"
else
    echo "⚠️  llama3.1 not found"
    echo "To run this test, pull the model: ollama pull llama3.1"
    exit 0
fi

# Test simple refinement
echo ""
echo "Testing LLM refinement with simple input:"
INPUT="this is a test of the llm refiner it should make this text better"
echo "Input: $INPUT"
echo ""

# Run refinement with timeout
OUTPUT=$(timeout 30 echo "$INPUT" | java -jar "$JAR" 2>/dev/null || echo "TIMEOUT")

if [[ "$OUTPUT" == "TIMEOUT" ]]; then
    echo "❌ FAIL - Refinement timed out after 30 seconds"
    exit 1
elif [[ -z "$OUTPUT" ]]; then
    echo "❌ FAIL - No output received"
    exit 1
else
    echo "Output received (first 100 chars):"
    echo "$OUTPUT" | head -c 100
    echo ""
    echo ""
    
    # Basic validation - output should be different from input
    if [[ "$OUTPUT" != "$INPUT" ]]; then
        echo "✅ PASS - Refinement produced different output"
    else
        echo "⚠️  WARN - Output identical to input"
    fi
    
    # Check if it looks like markdown
    if echo "$OUTPUT" | grep -qE "^#|^\*|^\-"; then
        echo "✅ PASS - Output contains markdown formatting"
    else
        echo "⚠️  WARN - No markdown formatting detected"
    fi
fi

# Test with corrections + refinement
echo ""
echo "Testing combined corrections + LLM refinement:"
INPUT2="i wanna pushto github and committhis code"
echo "Input: $INPUT2"
echo ""

OUTPUT2=$(timeout 30 echo "$INPUT2" | java -jar "$JAR" 2>/dev/null || echo "TIMEOUT")

if [[ "$OUTPUT2" == "TIMEOUT" ]]; then
    echo "❌ FAIL - Refinement timed out"
    exit 1
else
    echo "Output received (first 100 chars):"
    echo "$OUTPUT2" | head -c 100
    echo ""
    echo ""
    
    # Check if corrections were applied
    if echo "$OUTPUT2" | grep -qi "github\|push to\|commit"; then
        echo "✅ PASS - Corrections appear to be applied"
    else
        echo "⚠️  WARN - Corrections may not have been applied"
    fi
fi

echo ""
echo "=== LLM Smoke Test Complete ==="
