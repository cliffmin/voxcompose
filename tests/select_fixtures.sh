#!/usr/bin/env bash
set -euo pipefail

# Select 1 empty transcript sample and 4 non-empty (3 shortest + 1 longest)
# from ~/Documents/VoiceNotes that have matching .wav, .json, and .txt.
# Copy them into tests/fixtures/ for local tests (not committed).

VOICENOTES="${HOME}/Documents/VoiceNotes"
DEST_DIR="$(cd "$(dirname "$0")/.." && pwd)/fixtures"
mkdir -p "$DEST_DIR"

if [ ! -d "$VOICENOTES" ]; then
  echo "VoiceNotes directory not found: $VOICENOTES" >&2
  exit 2
fi

TMP_ALL="$(mktemp)"
trap 'rm -f "$TMP_ALL"' EXIT

# Collect candidates: dur<TAB>txtsize<TAB>wav<TAB>txt<TAB>json
find "$VOICENOTES" -type f -name "*.wav" -print0 | while IFS= read -r -d '' wav; do
  base="${wav%.wav}"
  json="$base.json"
  txt=""
  for cand in "$base.txt" "$base.en.txt" "$base.english.txt"; do
    if [ -f "$cand" ]; then txt="$cand"; break; fi
  done
  [ -n "$txt" ] || continue
  [ -f "$json" ] || continue
  ws=$(stat -f%z "$wav" 2>/dev/null || stat -c%s "$wav" 2>/dev/null || echo 0)
  ts=$(stat -f%z "$txt" 2>/dev/null || stat -c%s "$txt" 2>/dev/null || echo 0)
  # 16kHz * 2 bytes * 1 channel; ignore header 44 bytes
  dur=$(awk -v b="$ws" 'BEGIN { if (b>44) printf "%.3f", (b-44)/32000; else printf "0.000" }')
  printf "%s\t%s\t%s\t%s\t%s\n" "$dur" "$ts" "$wav" "$txt" "$json"
done | sort -n > "$TMP_ALL"

if [ ! -s "$TMP_ALL" ]; then
  echo "No matching (wav, json, txt) found under $VOICENOTES" >&2
  exit 3
fi

# Partition empty-vs-nonempty by txt size
TMP_EMPTY="$(mktemp)"; TMP_NONEMPTY="$(mktemp)"
trap 'rm -f "$TMP_EMPTY" "$TMP_NONEMPTY"' EXIT
awk -F "\t" '$2==0 {print > e} $2>0 {print > n}' e="$TMP_EMPTY" n="$TMP_NONEMPTY" "$TMP_ALL"

select_and_copy() {
  local line="$1"
  [ -n "$line" ] || return 0
  local dur ts wav txt json
  dur="$(echo "$line" | awk -F "\t" '{print $1}')"
  ts="$(echo "$line" | awk -F "\t" '{print $2}')"
  wav="$(echo "$line" | awk -F "\t" '{print $3}')"
  txt="$(echo "$line" | awk -F "\t" '{print $4}')"
  json="$(echo "$line" | awk -F "\t" '{print $5}')"
  local base
  base="$(basename "${wav%.wav}")"
  cp -p "$wav" "$DEST_DIR/$base.wav"
  cp -p "$json" "$DEST_DIR/$base.json"
  cp -p "$txt" "$DEST_DIR/$base.txt"
  echo "Added fixture: $base (dur=${dur}s, txt_bytes=$ts)"
}

# 1 empty if available
if [ -s "$TMP_EMPTY" ]; then
  select_and_copy "$(head -n1 "$TMP_EMPTY")"
fi

# 3 shortest non-empty + 1 longest non-empty
COUNT=0
if [ -s "$TMP_NONEMPTY" ]; then
  while IFS= read -r line && [ $COUNT -lt 3 ]; do
    select_and_copy "$line"
    COUNT=$((COUNT+1))
  done < "$TMP_NONEMPTY"
  # Longest non-empty
  select_and_copy "$(tail -n1 "$TMP_NONEMPTY")"
fi

echo "Fixtures prepared under: $DEST_DIR"

