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

# Growth metrics via Python helper (handles state and timestamps)
python3 - <<'PY'
import sys, json, os, platform
from pathlib import Path
from datetime import datetime, timezone, timedelta

# Reuse same data dir resolution
override=os.getenv('VOXCOMPOSE_DATA_DIR')
if override:
    data_dir=Path(override)
elif os.getenv('XDG_DATA_HOME'):
    data_dir=Path(os.environ['XDG_DATA_HOME'])/'voxcompose'
elif platform.system()=='Darwin':
    data_dir=Path.home()/'Library'/'Application Support'/'VoxCompose'
else:
    data_dir=Path.home()/'.local'/'share'/'voxcompose'

profile=data_dir/'learned_profile.json'
state_dir=(data_dir/'state') if platform.system()=='Darwin' else (Path.home()/'.local'/'state'/'voxcompose')
if os.getenv('XDG_STATE_HOME'):
    state_dir=Path(os.environ['XDG_STATE_HOME'])/'voxcompose'
if os.getenv('VOXCOMPOSE_STATE_DIR'):
    state_dir=Path(os.environ['VOXCOMPOSE_STATE_DIR'])

with open(profile,'r') as f:
    d=json.load(f)

wc=set((d.get('wordCorrections') or {}).keys())
cap=set((d.get('capitalizations') or {}).keys())
vocab=set((d.get('technicalVocabulary') or []))
pat=set((d.get('phrasePatterns') or {}).keys())

now=datetime.now(timezone.utc)
state_file=state_dir/'viewer_state.json'
state={}
if state_file.exists():
    try:
        state=json.loads(state_file.read_text())
    except Exception:
        state={}

entries=state.setdefault('entries',{
    'wordCorrections':{},'capitalizations':{},'technicalVocabulary':{},'phrasePatterns':{}
})

def reg(cat, keys):
    iso=now.isoformat()
    fs=entries.setdefault(cat,{})
    new=[]
    for k in keys:
        if k not in fs:
            fs[k]=iso
            new.append(k)
    return new

nw=reg('wordCorrections',wc)
nc=reg('capitalizations',cap)
nv=reg('technicalVocabulary',vocab)
np=reg('phrasePatterns',pat)

cut=lambda days: now - timedelta(days=days)

def count_window(cat, days):
    c=0
    for ts in entries.get(cat,{}).values():
        if ts.endswith('Z'):
            ts=ts[:-1]+"+00:00"
        try:
            dt=datetime.fromisoformat(ts)
        except Exception:
            continue
        if dt>=cut(days): c+=1
    return c

added_today={
    'wc':count_window('wordCorrections',1),
    'cap':count_window('capitalizations',1),
    'vocab':count_window('technicalVocabulary',1),
    'pat':count_window('phrasePatterns',1),
}
added_week={
    'wc':count_window('wordCorrections',7),
    'cap':count_window('capitalizations',7),
    'vocab':count_window('technicalVocabulary',7),
    'pat':count_window('phrasePatterns',7),
}

snapshots=state.setdefault('snapshots',[])
base_total=snapshots[0]['counts']['total'] if snapshots else (len(wc)+len(cap)+len(vocab)+len(pat))
snapshots.append({'ts':now.isoformat(),'counts':{'wc':len(wc),'cap':len(cap),'vocab':len(vocab),'pat':len(pat),'total':len(wc)+len(cap)+len(vocab)+len(pat)}})
state['last_run']=now.isoformat()
state_dir.mkdir(parents=True,exist_ok=True)
state_file.write_text(json.dumps(state,indent=2))

print('\n\033[1m\033[0;36m📈 Growth & Recent Additions\033[0m')
print('────────────────────────────────────────────────────────────')
print(f"  Added today (24h):     wc={added_today['wc']}  cap={added_today['cap']}  vocab={added_today['vocab']}  patterns={added_today['pat']}")
print(f"  Added last 7 days:     wc={added_week['wc']}  cap={added_week['cap']}  vocab={added_week['vocab']}  patterns={added_week['pat']}")
print(f"  Growth since tracking: +{(len(wc)+len(cap)+len(vocab)+len(pat))-base_total} total items")
if any([nw,nc,nv,np]):
    def sample(name, items):
        if items:
            print(f"  New {name} this run ({len(items)}): "+', '.join(list(items)[:10]))
    sample('word corrections', nw)
    sample('capitalizations', nc)
    sample('vocabulary terms', nv)
    sample('phrase patterns', np)
print(f"  State: {state_file}")
PY

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
