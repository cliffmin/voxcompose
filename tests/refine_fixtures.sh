#!/usr/bin/env bash
set -euo pipefail

# Refine all fixture .txt files with VoxCompose; assert non-empty Markdown output and that
# the CLI logs a distinctive line indicating refinement began. Also run one disabled test via VOX_REFINE=0.
JAR="${HOME}/code/voxcompose/build/libs/voxcompose-1.0.0-all.jar"
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
PASS=0; FAIL=0; SEEN_ANY=0; DISABLED_RAN=0
for txt in "$FIXDIR"/*.txt; do
  [ -e "$txt" ] || break
  SEEN_ANY=1
  base="${txt%.txt}"
  name="$(basename "$base")"
  out="$(mktemp)"; err="$(mktemp)"; trap 'rm -f "$out" "$err"' EXIT
  if ! cat "$txt" | java -jar "$JAR" --model "$MODEL" --timeout-ms "$TIMEOUT" --memory "$MEMORY_PATH" > "$out" 2>"$err"; then
    echo "FAIL refine: $name (jar error)" >&2
    FAIL=$((FAIL+1)); continue
  fi
  if ! grep -q "INFO: Running LLM refinement with model:" "$err"; then
    echo "FAIL refine: $name (missing refinement start log)" >&2
    echo "stderr snippet:" >&2
    sed -n '1,8p' "$err" >&2 || true
    FAIL=$((FAIL+1)); continue
  fi
  if [ ! -s "$out" ]; then
    echo "FAIL refine: $name (empty output)" >&2
    FAIL=$((FAIL+1)); continue
  fi
  echo "OK refine: $name"
  PASS=$((PASS+1))

  # Run one disabled test to verify VOX_REFINE toggle logs correctly
  if [ $DISABLED_RAN -eq 0 ]; then
    out2="$(mktemp)"; err2="$(mktemp)"; trap 'rm -f "$out" "$err" "$out2" "$err2"' EXIT
    if ! cat "$txt" | VOX_REFINE=0 java -jar "$JAR" --model "$MODEL" --timeout-ms "$TIMEOUT" --memory "$MEMORY_PATH" > "$out2" 2>"$err2"; then
      echo "FAIL refine (disabled): $name (jar error)" >&2
      FAIL=$((FAIL+1))
    else
      if ! grep -q "INFO: LLM refinement disabled via VOX_REFINE=" "$err2"; then
        echo "FAIL refine (disabled): $name (missing disabled log)" >&2
        echo "stderr snippet:" >&2
        sed -n '1,8p' "$err2" >&2 || true
        FAIL=$((FAIL+1))
      else
        echo "OK refine disabled: $name"
        PASS=$((PASS+1))
        DISABLED_RAN=1
      fi
    fi
    rm -f "$out2" "$err2"
  fi

  rm -f "$out" "$err"
  trap - EXIT
done

if [ $SEEN_ANY -eq 0 ]; then
  echo "No fixtures found under $FIXDIR (expected one or more *.txt files)." >&2
  exit 2
fi

if [ $FAIL -gt 0 ]; then
  echo "Completed with failures: PASS=$PASS FAIL=$FAIL" >&2
  exit 1
fi

echo "All fixtures refined successfully: PASS=$PASS FAIL=$FAIL"

