#!/usr/bin/env bash
# Migrate VoxCompose learned profile from legacy config path to XDG/macOS data dir
# Safe: creates target directory, makes timestamped backup, does not overwrite existing new file
# Usage: ./tools/migrate_learning_data.sh

set -euo pipefail

# Resolve target data dir
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

LEGACY_PATH="$HOME/.config/voxcompose/learned_profile.json"
TARGET_DIR="$(resolve_data_dir)"
TARGET_PATH="$TARGET_DIR/learned_profile.json"

YELLOW='\033[0;33m'
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

if [[ ! -f "$LEGACY_PATH" ]]; then
  echo -e "${RED}Legacy profile not found:${NC} $LEGACY_PATH"
  echo -e "${YELLOW}Nothing to migrate.${NC}"
  exit 0
fi

mkdir -p "$TARGET_DIR"

if [[ -f "$TARGET_PATH" ]]; then
  echo -e "${YELLOW}Target already exists:${NC} $TARGET_PATH"
  ts=$(date +%Y%m%d-%H%M%S)
  backup="$TARGET_PATH.backup-$ts"
  echo -e "${YELLOW}Creating backup instead:${NC} $backup"
  cp "$LEGACY_PATH" "$backup"
  echo -e "${GREEN}Backup created.${NC}"
  exit 0
fi

cp "$LEGACY_PATH" "$TARGET_PATH"

echo -e "${GREEN}Migrated learned profile:${NC}"
echo "  from: $LEGACY_PATH"
echo "  to  : $TARGET_PATH"

echo -e "${YELLOW}You can delete the legacy file once you verify:${NC}"
echo "  rm \"$LEGACY_PATH\""
