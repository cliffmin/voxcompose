# VoxCompose – VoxCore Intelligence Layer

The AI‑powered transcript refinement engine for the VoxCore voice intelligence platform.

Note: This repository is a resource dump for interview prep and technical discussion. It does not contain a runnable application, build system, or release artifacts. All examples are illustrative.

## VoxCompose Intelligence Architecture

VoxCompose is the production intelligence layer envisioned for VoxCore’s advanced processing. It transforms raw speech recognition into intelligent, contextual output through:

- Real‑time Learning: Adapts to vocabulary and communication patterns
- Professional Quality: Optimized for technical content and business communication
- Performance Excellence: 92% speed improvement (1.8s → 142ms) for common inputs
- Seamless Integration: Fits into VoxCore platform architecture

### VoxCore Platform Integration (Concept)

```
[VoxCore Input] → [Whisper Recognition] → [VoxCompose Intelligence] → [Professional Output]
                                           ↓
                    [Learning Engine] ← [User Corrections] → [Analytics]
```

### Intelligence Capabilities

- Intelligent Refinement: Context‑aware correction and professional formatting
- Adaptive Learning: Builds personalized intelligence from usage patterns
- Self‑Optimization: Continually improves accuracy and relevance
- Privacy‑First: Local‑first processing design
- Enterprise‑Ready: Team learning and organizational intelligence (concept)

## Performance Highlights (Concept)

Illustrative examples of optimization impact:

```
Response Time (ms)
2000 |
1800 | * (Initial)
1600 |  \
1400 |   \
1200 |    \ (Optimizations)
1000 |     *
 800 |      \
 600 |       *
 400 |        \___
 200 |            *---*---*---*
   0 +------------------------>
     0   1   2   3   4   5   6  Iterations
```

## How It Works (Concept)

1) Input Processing: Ingest raw transcript
2) Context Analysis: Content type, domain, and preferences
3) Smart Corrections: Learned patterns, technical vocabulary, formatting
4) Adaptive Learning: Improve from corrections and usage
5) Performance Pathing: Fast path (<200ms) for common patterns

## Examples (Concept)

- Technical Vocabulary: “jason” → “JSON”, “api” → “API”
- Word Boundaries: “pushto” → “push to”, “committhis” → “commit this”
- Capitalization: “nodejs” → “Node.js”, “postgresql” → “PostgreSQL”

## Technical Architecture (Concept)

```
Raw Transcript → Content Analysis → Pattern Matching → Smart Corrections → Learning Update
```

Performance tactics:
- Fast Path: <200ms for frequent patterns and corrections
- Intelligent Routing: Depth based on content complexity
- Learning Pipeline: Async pattern learning and updates

## Enterprise Intelligence (Concept)

- Shared Vocabularies: Organizational terminology and preferences
- Collaborative Learning: Team‑wide improvements from individual usage
- Analytics & Insights: Accuracy, throughput, improvement rates
- Compliance Controls: Enterprise privacy and retention policies

## Using the Java CLI

Quickstart

```bash
TMP=$(mktemp -d)
echo "i need to pushto github and update the json api" | \
  java -jar cli-java/build/libs/voxcompose-cli-all.jar --data-dir "$TMP" --stats
```

- Input is echoed to stdout unchanged
- learned_profile.json is written under $TMP with essential caps and splits
- --stats emits a JSON line to stderr with basic metrics

Data directory precedence
- VOXCOMPOSE_DATA_DIR
- $XDG_DATA_HOME/voxcompose
- macOS: ~/Library/Application Support/VoxCompose
- Linux: ~/.local/share/voxcompose

Integration (PTT/Hammerspoon) example
```bash
# Wrapper script example
voxcompose() {
  local jar="$HOME/.local/share/voxcompose/voxcompose-cli-all.jar"
  java -jar "$jar" "$@"
}
```

## Documentation & Resources

- **[Intelligence Architecture](docs/ARCHITECTURE.md)** - Technical design and implementation details
- **[Performance Analysis](docs/PERFORMANCE.md)** - Detailed optimization journey and benchmarks
- **[Learning System](docs/SELF_LEARNING.md)** - Adaptive intelligence and improvement algorithms
- **[macOS Integration](docs/MACOS_PTT_INTEGRATION.md)** - Push-to-talk dictation setup and integration
- **[Long-Term CLI Integration Plan](docs/LONG_TERM_CLI_INTEGRATION.md)** - Draft plan for an official CLI and PTT integration

## Using This Repository

This is a resource‑only repository. There is no CLI or installation here. To validate repo hygiene, run:

```bash
bash tests/run_checks.sh
```

## Contributing

Improvements or clarifications to the documentation are welcome.

- See `CONTRIBUTING.md` for contribution guidelines.
- See `CODE_OF_CONDUCT.md` for community standards.
- See `SECURITY.md` for vulnerability reporting.

See VoiceCore Platform Development for contribution guidelines:
- https://github.com/cliffmin/voxcore/tree/main/docs/development

## License

MIT — See `LICENSE`.

---

All metrics, versions, and examples are illustrative and do not represent a shipped product.
