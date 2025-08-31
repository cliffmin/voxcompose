#!/usr/bin/env bash
set -euo pipefail

# Refine all fixture .txt files with VoxCompose; assert non-empty Markdown output and exit 0.
JAR="${HOME}/code/voxcompose/build/libs/voxcompose-0.1.0-all.jar"
FIXDIR="$(cd "$(dirname "$0")/.." && pwd)/fixtures"
MODEL="${VOX_MODEL:-llama3.1}"
TIMEOUT="${VOX_TIMEOUT_MS:-10000}"
MEMORY_PATH="${VOX_MEMORY:-$HOME/Library/Application Support/voxcompose/memory.jsonl}"

if [ ! -f "$JAR" ]; then
  echo "Missing VoxCompose jar: $JAR" >&2
  echo "Build it: ./gradlew --no-daemon clean fatJar" >&2
  exit 2
fi
if [ ! -d "$FIXDIR" ]; then
  echo "No fixtures directory: $FIXDIR" >&2
  exit 2
fi

shopt -s nullglob
PASS=0; FAIL=0
for txt in "$FIXDIR"/*.txt; do
  base="${txt%.txt}"
  name="$(basename "$base")"
  out="$(mktemp)"; trap 'rm -f "$out"' EXIT
  if ! cat "$txt" | java -jar "$JAR" --model "$MODEL" --timeout-ms "$TIMEOUT" --memory "$MEMORY_PATH" > "$out" 2>/dev/null; then
    echo "FAIL refine: $name (jar error)" >&2
    FAIL=$((FAIL+1)); continue
  fi
  if [ ! -s "$out" ]; then
    echo "FAIL refine: $name (empty output)" >&2
    FAIL=$((FAIL+1)); continue
  fi
  echo "OK refine: $name"
  PASS=$((PASS+1))
  rm -f "$out"
  trap - EXIT
done

if [ $FAIL -gt 0 ]; then
  echo "Completed with failures: PASS=$PASS FAIL=$FAIL" >&2
  exit 1
fi

echo "All fixtures refined successfully: PASS=$PASS FAIL=$FAIL"

