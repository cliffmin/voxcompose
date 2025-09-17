#!/usr/bin/env python3
"""
Quick viewer for VoxCompose self-learning data
Usage: python3 show_learning.py [--json|--summary|--corrections|--vocab|--growth]
"""

import json
import sys
import os
import platform
from pathlib import Path
from datetime import datetime, timedelta, timezone

def utcnow_iso():
    return datetime.now(timezone.utc).isoformat()

def parse_iso(ts: str) -> datetime:
    try:
        # Handle trailing Z or timezone-aware ISO
        if ts.endswith('Z'):
            ts = ts[:-1] + '+00:00'
        return datetime.fromisoformat(ts)
    except Exception:
        return datetime.now(timezone.utc)

def resolve_data_dir() -> Path:
    """Resolve the data directory using precedence:
    1) VOXCOMPOSE_DATA_DIR
    2) XDG_DATA_HOME/voxcompose
    3) macOS: ~/Library/Application Support/VoxCompose
    4) Linux/other: ~/.local/share/voxcompose
    """
    override = os.getenv('VOXCOMPOSE_DATA_DIR')
    if override:
        return Path(override)

    xdg = os.getenv('XDG_DATA_HOME')
    if xdg:
        return Path(xdg) / 'voxcompose'

    system = platform.system()
    if system == 'Darwin':
        return Path.home() / 'Library' / 'Application Support' / 'VoxCompose'
    else:
        return Path.home() / '.local' / 'share' / 'voxcompose'

def resolve_state_dir() -> Path:
    """Resolve a state directory for viewer bookkeeping (timestamps, snapshots)."""
    override = os.getenv('VOXCOMPOSE_STATE_DIR')
    if override:
        return Path(override)
    xdg_state = os.getenv('XDG_STATE_HOME')
    if xdg_state:
        return Path(xdg_state) / 'voxcompose'
    # macOS: use Application Support/VoxCompose/state; Linux: ~/.local/state/voxcompose
    if platform.system() == 'Darwin':
        return Path.home() / 'Library' / 'Application Support' / 'VoxCompose' / 'state'
    else:
        return Path.home() / '.local' / 'state' / 'voxcompose'

def load_profile():
    """Load the learning profile from the new data location only."""
    path = resolve_data_dir() / 'learned_profile.json'
    if not path.exists():
        print(f"No learning profile found at: {path}")
        legacy = Path.home() / '.config' / 'voxcompose' / 'learned_profile.json'
        if legacy.exists():
            print(f"Legacy profile detected at: {legacy}")
            print("Please migrate using tools/migrate_learning_data.sh")
        else:
            print("Run VoxCompose with corrections to build learning data or set VOXCOMPOSE_DATA_DIR.")
        sys.exit(1)
    with open(path, 'r') as f:
        data = json.load(f)
    # Attach metadata so we can print path later
    data.setdefault('_meta', {})
    data['_meta']['path'] = str(path)
    data['_meta']['legacy'] = False
    return data

def show_summary(data):
    """Show summary statistics"""
    print("\n📊 LEARNING SUMMARY")
    print("=" * 50)
    print(f"Word Corrections:     {len(data.get('wordCorrections', {})):4d} entries")
    print(f"Capitalizations:      {len(data.get('capitalizations', {})):4d} entries")
    print(f"Technical Vocabulary: {len(data.get('technicalVocabulary', [])):4d} terms")
    print(f"Phrase Patterns:      {len(data.get('phrasePatterns', {})):4d} patterns")
    print("-" * 50)
    total = sum([
        len(data.get('wordCorrections', {})),
        len(data.get('capitalizations', {})),
        len(data.get('technicalVocabulary', [])),
        len(data.get('phrasePatterns', {}))
    ])
    print(f"Total Learning Items: {total:4d}")

def show_corrections(data):
    """Show all corrections in a compact format"""
    print("\n📝 CORRECTIONS")
    print("=" * 50)
    
    # Word corrections
    if data.get('wordCorrections'):
        print("\nWord Corrections:")
        for orig, corrected in sorted(data['wordCorrections'].items()):
            print(f"  {orig:20s} → {corrected}")
    
    # Capitalizations
    if data.get('capitalizations'):
        print("\nCapitalizations:")
        for orig, corrected in sorted(data['capitalizations'].items()):
            print(f"  {orig:20s} → {corrected}")
    
    # Phrase patterns
    if data.get('phrasePatterns'):
        print("\nPhrase Patterns:")
        for pattern, replacement in sorted(data['phrasePatterns'].items()):
            print(f"  {pattern:20s} → {replacement}")

def show_vocabulary(data):
    """Show technical vocabulary in columns"""
    print("\n💻 TECHNICAL VOCABULARY")
    print("=" * 50)
    
    vocab = data.get('technicalVocabulary', [])
    if vocab:
        # Sort and display in 3 columns
        vocab_sorted = sorted(vocab)
        cols = 3
        for i in range(0, len(vocab_sorted), cols):
            row = vocab_sorted[i:i+cols]
            print("  " + "".join(f"{term:25s}" for term in row))
    else:
        print("  No vocabulary learned yet")

def load_state() -> dict:
    path = resolve_state_dir() / 'viewer_state.json'
    if path.exists():
        try:
            with open(path, 'r') as f:
                return json.load(f)
        except Exception:
            return {}
    return {}

def save_state(state: dict) -> None:
    p = resolve_state_dir()
    p.mkdir(parents=True, exist_ok=True)
    with open(p / 'viewer_state.json', 'w') as f:
        json.dump(state, f, indent=2)

