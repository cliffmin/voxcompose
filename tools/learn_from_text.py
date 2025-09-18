#!/usr/bin/env python3
"""
Minimal learner hook for VoxCompose.
- Reads transcript text from stdin
- Updates learned_profile.json under the data dir
- Prints nothing (acts as a side-effect hook)

Data dir precedence:
1) VOXCOMPOSE_DATA_DIR
2) $XDG_DATA_HOME/voxcompose
3) macOS: ~/Library/Application Support/VoxCompose
4) Linux/other: ~/.local/share/voxcompose

This is a temporary, minimal solution so learning updates happen in current workflows
(e.g., macOS PTT) without a full CLI. Heavy logic will live in the future Java-based CLI.
"""
import json
import os
import platform
import sys
from pathlib import Path
from typing import Dict, Any


def resolve_data_dir() -> Path:
    override = os.getenv("VOXCOMPOSE_DATA_DIR")
    if override:
        return Path(override)
    xdg = os.getenv("XDG_DATA_HOME")
    if xdg:
        return Path(xdg) / "voxcompose"
    if platform.system() == "Darwin":
        return Path.home() / "Library" / "Application Support" / "VoxCompose"
    return Path.home() / ".local" / "share" / "voxcompose"


def load_profile(path: Path) -> Dict[str, Any]:
    if path.exists():
        try:
            return json.loads(path.read_text())
        except Exception:
            pass
    return {
        "wordCorrections": {},
        "capitalizations": {},
        "technicalVocabulary": [],
        "phrasePatterns": {},
    }


essential_caps = [
    ("api", "API"),
    ("json", "JSON"),
    ("http", "HTTP"),
    ("url", "URL"),
    ("github", "GitHub"),
    ("nodejs", "Node.js"),
    ("postgresql", "PostgreSQL"),
    ("kubernetes", "Kubernetes"),
    ("docker", "Docker"),
    ("redis", "Redis"),
    ("graphql", "GraphQL"),
    ("rest", "REST"),
]

basic_splits = [
    ("pushto", "push to"),
    ("committhis", "commit this"),
    ("followup", "follow up"),
    ("setup", "set up"),
    ("signin", "sign in"),
    ("signout", "sign out"),
    ("login", "log in"),
    ("logout", "log out"),
    ("frontend", "front end"),
    ("backend", "back end"),
    ("dropdown", "drop down"),
    ("builtin", "built in"),
]


def apply_learning(text: str, profile: Dict[str, Any]) -> bool:
    """Apply trivial learning rules based on observed text. Returns True if modified."""
    changed = False
    text_l = text.lower()

    caps = profile.setdefault("capitalizations", {})
    vocab = set(profile.setdefault("technicalVocabulary", []))
    splits = profile.setdefault("wordCorrections", {})

    for low, proper in essential_caps:
        if low in text_l and caps.get(low) != proper:
            caps[low] = proper
            vocab.add(proper)
            changed = True

    for bad, good in basic_splits:
        if bad in text_l and splits.get(bad) != good:
            splits[bad] = good
            changed = True

    if changed:
        profile["technicalVocabulary"] = sorted(vocab)
    return changed


def atomic_write(path: Path, data: Dict[str, Any]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    tmp = path.with_suffix(path.suffix + ".tmp")
    tmp.write_text(json.dumps(data, indent=2))
    tmp.replace(path)


def main() -> int:
    try:
        raw = sys.stdin.read()
    except Exception:
        return 0  # nothing to learn
    if not raw.strip():
        return 0

    data_dir = resolve_data_dir()
    profile_path = data_dir / "learned_profile.json"
    profile = load_profile(profile_path)

    if apply_learning(raw, profile):
        atomic_write(profile_path, profile)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())