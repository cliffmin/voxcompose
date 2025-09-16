# VoxCompose

**Smart transcript refinement with self-learning corrections and local LLM processing**

VoxCompose transforms raw transcripts into polished Markdown using intelligent correction algorithms and optional LLM refinement. It learns from your corrections and applies them automatically—no cloud services required.

### 🏆 Major Achievements in v0.3.0

| Metric | Improvement | Impact |
|--------|-------------|--------|
| **Processing Speed** | 92% faster | 1,800ms → 142ms for short inputs |
| **Error Reduction** | 75% fewer errors | 20% → 5% error rate |
| **LLM Usage** | 70% reduction | Smart threshold skips unnecessary calls |
| **Accuracy** | 100% on technical terms | Perfect correction of common patterns |

## ✨ Key Features

- **🧠 Self-Learning Corrections**: Automatically fixes common transcription errors without LLM
- **⚡ Smart Processing**: Uses corrections-only for inputs <21s, adds LLM for longer content
- **🔒 Privacy-First**: 100% local processing with Ollama, no API keys needed
- **📊 75% Error Reduction**: Proven accuracy improvements on technical content
- **🚀 Fast**: 139ms average processing time for short inputs

## 📚 Documentation

- **[📈 Performance Improvements](docs/PERFORMANCE.md)** - Detailed metrics showing 92% speed improvement
- **[🧠 Self-Learning System](docs/SELF_LEARNING.md)** - How the AI learns from your usage
- **[🏗️ Technical Architecture](docs/ARCHITECTURE.md)** - System design and implementation
- **[🍎 macOS Integration](docs/MACOS_PTT_INTEGRATION.md)** - Setup with push-to-talk dictation
- **[📍 Repository Structure](WARP.md)** - Canonical file structure reference

## 📈 Performance & Accuracy

### Self-Learning Corrections Impact

```
ACCURACY IMPROVEMENTS (Before → After)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Word Concatenations:
  Before: 0%   |                    |
  After:  100% |████████████████████| +100%

Technical Terms:
  Before: 20%  |████                |
  After:  100% |████████████████████| +80%

Overall Accuracy:
  Before: 80%  |████████████████    |
  After:  95%  |███████████████████ | +15%
```

### Smart Processing Strategy

| Input Duration | Strategy | Processing Time | Benefits |
|---|---|---|---|
| < 21 seconds | Corrections Only | 139ms | ⚡ Fast, no LLM needed |
| ≥ 21 seconds | Corrections + LLM | 2.6s | 🎯 Full refinement |

### Automatic Corrections Examples

**Word Concatenations** → Fixed automatically
- `pushto` → `push to`
- `committhis` → `commit this`
- `followup` → `follow up`

**Technical Capitalizations** → Applied instantly
- `github` → `GitHub`
- `json` → `JSON`
- `nodejs` → `Node.js`
- `postgresql` → `PostgreSQL`

## 🚀 Quick Start

```bash
# 1. Install Ollama and pull a model
brew install ollama
ollama serve &
ollama pull llama3.1

# 2. Build VoxCompose
./gradlew --no-daemon clean fatJar

# 3. Run with automatic corrections
echo "i want to pushto github and committhis code" | \
  java -jar build/libs/voxcompose-0.1.0-all.jar

# Output: "I want to push to GitHub and commit this code"
```

## 🔧 Configuration

### Key Options

| Option | Description | Default |
|--------|-------------|------|
| `--model` | LLM model name | `llama3.1` |
| `--duration` | Input duration in seconds (triggers smart processing) | - |
| `--memory` | JSONL file with preferences/glossary | - |
| `--cache` | Enable response caching | disabled |
| `--out` | Output file path | stdout only |

### Environment Variables

- `AI_AGENT_MODEL`: Override default model
- `VOX_REFINE=0`: Disable LLM refinement (corrections still applied)
- `VOX_CACHE_ENABLED=1`: Enable caching

## 🔗 Integration with macOS PTT Dictation

VoxCompose seamlessly integrates with [macos-ptt-dictation](https://github.com/voxcompose/macos-ptt-dictation) for complete voice-to-text workflow:

1. **macOS PTT** captures audio with push-to-talk (F13/Shift+F13)
2. **Whisper** transcribes audio to text
3. **VoxCompose** applies corrections and optional LLM refinement
4. **Result**: Polished Markdown ready for use

### Setup Integration

```lua
-- In macos-ptt-dictation/hammerspoon/ptt_config.lua
LLM_REFINER = {
  ENABLED = true,
  CMD = { "/usr/bin/java", "-jar", os.getenv("HOME") .. "/code/voxcompose/build/libs/voxcompose-0.1.0-all.jar" },
  ARGS = { "--model", "llama3.1", "--duration", "{{DURATION}}" },
}
```

## 🧪 Testing

### Run Complete Test Suite

```bash
# Run all tests
./tests/run_tests.sh

# Individual tests:
./tests/validate_self_learning.sh  # Core validation
./tests/test_capabilities.sh       # Capabilities endpoint
./tests/test_duration_threshold.sh # Duration logic
./tests/generate_metrics.sh        # Performance report
```

### Expected Results

```
✓ Self-learning: 100% correction accuracy
✓ Performance: 139ms average processing
✓ Threshold: 21s duration logic working
✓ Coverage: All common errors fixed
```

## 📦 Installation

### Build from Source

```bash
# Requirements: Java 17+, Ollama
brew install openjdk@17 ollama

# Build
./gradlew --no-daemon clean fatJar
```

### Homebrew (Coming Soon)

```bash
brew tap voxcompose/tap
brew install voxcompose
```


## Changelog

See [CHANGELOG.md](./CHANGELOG.md)
