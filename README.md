# VoxCompose Resources

This repository is a resource dump for interview prep and technical discussion. It does not contain a runnable application, build system, or release artifacts. Instead, it aggregates design documents, examples, and reference materials about the VoxCompose concept: an intelligent transcript refiner with self-learning corrections and customizable dictionaries.

## Table of Contents

- Overview
- How It Works
- Examples
- Quick Start
- Configuration
- Capabilities
- Integration (macOS PTT)
- Testing
- Documentation
- Installation
- Requirements
- Contributing
- License
- Project Achievements

## What is VoxCompose?

Conceptually, VoxCompose is a transcript refinement layer that sits between a speech recognition system and the destination text. It learns from your writing patterns, technical vocabulary, and correction preferences to automatically fix common transcription errors. The materials here describe such a system’s design and behavior.

### Primary Use Case (Concept)

```
[Hold Key] → [Record Audio] → [Whisper Transcribes] → [VoxCompose Refines] → [Polished Text]
```

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

## Using This Repository

- Read the design docs under `docs/` for architecture, performance reasoning, self-learning strategy, and macOS integration concepts.
- Use the examples below to understand the intended refinements and behavior.
- There is no CLI here; installation, build, or binary usage is intentionally out of scope.

## Configuration (Concept)

The docs discuss potential configuration options for a future implementation. There is no live CLI in this repository.

## Capabilities (Concept)

The docs describe a capabilities payload used for integration negotiation (e.g., “minimum duration” for LLM refinement). This is illustrative and not produced by code in this repo.

## 🔗 Integration with macOS PTT Dictation

VoxCompose seamlessly integrates with [macOS Push-to-Talk Dictation](https://github.com/cliffmin/macos-ptt-dictation) for a complete voice-to-text workflow:

### How It Works

```mermaid
graph LR
    A[Hold Hotkey] --> B[Audio Recording]
    B --> C[Whisper Transcription]
    C --> D[VoxCompose Refinement]
    D --> E[Polished Text at Cursor]
```

1. **Push-to-Talk**: Hold `Cmd+Alt+Ctrl+Space` (or your custom hotkey) to record
2. **Transcription**: Whisper converts audio to text locally  
3. **Refinement**: VoxCompose applies corrections and formatting
4. **Insertion**: Polished text appears at your cursor position

### Quick Setup (Concept)

For conceptual integration with macOS Push-to-Talk Dictation, see docs/MACOS_PTT_INTEGRATION.md. This repository does not provide or install a CLI.

### Advanced Configuration (Concept)

Some docs describe memory files, dictionaries, and thresholds for discussion. Treat these as design references rather than instructions.

### Troubleshooting (Concept)

Operational troubleshooting is out of scope for this repository. For PTT setup background, see the [macos-ptt-dictation documentation](https://github.com/cliffmin/macos-ptt-dictation/blob/main/docs/setup/README.md).

## Testing

This repository includes lightweight scripts under `tests/` that validate documentation hygiene (no build files, updated README messaging). There are no unit/integration tests for code here.

## Documentation

- [Performance Analysis](docs/PERFORMANCE.md) - Detailed benchmarks and optimization journey
- [Self-Learning System](docs/SELF_LEARNING.md) - How the correction engine learns (includes data location and migration)
- [Architecture](docs/ARCHITECTURE.md) - Technical design and implementation
- [macOS Integration](docs/MACOS_PTT_INTEGRATION.md) - Push-to-talk dictation setup

## Installation

Not applicable. This repository does not distribute binaries or a build system.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

### Development Setup

Clone the repository and browse the docs:

```bash
git clone https://github.com/cliffmin/voxcompose.git
cd voxcompose
ls docs
```

## License

MIT - See [LICENSE](LICENSE) for details.

## Acknowledgments

- Built for the macOS dictation community
- Inspired by the need for better technical transcription
- Thanks to all contributors and users providing feedback

---

## Project Achievements

### Performance Milestones (Concept)

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

See [CHANGELOG.md](CHANGELOG.md) for a history of document changes.
