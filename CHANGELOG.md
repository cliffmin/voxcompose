# Changelog

All notable changes to this project will be documented in this file.

The format is based on Keep a Changelog, and this project adheres to Semantic Versioning.

## Unreleased
- Add GitHub Actions release workflow to build and upload the fat jar on tag push (v*), with the sha256 printed into the Release body.
- Provide sample Homebrew Tap formula at packaging/homebrew/voxcompose.rb and README instructions for installation.
- Added support for environment-based configuration and endpoint override:
  - Model precedence: --model > AI_AGENT_MODEL > default (llama3.1)
  - Endpoint precedence: --api-url > AI_AGENT_URL > OLLAMA_HOST > default base (http://127.0.0.1:11434), automatically appending /api/generate when missing
- Added --api-url CLI flag to override endpoint
- Added additional logs for effective model/endpoint and their sources
- Sidecar now includes endpoint, model_source, and endpoint_source fields
- Updated README to document configuration and precedence

## 0.1.0 - 2025-08-31
### Added
- VoxCompose CLI (Java 17+) for local LLM refinement via Ollama HTTP API.
  - Reads transcript from stdin; outputs Markdown to stdout.
  - Optional JSONL memory file to bias tone/glossary (last ~20 items).
- Build and packaging:
  - Gradle wrapper pinned to 8.10.2; `fatJar` task produces `build/libs/voxcompose-0.1.0-all.jar`.
  - OkHttp + Gson dependencies.
- Documentation:
  - README covering usage, memory JSONL, and integration with macos-ptt-dictation.
- Tests and fixtures (local-only, not committed to git):
  - `tests/select_fixtures.sh` to collect sample artifacts.
  - `tests/refine_fixtures.sh` to assert Markdown output and logging.
- .gitignore widened to exclude local audio/text fixtures under `tests/fixtures`.

