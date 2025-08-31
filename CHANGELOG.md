# Changelog

All notable changes to this project will be documented in this file.

## 2025-08-31

- Initial VoxCompose CLI (Java 17+), local LLM refine via Ollama HTTP API
  - Reads transcript from stdin, outputs Markdown to stdout
  - Optional JSONL memory file to bias tone/glossary (last ~20 items)
- Build and packaging
  - Gradle wrapper pinned to 8.10.2; fatJar task produces `build/libs/voxcompose-0.1.0-all.jar`
  - OkHttp + Gson dependencies
- Documentation
  - README covering usage, memory JSONL, and integration with macos-ptt-dictation
- Tests and fixtures (local-only, not committed to git)
  - `tests/select_fixtures.sh` copies sample wav/json/txt from `~/Documents/VoiceNotes` into `tests/fixtures`
  - `tests/refine_fixtures.sh` runs VoxCompose over fixtures and asserts non-empty Markdown output
- .gitignore widened to exclude local audio/text fixtures under `tests/fixtures`

