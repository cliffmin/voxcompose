# VoxCompose


> ## Archived · 2026-08-26
>
> **This project is no longer developed.** Work has moved to **SelfTalk** — a rebuild of
> the same idea (press a key, speak, get your words at the cursor) with a deliberately
> smaller scope and the lessons this line of work paid for written down as constraints
> rather than rediscovered.
>
> **What carried forward: the measurements and the failures, not the code.** SelfTalk
> starts from an empty repository. Nothing here is maintained; issues and pull requests
> will not be answered.
>
> **Why this one stopped.** Three failures from this project are now permanent rules
> elsewhere. A dangling symlink after a directory move killed the hotkey **silently for
> five weeks** — `require` failed at startup and nothing a user would look at said so.
> A zero-byte vocabulary file changed decoding by its *presence*, not its contents, and
> removing it was worth 6.7 points of word error rate. And the benchmark compared two
> word lists by index, so a single inserted word zeroed everything after it — a transcript
> whose only defect was `Vox Core` for `VoxCore` scored 0%.
>
> The rules those became: fail loudly or not at all; presence is not content; never score
> by index.

---

Self-learning transcript refinement for [VoxCore](https://github.com/cliffmin/voxcore). Automatically fixes concatenations, capitalizes technical terms, and optionally applies local LLM polish -- all on-device, no API keys required.

*Part of the [VoxCore ecosystem](https://github.com/cliffmin/voxcore#ecosystem) for local voice-to-text on macOS.*

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
- **Universal hotkey → any app**: speak once, paste into ChatGPT/Claude/Cursor/email/docs.
- **Never lose work**: every capture is saved locally; failures don’t cost you recordings.
- **Self-learning & adaptive**: corrections improve with your speech patterns; fixes concatenations/tech terms.
- **Duration-aware**: <21s uses fast corrections-only; longer adds LLM refinement for clarity.
- **Fast & local**: ~140ms short-path; 100% on-device with Ollama (no API keys); privacy-first unless you point at a remote `AI_AGENT_URL`/`OLLAMA_HOST`.

Recent improvements (v1.0.0):
- Self-learning corrections system (100% correction rate for word concatenations)
- Duration-aware processing: fast corrections-only for <21s, full LLM for longer clips
- Capabilities negotiation with VoxCore (returns learned threshold)
- Connection pooling, response caching, buffered I/O for performance

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

## Ecosystem

VoxCompose is a plugin in the VoxCore ecosystem. Install VoxCore first, then optionally add VoxCompose for adaptive LLM refinement.

| Component | Role | Links |
|-----------|------|-------|
| **[VoxCore](https://github.com/cliffmin/voxcore)** | Core push-to-talk engine | [Setup](https://github.com/cliffmin/voxcore/tree/main/docs/setup) · [Architecture](https://github.com/cliffmin/voxcore/blob/main/docs/development/architecture.md) |
| **VoxCompose** (this repo) | Self-learning transcript refinement plugin | [Docs](docs/) · [Integration guide](docs/voxcore-integration.md) |
| **[homebrew-tap](https://github.com/cliffmin/homebrew-tap)** | Homebrew distribution for both | `brew tap cliffmin/tap` |

## Documentation

- [VoxCore Integration](docs/voxcore-integration.md) — connecting to VoxCore push-to-talk
- [Architecture](docs/architecture.md) — system design and pipeline
- [Self-Learning](docs/self-learning.md) — how corrections accumulate
- [Performance](docs/performance.md) — benchmarks and tuning

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

## Upgrading
```bash
brew update && brew upgrade voxcompose
voxcompose --version
```
Data is preserved (learned profile at `~/.config/voxcompose/learned_profile.json`).
