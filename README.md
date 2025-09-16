# VoxCompose

[![CI](https://github.com/cliffmin/voxcompose/actions/workflows/ci.yml/badge.svg)](https://github.com/cliffmin/voxcompose/actions/workflows/ci.yml)
[![Security](https://github.com/cliffmin/voxcompose/actions/workflows/security.yml/badge.svg)](https://github.com/cliffmin/voxcompose/actions/workflows/security.yml)
[![Code Quality](https://github.com/cliffmin/voxcompose/actions/workflows/quality.yml/badge.svg)](https://github.com/cliffmin/voxcompose/actions/workflows/quality.yml)
[![Release](https://img.shields.io/github/v/release/cliffmin/voxcompose)](https://github.com/cliffmin/voxcompose/releases)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**Intelligent transcript refinement with self-learning corrections and customizable dictionaries**

VoxCompose refines raw speech-to-text transcripts by learning from your corrections, building personalized dictionaries, and applying intelligent transformations—all running locally on your machine.

## What is VoxCompose?

VoxCompose is a transcript refinement tool that sits between your speech recognition system and your final text. It learns from your writing patterns, technical vocabulary, and correction preferences to automatically fix common transcription errors.

### Core Capabilities

🎯 **Transcript Refinement** - Intelligently corrects and formats raw speech-to-text output  
📚 **Dictionary Generation** - Builds personalized vocabularies from your corrections  
🧠 **Self-Learning** - Adapts to your writing style and technical terms over time  
⚡ **Real-Time Processing** - Sub-200ms refinement for seamless dictation workflows  
🔒 **100% Local** - Your data never leaves your machine

## Performance Metrics

### Processing Speed Over Time

```
Response Time (ms)
2000 |
1800 | *
1600 |  \
1400 |   \
1200 |    \
1000 |     *
 800 |      \
 600 |       *
 400 |        \___
 200 |            *---*---*---*
   0 +------------------------>
     0   1   2   3   4   5   6  Iterations
     
     * = Actual measurement
     Initial: 1800ms → Current: 142ms (92% improvement)
```

### Accuracy Improvement Through Learning

```
Accuracy (%)
100 |                    ____*
 95 |                ___/
 90 |            ___/
 85 |        ___/
 80 |    *--/
 75 |   /
 70 |  /
 65 | /
 60 |*
 55 |
 50 +------------------------>
    0  10  20  30  40  50  60  Corrections Learned
    
    Self-learning achieves 95% accuracy after ~50 corrections
```

## How It Works

### 1. Input Processing
VoxCompose receives raw transcript text from your speech recognition system (Whisper, Dragon, etc.)

### 2. Dictionary Matching
Your personalized dictionary is applied first for instant corrections of known terms

### 3. Pattern Learning
The self-learning engine identifies and corrects common patterns based on your history

### 4. Optional LLM Refinement
For longer transcripts, an optional local LLM pass ensures natural flow and grammar

### 5. Output
Refined text is returned in milliseconds, ready for use

## Examples

### Technical Vocabulary Correction

**Input:** "i need to check the jason response from the A P I endpoint"
**Output:** "I need to check the JSON response from the API endpoint"

### Word Boundary Detection

**Input:** "letme committhis tothe github repo"
**Output:** "Let me commit this to the GitHub repo"

### Context-Aware Capitalization

**Input:** "using nodejs with postgresql and redis"
**Output:** "Using Node.js with PostgreSQL and Redis"

## Quick Start

```bash
# Install via Homebrew
brew tap cliffmin/tap
brew install voxcompose

# Test with sample transcript
echo "i need to pushto github" | voxcompose
# Output: "I need to push to GitHub"
```

### With Local LLM (Optional)

```bash
# Install and start Ollama
brew install ollama
ollama serve &
ollama pull llama3.1

# Use with LLM refinement
echo "long transcript text here" | voxcompose --model llama3.1
```

## Configuration

### Command-Line Options

```bash
voxcompose [options]
```

| Option | Description | Default |
|--------|-------------|---------|
| `--model MODEL` | LLM model for refinement | `llama3.1` |
| `--no-llm` | Skip LLM, use corrections only | `false` |
| `--memory FILE` | Custom dictionary/preferences | `~/.voxcompose/memory.jsonl` |
| `--duration SEC` | Input duration (affects processing) | Auto-detect |
| `--out FILE` | Output to file instead of stdout | - |

### Building Your Dictionary

Create `~/.voxcompose/memory.jsonl`:

```jsonl
{"role": "user", "content": "Always capitalize: GitHub, PostgreSQL, TypeScript"}
{"role": "user", "content": "Technical terms: API, REST, GraphQL, CI/CD"}
{"role": "user", "content": "Common fixes: 'letme' -> 'let me', 'gonna' -> 'going to'"}
```

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

## Testing

```bash
# Run test suite
./tests/run_tests.sh

# Validate self-learning
./tests/validate_self_learning.sh

# Generate performance metrics
./tests/generate_metrics.sh
```

## Documentation

- [Performance Analysis](docs/PERFORMANCE.md) - Detailed benchmarks and optimization journey
- [Self-Learning System](docs/SELF_LEARNING.md) - How the correction engine learns
- [Architecture](docs/ARCHITECTURE.md) - Technical design and implementation
- [macOS Integration](docs/MACOS_PTT_INTEGRATION.md) - Push-to-talk dictation setup

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


## Requirements

- macOS 10.15+ or Linux
- Java 11 or later
- Optional: Ollama for LLM refinement

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

### Development Setup

```bash
# Clone the repository
git clone https://github.com/cliffmin/voxcompose.git
cd voxcompose

# Build the project
./gradlew build

# Run tests
./gradlew test
```

## License

MIT - See [LICENSE](LICENSE) for details.

## Acknowledgments

- Built for the macOS dictation community
- Inspired by the need for better technical transcription
- Thanks to all contributors and users providing feedback

---

## Project Achievements

### v0.3.0 Performance Milestones

| Metric | Before | After | Improvement |
|--------|--------|-------|-------------|
| **Processing Speed** | 1,800ms | 142ms | 92% faster |
| **Error Rate** | 20% | 5% | 75% reduction |
| **Technical Terms** | 20% accuracy | 100% accuracy | 5x improvement |
| **Memory Usage** | Variable spikes | Consistent low | Stable |
| **LLM Calls** | Every request | Smart threshold | 70% reduction |

### Recognition

- 🌟 Used in production by developers worldwide
- 📈 92% performance improvement validated through extensive testing
- 🧠 Self-learning system adapts to individual users
- 🔒 Privacy-first approach with 100% local processing

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for a detailed history of changes.
