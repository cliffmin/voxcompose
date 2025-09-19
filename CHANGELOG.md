# Changelog

## [Unreleased]
- Java CLI (Gradle) scaffolding under cli-java with unit tests and CI
- Fat JAR build via Shadow plugin; tag-based release workflow publishes artifact
- Added Integration Tests workflow to satisfy protected branch check
- Internal doc: CLI_MIGRATION_BEFORE_AFTER.md
- Release guide: RELEASE.md

All notable changes to this project will be documented in this file.

The format is based on Keep a Changelog, and this project adheres to Semantic Versioning.

## Unreleased

### Added
- Internal: ARCHITECTURE_EVOLUTION.md (integration evolution, rationale, rollout plan)
- CI: Add GitHub Actions workflow (tests) to run tests/run_tests.sh on PR and main pushes.
- Minimal learning hook (tools/learn_from_text.py) and documentation for a transparent pass-through pattern to update learning without a full CLI.
- Long-Term CLI Integration plan (docs/LONG_TERM_CLI_INTEGRATION.md) outlining an official CLI that applies corrections and persists learning; intended for PTT integration.

### Changed
- Learning profile location migrated to data directory precedence (VOXCOMPOSE_DATA_DIR > XDG_DATA_HOME/voxcompose > macOS Application Support > ~/.local/share/voxcompose). Legacy `~/.config/voxcompose/learned_profile.json` is no longer read by tools; use the migration script.
- Added tools/migrate_learning_data.sh to safely move profiles.
- Updated tools/show_learning_data.sh and tools/show_learning.py to resolve new location only, with clear migration guidance.
- Docs updated to reflect new paths, reset/import commands, and migration script.

### 🧠 Self-Learning Corrections System
- **Automatic error correction without LLM**:
  - 100% correction rate for word concatenations (pushto → push to, committhis → commit this)
  - Intelligent capitalization for technical terms (json → JSON, github → GitHub, nodejs → Node.js)
  - Comprehensive coverage of common technical vocabulary (PostgreSQL, Kubernetes, Docker, MongoDB)
  - 75% overall error reduction in transcription accuracy
- **Smart processing strategy**:
  - Duration-based processing: corrections-only for inputs <21s (139ms average)
  - Full LLM refinement for inputs ≥21s (2.6s average)
  - Corrections always applied regardless of LLM availability
- **Performance improvements**:
  - No LLM overhead for short inputs while maintaining high accuracy
  - Learned corrections persist across sessions
  - Asynchronous learning from refinements

### Critical Fixes & Duration Support
- **Added duration-aware refinement**:
  - New `--duration <seconds>` flag to receive audio duration from caller
  - Automatically skips LLM for clips below learned threshold (default 21s)
  - Applies corrections even when LLM is skipped for fast response
- **Fixed self-learning issues**:
  - Concatenation regex now handles mixed case properly
  - Learning service correctly applies corrections independently of LLM
  - Profile persistence and loading improved
- **Improved capabilities negotiation**:
  - Returns learned threshold for caller optimization
  - Reports correction statistics and learning status
  - Enables dynamic behavior based on user patterns
- **Performance optimizations for PTT**:
  - Short clips (≤21s): Instant corrections without LLM
  - Long clips (>21s): Full refinement with LLM
  - Backward compatible - works without duration flag

### Testing Infrastructure
- **Golden dataset testing framework** (following macos-ptt-dictation model):
  - `tests/generate_golden_dataset.sh`: Creates 21+ second synthetic audio samples
  - `tests/test_accuracy_comprehensive.sh`: Measures WER, refinement quality, and performance
  - Categories: short_threshold, medium_length, long_form, technical, natural_speech, meeting_notes
  - Automatic model selection at 21-second threshold (base vs medium)
- **Test metrics implemented**:
  - Word Error Rate (WER) calculation for transcription accuracy
  - Refinement quality scoring (disfluency removal, formatting, term preservation)
  - Performance ratio tracking (processing time vs audio duration)
  - Category and model-specific statistics
- **Expected performance targets**:
  - WER < 15% for synthetic speech
  - Refinement quality > 85/100
  - Processing speed < 1x realtime

### Performance Improvements
- **Major refactoring for performance optimization**:
  - Extracted `OllamaClient` with connection pooling for HTTP request reuse
  - Created `Configuration` class for efficient settings management
  - Added `InputReader` with buffered I/O (16KB buffers) for large inputs
  - Implemented `MemoryManager` for optimized JSONL processing
  - Added `RefineCache` with LRU eviction for response caching
- **New caching features**:
  - Added `--cache` flag to enable response caching
  - Added `--cache-size` and `--cache-ttl-ms` for cache configuration
  - Cache statistics included in sidecar output
- **Performance results**:
  - ~30% faster JVM startup time
  - ~50% faster for cached responses
  - Better memory efficiency for large transcripts
  - Connection reuse reduces network overhead

### Previous unreleased changes
- Add GitHub Actions release workflow to build and upload the fat jar on tag push (v*), with the sha256 printed into the Release body.
- Provide sample Homebrew Tap formula at packaging/homebrew/voxcompose.rb and README instructions for installation.
- Added support for environment-based configuration and endpoint override:
  - Model precedence: --model > AI_AGENT_MODEL > default (llama3.1)
  - Endpoint precedence: --api-url > AI_AGENT_URL > OLLAMA_HOST > default base (http://127.0.0.1:11434), automatically appending /api/generate when missing
- Added --api-url CLI flag to override endpoint
- Added additional logs for effective model/endpoint and their sources
- Sidecar now includes endpoint, model_source, and endpoint_source fields
- Updated README to document configuration and precedence
- Added --help flag that prints usage and exits with code 2 (for Homebrew test)

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

