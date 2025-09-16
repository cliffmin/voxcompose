# VoxCompose

[![CI](https://github.com/cliffmin/voxcompose/actions/workflows/ci.yml/badge.svg)](https://github.com/cliffmin/voxcompose/actions/workflows/ci.yml)
[![Security](https://github.com/cliffmin/voxcompose/actions/workflows/security.yml/badge.svg)](https://github.com/cliffmin/voxcompose/actions/workflows/security.yml)
[![Code Quality](https://github.com/cliffmin/voxcompose/actions/workflows/quality.yml/badge.svg)](https://github.com/cliffmin/voxcompose/actions/workflows/quality.yml)
[![Release](https://img.shields.io/github/v/release/cliffmin/voxcompose)](https://github.com/cliffmin/voxcompose/releases)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

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
- **[🍎 macOS Integration](#-integration-with-macos-ptt-dictation)** - Setup with push-to-talk dictation
- **[📍 Repository Structure](warp.md)** - Canonical file structure reference

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

VoxCompose seamlessly integrates with [macos-ptt-dictation](https://github.com/cliffmin/macos-ptt-dictation) for a complete voice-to-text workflow:

### How It Works

```mermaid
graph LR
    A[Hold F13] --> B[Audio Recording]
    B --> C[Whisper Transcription]
    C --> D[VoxCompose Refinement]
    D --> E[Polished Text at Cursor]
```

1. **Push-to-Talk**: Hold F13 to record, release to process
2. **Transcription**: Whisper converts audio to text locally  
3. **Refinement**: VoxCompose applies corrections and formatting
4. **Insertion**: Text appears at your cursor position

### Quick Setup

#### Step 1: Install macos-ptt-dictation

```bash
# Install the complete PTT system
git clone https://github.com/cliffmin/macos-ptt-dictation.git
cd macos-ptt-dictation
./scripts/setup/install.sh
```

#### Step 2: Configure VoxCompose Integration

Edit `~/.hammerspoon/ptt_config.lua`:

```lua
-- Enable VoxCompose as the post-processor
LLM_REFINER = {
  ENABLED = true,
  -- If installed via Homebrew:
  CMD = { "/usr/local/bin/voxcompose" },
  -- Or if using JAR directly:
  -- CMD = { "/usr/bin/java", "-jar", os.getenv("HOME") .. "/voxcompose.jar" },
  ARGS = { 
    "--model", "llama3.1",
    "--duration", "{{DURATION}}",
    "--memory", os.getenv("HOME") .. "/.config/voxcompose/memory.jsonl"
  },
}
```

#### Step 3: Reload Hammerspoon

```bash
# Apply configuration
hs -c "hs.reload()"
```

### Advanced Configuration

#### Custom Corrections Dictionary

Create `~/.config/voxcompose/memory.jsonl` with your preferences:

```jsonl
{"role": "user", "content": "Always capitalize: GitHub, TypeScript, PostgreSQL"}
{"role": "user", "content": "Technical terms: API, CI/CD, REST, GraphQL"}
{"role": "user", "content": "Company names: OpenAI, Anthropic, Google"}
```

#### Performance Tuning

```lua
-- For fastest response (corrections only, no LLM)
LLM_REFINER = {
  ENABLED = true,
  CMD = { "/usr/local/bin/voxcompose" },
  ARGS = { "--no-llm" },  -- Skip LLM, use corrections only
}

-- For maximum accuracy (always use LLM)
LLM_REFINER = {
  ENABLED = true,
  CMD = { "/usr/local/bin/voxcompose" },
  ARGS = { 
    "--model", "llama3.1",
    "--force-llm"  -- Always apply LLM refinement
  },
}
```

### Troubleshooting Integration

| Issue | Solution |
|-------|----------|
| VoxCompose not found | Ensure it's installed: `brew install cliffmin/tap/voxcompose` |
| Ollama not running | Start Ollama: `ollama serve &` |
| No corrections applied | Check memory file exists and is valid JSONL |
| Slow processing | Use `--no-llm` flag for instant corrections only |

For detailed PTT setup, see the [macos-ptt-dictation documentation](https://github.com/cliffmin/macos-ptt-dictation/blob/main/docs/setup/README.md).

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

### Option 1: Homebrew (Recommended)

```bash
# Add the tap and install
brew tap cliffmin/tap
brew install voxcompose

# Verify installation
voxcompose --help
```

### Option 2: Build from Source

```bash
# Requirements: Java 11+, Ollama
brew install openjdk@11 ollama gradle

# Clone and build
git clone https://github.com/cliffmin/voxcompose.git
cd voxcompose
./gradlew --no-daemon clean fatJar

# Create alias for easy access (optional)
alias voxcompose='java -jar $(pwd)/build/libs/voxcompose-0.1.0-all.jar'
```

### Option 3: Direct JAR Download

```bash
# Download the latest release JAR
curl -L https://github.com/cliffmin/voxcompose/releases/latest/download/voxcompose-0.3.0-all.jar \
  -o voxcompose.jar

# Run directly
java -jar voxcompose.jar --help
```


## Changelog

See [CHANGELOG.md](./CHANGELOG.md)