def compute_growth(data: dict) -> dict:
    # Build current key sets
    wc = set((data.get('wordCorrections') or {}).keys())
    cap = set((data.get('capitalizations') or {}).keys())
    vocab = set((data.get('technicalVocabulary') or []))
    pat = set((data.get('phrasePatterns') or {}).keys())

    now_iso = utcnow_iso()
    state = load_state()
    entries = state.setdefault('entries', {
        'wordCorrections': {},
        'capitalizations': {},
        'technicalVocabulary': {},
        'phrasePatterns': {}
    })

    # Helper to register first-seen timestamps
    def register(cat: str, keys: set[str]):
        first_seen = entries.setdefault(cat, {})
        new_keys = []
        for k in keys:
            if k not in first_seen:
                first_seen[k] = now_iso
                new_keys.append(k)
        return new_keys

    new_wc = register('wordCorrections', wc)
    new_cap = register('capitalizations', cap)
    new_vocab = register('technicalVocabulary', vocab)
    new_pat = register('phrasePatterns', pat)

    # Counts per window
    def in_window(cat: str, days: int) -> int:
        cutoff = datetime.now(timezone.utc) - timedelta(days=days)
        cnt = 0
        for ts in entries.get(cat, {}).values():
            if parse_iso(ts) >= cutoff:
                cnt += 1
        return cnt

    today = {
        'wordCorrections': in_window('wordCorrections', 1),
        'capitalizations': in_window('capitalizations', 1),
        'technicalVocabulary': in_window('technicalVocabulary', 1),
        'phrasePatterns': in_window('phrasePatterns', 1),
    }
    week = {
        'wordCorrections': in_window('wordCorrections', 7),
        'capitalizations': in_window('capitalizations', 7),
        'technicalVocabulary': in_window('technicalVocabulary', 7),
        'phrasePatterns': in_window('phrasePatterns', 7),
    }

    # Snapshot counts
    total_now = len(wc) + len(cap) + len(vocab) + len(pat)
    snapshots = state.setdefault('snapshots', [])
    base_total = snapshots[0]['counts']['total'] if snapshots else total_now
    snapshots.append({
        'ts': now_iso,
        'counts': {
            'wordCorrections': len(wc),
            'capitalizations': len(cap),
            'technicalVocabulary': len(vocab),
            'phrasePatterns': len(pat),
            'total': total_now
        }
    })
    state['last_run'] = now_iso
    save_state(state)

    return {
        'new_this_run': {
            'wordCorrections': new_wc,
            'capitalizations': new_cap,
            'technicalVocabulary': new_vocab,
            'phrasePatterns': new_pat,
        },
        'added_today': today,
        'added_week': week,
        'growth_since_first_tracking': total_now - base_total,
        'snapshot_count': len(snapshots),
        'state_path': str(resolve_state_dir() / 'viewer_state.json')
    }

def show_growth(data: dict) -> None:
    g = compute_growth(data)
    print("\n📈 GROWTH & RECENT ADDITIONS")
    print("=" * 50)
    print(f"Added today (24h):     wc={g['added_today']['wordCorrections']}  cap={g['added_today']['capitalizations']}  vocab={g['added_today']['technicalVocabulary']}  patterns={g['added_today']['phrasePatterns']}")
    print(f"Added last 7 days:     wc={g['added_week']['wordCorrections']}  cap={g['added_week']['capitalizations']}  vocab={g['added_week']['technicalVocabulary']}  patterns={g['added_week']['phrasePatterns']}")
    print(f"Growth since tracking: +{g['growth_since_first_tracking']} total items")

    # Show last batch new keys (up to 10 per category)
    def list_first_n(name: str, keys: list[str], n: int = 10):
        if keys:
            sample = ", ".join(list(keys)[:n])
            print(f"New {name} this run ({len(keys)}): {sample}")
    list_first_n('word corrections', g['new_this_run']['wordCorrections'])
    list_first_n('capitalizations', g['new_this_run']['capitalizations'])
    list_first_n('vocabulary terms', g['new_this_run']['technicalVocabulary'])
    list_first_n('phrase patterns', g['new_this_run']['phrasePatterns'])

    print(f"State: {g['state_path']}")

def main():
    """Main entry point"""
    data = load_profile()
    
    # Check command line arguments
    if len(sys.argv) > 1:
        arg = sys.argv[1]
        if arg == '--json':
            # Pretty print JSON
            print(json.dumps(data, indent=2))
        elif arg == '--summary':
            show_summary(data)
        elif arg == '--corrections':
            show_corrections(data)
        elif arg == '--vocab':
            show_vocabulary(data)
        elif arg == '--growth':
            show_growth(data)
        else:
            print("Usage: python3 show_learning.py [--json|--summary|--corrections|--vocab|--growth]")
            sys.exit(1)
    else:
        # Default: show everything in a nice format
        show_summary(data)
        show_corrections(data)
        show_vocabulary(data)
        show_growth(data)
        
        # Show file location
        meta = data.get('_meta', {})
        profile_path = meta.get('path')
        if profile_path:
            print(f"\n📁 Profile: {profile_path}")
        print("\nOptions:")
        print("  --json        : Export as formatted JSON")
        print("  --summary     : Show only statistics")
        print("  --corrections : Show only corrections")
        print("  --vocab       : Show only vocabulary")
        print("  --growth      : Show growth and recent additions")

if __name__ == "__main__":
    main()
    main()