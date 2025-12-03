# VoxCompose

Transcript refiner for [VoxCore](https://github.com/cliffmin/voxcore) with self-learning corrections and optional local LLM polish.

## Quick start
```bash
brew tap cliffmin/tap
brew install voxcompose ollama

ollama serve &
ollama pull llama3.1

echo "i want to pushto github and committhis code" | voxcompose
# → "I want to push to GitHub and commit this code"
```

## Requirements
- Java 21
- macOS 11+ (primary target; Ollama required for LLM refinement)
- Optional (golden/accuracy suite): ffmpeg/ffprobe, whisper-cpp (`whisper-cli`), jq, bc

## Features
- **Self-learning corrections**: fixes common errors without LLM calls
- **Duration-aware**: <21s uses corrections-only; long-form adds LLM
- **Fast & local**: ~140ms short-path; 100% on-device with Ollama (no API keys)
- **Privacy-first**: local by default; if you set `AI_AGENT_URL`/`OLLAMA_HOST` to remote, transcripts go there

Recent improvements (0.4.4):
- Short clips skip LLM (~90% faster for <21s)
- Upfront fixes for concatenations/tech terms (`pushto`, `committhis`, `github/json`, etc.)
- Optional caching for repeated prompts

Automatic corrections (examples):
- `pushto` → `push to`, `committhis` → `commit this`
- `github` → `GitHub`, `json` → `JSON`, `nodejs` → `Node.js`

## Usage & configuration
```bash
echo "i want to pushto github" | voxcompose --duration 10
```

| Flag | Description | Default |
| --- | --- | --- |
| `--model` | LLM model name | `llama3.1` |
| `--duration` | Input duration in seconds (guides LLM usage) | required for long/short split |
| `--memory` | JSONL preferences/glossary | - |
| `--cache` | Enable response caching | disabled |

Env vars: `AI_AGENT_MODEL`, `VOX_REFINE=0` (disable LLM), `VOX_CACHE_ENABLED=1`, `OLLAMA_HOST` (override endpoint).

## VoxCore integration
Full guide: [docs/voxcore-integration.md](docs/voxcore-integration.md)
```bash
brew install cliffmin/tap/voxcore
vim ~/.hammerspoon/ptt_config.lua
# LLM_REFINER = { ENABLED = true, CMD = { "voxcompose", "--duration" }, ... }
```

## Installation
```bash
brew tap cliffmin/tap
brew install voxcompose ollama
```
Build from source:
```bash
brew install openjdk@21 ollama
git clone https://github.com/cliffmin/voxcompose.git
cd voxcompose && ./gradlew --no-daemon clean fatJar
```

## Testing
- Unit tests: `./gradlew test`
- Integration: `./tests/run_tests.sh`
- Golden accuracy/perf (local-only, needs ffmpeg + whisper-cpp + Ollama running): `tests/run_golden.sh` (writes to `tests/results/`, gitignored)

## Upgrading
```bash
brew update && brew upgrade voxcompose
voxcompose --version
```
Data is preserved (learned profile at `~/.config/voxcompose/learned_profile.json`).
