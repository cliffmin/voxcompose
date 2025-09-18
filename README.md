# VoxCompose

AI-powered transcript refinement and self-learning engine for the VoxCore platform.

Status: Early alpha. A shell-based CLI shim is included for immediate use; a long-term Java CLI is planned. Releases will attach a packaged zip (voxcompose-<tag>.zip) when available.

## Features
- Intelligent refinement: context-aware cleanup and professional formatting
- Adaptive learning: improves from your corrections and usage patterns
- Fast path: sub-200ms target for common inputs
- Privacy-first: local processing by default

## Quick start
Using the temporary CLI shim:

```bash
# Basic pass-through + learning
echo "i need to pushto github and update the json api" | ./cli/voxcompose --stats

# Disable learning
cat input.txt | ./cli/voxcompose --learn off > output.txt

# Override learning data directory
cat input.txt | ./cli/voxcompose --data-dir /tmp/vxdata
```

Notes
- The shim currently passes text through unchanged but updates learning data unless --learn off or --dry-run is set.
- Python 3 is required for the learning helper scripts in tools/.

## CLI options (shim)
- --duration <seconds>
- --data-dir <path>
- --state-dir <path>
- --profile <name>
- --learn <on|off> (default on)
- --dry-run
- --stats

## Documentation
- Architecture: docs/ARCHITECTURE.md
- Performance: docs/PERFORMANCE.md
- Learning system: docs/SELF_LEARNING.md
- macOS PTT integration: docs/MACOS_PTT_INTEGRATION.md
- [Long-Term CLI Integration Plan](docs/LONG_TERM_CLI_INTEGRATION.md)
- Enterprise deployment: docs/ENTERPRISE.md
- API reference: docs/API.md

## Contributing
- PRs welcome for refinements, learning improvements, and CLI UX.
- See VoxCore development docs for contribution guidelines: https://github.com/cliffmin/voxcore/tree/main/docs/development

## License
MIT — see LICENSE.
