#!/bin/bash

# Show VoxCompose self-learning data in a readable format
# Usage: ./show_learning_data.sh

set -euo pipefail

# Resolve data directory with precedence:
# 1) VOXCOMPOSE_DATA_DIR
# 2) XDG_DATA_HOME/voxcompose
# 3) macOS: ~/Library/Application Support/VoxCompose
# 4) Linux/other: ~/.local/share/voxcompose
resolve_data_dir() {
  if [[ -n "${VOXCOMPOSE_DATA_DIR:-}" ]]; then
    echo "$VOXCOMPOSE_DATA_DIR"
    return
  fi
  if [[ -n "${XDG_DATA_HOME:-}" ]]; then
    echo "$XDG_DATA_HOME/voxcompose"
    return
  fi
  local uname_s
  uname_s=$(uname -s 2>/dev/null || echo "")
  if [[ "$uname_s" == "Darwin" ]]; then
    echo "$HOME/Library/Application Support/VoxCompose"
  else
    echo "$HOME/.local/share/voxcompose"
  fi
}

DATA_DIR="$(resolve_data_dir)"
PROFILE_PATH="$DATA_DIR/learned_profile.json"
LEGACY_PATH="$HOME/.config/voxcompose/learned_profile.json"

# Require new location only; suggest migration if legacy exists
if [[ ! -f "$PROFILE_PATH" ]]; then
  echo -e "\033[0;31mNo learning profile found at:${NC} $PROFILE_PATH\033[0m"
  if [[ -f "$LEGACY_PATH" ]]; then
    echo -e "\033[0;33mLegacy profile detected at: $LEGACY_PATH\033[0m"
    echo -e "\033[0;33mPlease migrate using: tools/migrate_learning_data.sh\033[0m"
  else
    echo -e "\033[0;33mTip: Run VoxCompose to generate learning data, or set VOXCOMPOSE_DATA_DIR\033[0m"
  fi
  exit 1
fi

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[0;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m' # No Color

echo -e "${BOLD}${CYAN}════════════════════════════════════════════════════════════════${NC}"
echo -e "${BOLD}${CYAN}          VoxCompose Self-Learning Data Viewer${NC}"
echo -e "${BOLD}${CYAN}════════════════════════════════════════════════════════════════${NC}"
echo

# Function to display a section
display_section() {
    local section=$1
    local title=$2
    local format=$3
    
    echo -e "${BOLD}${BLUE}$title${NC}"
    echo -e "${BLUE}$(printf '─%.0s' {1..60})${NC}"
    
    if [ "$format" = "map" ]; then
        # Display as key -> value mapping
        python3 -c "
import json
import sys

with open('$PROFILE_PATH', 'r') as f:
    data = json.load(f)
    
if '$section' in data and data['$section']:
    items = data['$section']
    if isinstance(items, dict):
        # Sort by key for consistent display
        for key in sorted(items.keys()):
            value = items[key]
            print(f'  {key:20s} → {value}')
    else:
        print('  No data')
else:
    print('  No corrections learned yet')
"
    elif [ "$format" = "list" ]; then
        # Display as a list
        python3 -c "
import json
import sys

with open('$PROFILE_PATH', 'r') as f:
    data = json.load(f)
    
if '$section' in data and data['$section']:
    items = data['$section']
    if isinstance(items, list):
        # Display in columns
        cols = 3
        for i in range(0, len(items), cols):
            row = items[i:i+cols]
            print('  ' + ''.join(f'{item:25s}' for item in row))
    else:
        print('  No data')
else:
    print('  No vocabulary learned yet')
"
    elif [ "$format" = "stats" ]; then
        # Display statistics
        python3 -c "
import json
from datetime import datetime

with open('$PROFILE_PATH', 'r') as f:
    data = json.load(f)

# Count items in each category
word_count = len(data.get('wordCorrections', {}))
cap_count = len(data.get('capitalizations', {}))
vocab_count = len(data.get('technicalVocabulary', []))
phrase_count = len(data.get('phrasePatterns', {}))

print(f'  Word Corrections:     {word_count:4d} entries')
print(f'  Capitalizations:      {cap_count:4d} entries')
print(f'  Technical Vocabulary: {vocab_count:4d} terms')
print(f'  Phrase Patterns:      {phrase_count:4d} patterns')
print()
print(f'  Total Learning Items: {word_count + cap_count + vocab_count + phrase_count:4d}')

# Check for metadata
if 'metadata' in data:
    meta = data['metadata']
    if 'lastUpdated' in meta:
        print(f\"  Last Updated: {meta['lastUpdated']}\")
    if 'sessionCount' in meta:
        print(f\"  Sessions: {meta['sessionCount']}\")
"
    fi
    echo
}

# Display summary statistics
display_section "stats" "📊 Learning Statistics" "stats"

# Display word corrections
display_section "wordCorrections" "📝 Word Corrections" "map"

# Display capitalizations
display_section "capitalizations" "🔤 Capitalization Rules" "map"

# Display technical vocabulary
display_section "technicalVocabulary" "💻 Technical Vocabulary" "list"

# Display phrase patterns if they exist
python3 -c "
import json
with open('$PROFILE_PATH', 'r') as f:
    data = json.load(f)
    if 'phrasePatterns' in data and data['phrasePatterns']:
        exit(0)
    else:
        exit(1)
" 2>/dev/null && {
    display_section "phrasePatterns" "🔗 Phrase Patterns" "map"
}

# Show file location
echo -e "${BOLD}${GREEN}Profile Location:${NC} $PROFILE_PATH"

# Show export option
echo -e "${YELLOW}Tips:${NC}"
echo "• To export as JSON: cat $PROFILE_PATH | jq"
echo "• To backup: cp $PROFILE_PATH ~/voxcompose_learning_backup.json"
echo "• To reset learning: rm $PROFILE_PATH"
echo
