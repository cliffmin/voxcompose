#!/usr/bin/env bash
# Wrapper to ensure python3 is invoked with a file argument while reading stdin
# Usage: python3_wrapper.sh /path/to/script.py
set -euo pipefail
python3 "$1" >/dev/null