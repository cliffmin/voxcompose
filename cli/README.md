# VoxCompose CLI (Temporary Shim)

This is a temporary shell-based CLI that enables immediate integration while the long-term Java client is developed.

Usage
```bash
# Basic pass-through + learning
echo "i need to pushto github and update the json api" | ./cli/voxcompose --stats

# Disable learning
cat input.txt | ./cli/voxcompose --learn off > output.txt

# Override data dir
cat input.txt | ./cli/voxcompose --data-dir /tmp/vxdata
```

Behavior
- Reads stdin, writes the same text to stdout unchanged
- Updates learning via tools/learn_from_text.py unless --learn off or --dry-run
- Emits basic JSON stats (stderr) with --stats

Note
- This is a bridge. The long-term Java CLI will replace it and handle corrections inline.