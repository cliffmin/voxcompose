#!/usr/bin/env bash
set -euo pipefail

# Performance benchmark for VoxCompose
# Measures startup time, cache effectiveness, and throughput

JAR="${HOME}/code/voxcompose/build/libs/voxcompose-0.1.0-all.jar"
SAMPLE_TEXT="This is a sample transcript that needs refinement. It contains some, uh, disfluencies and needs proper structure."

if [ ! -f "$JAR" ]; then
  echo "Error: JAR not found at $JAR" >&2
  echo "Run: ./gradlew --no-daemon clean fatJar" >&2
  exit 1
fi

echo "=== VoxCompose Performance Benchmark ==="
echo

# Test 1: Baseline startup time (refinement disabled)
echo "1. Baseline startup time (VOX_REFINE=0):"
for i in {1..5}; do
  TIME=$( { time echo "$SAMPLE_TEXT" | VOX_REFINE=0 java -jar "$JAR" >/dev/null 2>&1; } 2>&1 | grep real | awk '{print $2}' )
  echo "  Run $i: $TIME"
done
echo

# Test 2: Small input processing (no cache)
echo "2. Small input processing (no cache):"
for i in {1..3}; do
  TIME=$( { time echo "$SAMPLE_TEXT" | java -jar "$JAR" --model llama3.1 >/dev/null 2>&1; } 2>&1 | grep real | awk '{print $2}' )
  echo "  Run $i: $TIME"
done
echo

# Test 3: Cache effectiveness (same input)
echo "3. Cache effectiveness test:"
TEMP_OUT=$(mktemp)
echo "  First run (cold cache):"
TIME1=$( { time echo "$SAMPLE_TEXT" | java -jar "$JAR" --cache --model llama3.1 2>&1 | tee "$TEMP_OUT"; } 2>&1 | grep real | awk '{print $2}' )
echo "    Time: $TIME1"

echo "  Second run (warm cache - should be faster):"
TIME2=$( { time echo "$SAMPLE_TEXT" | java -jar "$JAR" --cache --model llama3.1 >/dev/null 2>&1; } 2>&1 | grep real | awk '{print $2}' )
echo "    Time: $TIME2"

echo "  Third run (cache hit):"
TIME3=$( { time echo "$SAMPLE_TEXT" | java -jar "$JAR" --cache --model llama3.1 >/dev/null 2>&1; } 2>&1 | grep real | awk '{print $2}' )
echo "    Time: $TIME3"
rm -f "$TEMP_OUT"
echo

# Test 4: Large input handling
echo "4. Large input handling (1000 lines):"
LARGE_INPUT=$(yes "$SAMPLE_TEXT" | head -1000)
TIME=$( { time echo "$LARGE_INPUT" | VOX_REFINE=0 java -jar "$JAR" >/dev/null 2>&1; } 2>&1 | grep real | awk '{print $2}' )
echo "  Processing time: $TIME"
echo

# Test 5: Memory file processing
echo "5. Memory file processing:"
TEMP_MEMORY=$(mktemp)
for i in {1..20}; do
  echo "{\"ts\":\"2025-01-0${i}T00:00:00Z\",\"kind\":\"preference\",\"text\":\"Test preference $i\"}" >> "$TEMP_MEMORY"
done
TIME=$( { time echo "$SAMPLE_TEXT" | VOX_REFINE=0 java -jar "$JAR" --memory "$TEMP_MEMORY" >/dev/null 2>&1; } 2>&1 | grep real | awk '{print $2}' )
echo "  Time with 20 memory items: $TIME"
rm -f "$TEMP_MEMORY"
echo

echo "=== Benchmark Complete ==="
echo
echo "Key Performance Indicators:"
echo "- Startup overhead: Check Test 1 times (should be <100ms)"
echo "- Cache effectiveness: Compare Test 3 run times (2nd/3rd should be faster)"
echo "- Large input handling: Test 4 shows scalability"
echo "- Memory processing: Test 5 shows JSONL parsing efficiency"