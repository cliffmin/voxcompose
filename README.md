# VoxCompose

**Smart transcript refinement with self-learning corrections and local LLM processing**

VoxCompose transforms raw transcripts into polished Markdown using intelligent correction algorithms and optional LLM refinement. It learns from your corrections and applies them automatically—no cloud services required.

### 🏆 Major Achievements in v1.0.0

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

- **[📈 Performance Improvements](docs/performance.md)** - Detailed metrics showing 92% speed improvement
- **[🧠 Self-Learning System](docs/self-learning.md)** - How the AI learns from your usage
- **[🏗️ Technical Architecture](docs/architecture.md)** - System design and implementation
- **[🍎 VoxCore Integration](docs/voxcore-integration.md)** - Setup with VoxCore push-to-talk

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

VoxCompose seamlessly integrates with [VoxCore](https://github.com/cliffmin/voxcore) for complete voice-to-text workflow:

1. **macOS PTT** captures audio with push-to-talk (F13/Shift+F13)
2. **Whisper** transcribes audio to text
3. **VoxCompose** applies corrections and optional LLM refinement
4. **Result**: Polished Markdown ready for use

### Setup Integration

```lua
-- In ~/.hammerspoon/ptt_config.lua
LLM_REFINER = {
  ENABLED = true,
  CMD = { "/opt/homebrew/bin/voxcompose" },
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

### Homebrew (Recommended)

```bash
brew tap cliffmin/tap
brew install voxcompose ollama
```

<details>
<summary>Alternative: Build from source</summary>

```bash
brew install openjdk@21 ollama
git clone https://github.com/cliffmin/voxcompose.git
cd voxcompose && ./gradlew --no-daemon clean fatJar
# JAR at build/libs/voxcompose-*-all.jar
```
</details>


## Changelog

See [CHANGELOG.md](./CHANGELOG.md)
