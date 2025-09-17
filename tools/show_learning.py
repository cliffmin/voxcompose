#!/usr/bin/env python3
"""
Quick viewer for VoxCompose self-learning data
Usage: python3 show_learning.py [--json|--summary|--corrections|--vocab]
"""

import json
import sys
import os
import platform
from pathlib import Path

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
        else:
            print("Usage: python3 show_learning.py [--json|--summary|--corrections|--vocab]")
            sys.exit(1)
    else:
        # Default: show everything in a nice format
        show_summary(data)
        show_corrections(data)
        show_vocabulary(data)
        
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

if __name__ == "__main__":
    main()