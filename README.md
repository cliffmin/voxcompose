# VoxCompose

**Transcript refinement plugin for [VoxCore](https://github.com/cliffmin/voxcore)**

VoxCompose refines voice transcripts using self-learning corrections and optional local LLM processing. Works standalone or as part of the VoxCore push-to-talk workflow.

## Features

- **Self-learning corrections** — Fixes common transcription errors without LLM calls
- **Smart processing** — Corrections-only for short inputs (<21s), adds LLM for longer content
- **Privacy-first** — 100% local processing with Ollama, no API keys
- **Fast** — ~140ms for short inputs, 2.6s with LLM refinement

## Automatic Corrections

**Word concatenations:** `pushto` → `push to`, `committhis` → `commit this`

**Technical terms:** `github` → `GitHub`, `json` → `JSON`, `nodejs` → `Node.js`

## 🚀 Quick Start

```bash
# Install via Homebrew
brew tap cliffmin/tap
brew install voxcompose ollama

# Start Ollama and pull a model
ollama serve &
ollama pull llama3.1

# Test it
echo "i want to pushto github and committhis code" | voxcompose
# Output: "I want to push to GitHub and commit this code"
```

## Configuration

| Option | Description | Default |
|--------|-------------|------|
| `--model` | LLM model name | `llama3.1` |
| `--duration` | Input duration in seconds | - |
| `--memory` | JSONL file with preferences/glossary | - |
| `--cache` | Enable response caching | disabled |

**Environment variables:** `AI_AGENT_MODEL`, `VOX_REFINE=0` (disable LLM), `VOX_CACHE_ENABLED=1`

## VoxCore Integration

See [docs/voxcore-integration.md](docs/voxcore-integration.md) for setup with push-to-talk.

## Installation

```bash
brew tap cliffmin/tap
brew install voxcompose ollama
```

<details>
<summary>Build from source</summary>

```bash
brew install openjdk@21 ollama
git clone https://github.com/cliffmin/voxcompose.git
cd voxcompose && ./gradlew --no-daemon clean fatJar
```
</details>

## Testing

```bash
./gradlew test          # Java unit tests
./tests/run_tests.sh    # Integration tests
```
