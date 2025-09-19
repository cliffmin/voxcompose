# VoxCompose – VoxCore Intelligence Layer

The AI‑powered transcript refinement engine for the VoxCore voice intelligence platform.


## Overview

VoxCompose CLI provides fast pass-through corrections and local self-learning. See Install via Homebrew and Using the CLI below.

## Using the Java CLI

Quickstart

```bash
TMP=$(mktemp -d)
echo "i need to pushto github and update the json api" | \
  voxcompose --data-dir "$TMP" --stats
```

- Input is echoed to stdout unchanged
- learned_profile.json is written under $TMP with essential caps and splits
- --stats emits a JSON line to stderr with basic metrics

Data directory precedence
- VOXCOMPOSE_DATA_DIR
- $XDG_DATA_HOME/voxcompose
- macOS: ~/Library/Application Support/VoxCompose
- Linux: ~/.local/share/voxcompose
```

## Documentation & Resources

- Release Guide: `docs/RELEASE.md`
- CHANGELOG: `CHANGELOG.md`


## Contributing

Improvements or clarifications to the documentation are welcome.

- See `CONTRIBUTING.md` for contribution guidelines.
- See `CODE_OF_CONDUCT.md` for community standards.
- See `SECURITY.md` for vulnerability reporting.

See VoiceCore Platform Development for contribution guidelines:
- https://github.com/cliffmin/voxcore/tree/main/docs/development

## License

MIT — See `LICENSE`.

---

All metrics, versions, and examples are illustrative and do not represent a shipped product.
