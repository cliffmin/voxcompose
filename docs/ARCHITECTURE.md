# VoxCompose Architecture

## System Overview

VoxCompose is a high-performance, privacy-focused transcription refinement system built with a modular Java architecture that enables real-time correction and optional LLM enhancement.

```
┌──────────────────────────────────────────────────────────────────┐
│                        VoxCompose System                          │
├──────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌─────────────┐  ┌──────────────┐  ┌─────────────────────────┐ │
│  │   Input     │→ │  Correction  │→ │   Smart Processing      │ │
│  │   Reader    │  │   Engine     │  │     Controller          │ │
│  └─────────────┘  └──────────────┘  └─────────────────────────┘ │
│                           ↓                      ↓               │
│  ┌─────────────┐  ┌──────────────┐  ┌─────────────────────────┐ │
│  │  Learning   │← │    Cache     │  │    LLM Client           │ │
│  │   Service   │  │   Manager    │  │   (Optional)            │ │
│  └─────────────┘  └──────────────┘  └─────────────────────────┘ │
│                                                                   │
└──────────────────────────────────────────────────────────────────┘
```

## Core Components

### 1. Input Pipeline

**InputReader** (`io.InputReader`)
- Efficient stdin reading with 16KB buffers
- Non-blocking I/O for responsive performance
- UTF-8 encoding with proper character handling

### 2. Configuration System

**Configuration** (`config.Configuration`)
- Hierarchical configuration precedence
- Environment variable support
- Command-line argument parsing
- Default value management
 - Default memory path fallback to `~/.config/voxcompose/memory.jsonl` when `--memory` is not provided and the file exists

```java
// Configuration precedence
CLI Arguments > Environment Variables > Defaults
```

### 3. Correction Engine

**LearningService** (`learning.LearningService`)
- Pattern-based correction application
- Real-time string replacement
- Context-aware processing
- Zero-allocation design
 - Applies user dictionaries from `~/.voxcompose/dictionaries` (built-ins are installed on first run)

```
Performance Characteristics:
- Latency: < 150ms for typical input
- Throughput: > 1,200 words/second
- Memory: < 10MB resident
```

### 4. Smart Processing Controller

**Main** (`Main.java`)
- Duration-based routing logic
- Threshold management (21-second default)
- Graceful degradation
- Pipeline orchestration

### 5. Learning System

**Components:**
- **UserProfile**: Stores learned patterns
- **LearningService**: Applies corrections
- **Pattern Extractor**: Identifies corrections
- **Confidence Scorer**: Validates patterns

### 6. Caching Layer

**RefineCache** (`cache.RefineCache`)
- LRU eviction policy
- Configurable TTL
- Memory-efficient storage
- Thread-safe operations

### 7. LLM Integration

**OllamaClient** (`client.OllamaClient`)
- HTTP connection pooling
- Async request handling
- Timeout management
- Error recovery

## Data Flow

### Short Input Path (< 21 seconds)

```
Input → Corrections → Output
         (142ms)
```

1. Read input from stdin
2. Apply learned corrections
3. Output to stdout
4. Learn patterns asynchronously

### Long Input Path (≥ 21 seconds)

```
Input → Corrections → LLM Refinement → Output
         (142ms)       (2,358ms)
```

1. Read input from stdin
2. Apply learned corrections
3. Check cache for previous refinement
4. Send to LLM for restructuring
5. Cache result
6. Output to stdout
7. Learn new patterns

## Performance Optimizations

### 1. Memory Management

- **Object Pooling**: Reuse StringBuilder instances
- **Direct Buffers**: NIO for I/O operations
- **Lazy Loading**: Components initialized on-demand
- **Weak References**: For cache entries

### 2. Concurrency

- **Async Learning**: Non-blocking pattern updates
- **Thread Pools**: Managed executor services
- **Lock-Free**: Where possible using atomics
- **Read-Write Locks**: For shared state

### 3. Algorithm Efficiency

- **Compiled Regex**: Pre-compiled patterns
- **Trie Structures**: For word lookups
- **Binary Search**: For sorted collections
- **Hash Maps**: O(1) correction lookups

## Storage Architecture

### File System Layout

Preferred (XDG data/macOS Application Support):
```
$XDG_DATA_HOME/voxcompose/
# macOS fallback: ~/Library/Application Support/VoxCompose/
# Linux fallback: ~/.local/share/voxcompose/
├── learned_profile.json    # User learning data
├── cache/                  # Response cache
│   └── entries/            # Cached refinements
└── logs/                   # Application logs
```

Legacy (pre-migration):
```
~/.config/voxcompose/
├── learned_profile.json
```

### Profile Schema

```json
{
  "wordCorrections": {
    "pushto": "push to",
    "committhis": "commit this"
  },
  "capitalizations": {
    "github": "GitHub",
    "json": "JSON"
  },
  "statistics": {
    "totalRefinements": 1247,
    "avgProcessingTime": 142,
    "lastUpdated": "2025-09-16T10:30:00Z"
  }
}
```

## Integration Points

### 1. Command Line Interface

```bash
voxcompose [OPTIONS]
  --model <name>        # LLM model selection
  --duration <seconds>  # Input duration hint
  --cache              # Enable caching
  --memory <file>      # Memory JSONL path
```

### 2. Environment Variables

- `AI_AGENT_MODEL`: Override default model
- `AI_AGENT_URL`: Custom LLM endpoint
- `VOX_REFINE`: Enable/disable refinement
- `VOX_CACHE_ENABLED`: Cache control

### 3. macOS Integration

Designed for seamless integration with macOS PTT dictation:

```lua
-- Hammerspoon integration
LLM_REFINER = {
  CMD = { "java", "-jar", "voxcompose.jar" },
  ARGS = { "--duration", "{{DURATION}}" }
}
```

## Security Considerations

### 1. Privacy

- **Local Processing**: No cloud dependencies
- **No Telemetry**: Zero tracking
- **Encrypted Storage**: Optional profile encryption
- **Sandboxed**: Runs in user space only

### 2. Input Validation

- **Size Limits**: Maximum input length enforced
- **Character Filtering**: Invalid UTF-8 handling
- **Injection Prevention**: Sanitized LLM prompts
- **Path Traversal**: Protected file operations

### 3. Network Security

- **Local Only**: Default localhost binding
- **HTTPS Option**: For remote LLM endpoints
- **Timeout Protection**: Prevent hanging
- **Connection Limits**: Resource exhaustion prevention

## Extensibility

### Plugin Architecture (Future)

```java
public interface CorrectionPlugin {
    String getName();
    String applyCorrections(String input);
    void learn(String input, String output);
}
```

### Custom Patterns

Users can extend corrections via profile editing:

```json
{
  "customPatterns": [
    {
      "pattern": "\\bcorp\\b",
      "replacement": "corporation",
      "confidence": 0.95
    }
  ]
}
```

## Testing Architecture

### Test Categories

1. **Unit Tests**: Component isolation
2. **Integration Tests**: Pipeline validation
3. **Performance Tests**: Benchmark suite
4. **Golden Tests**: Accuracy validation

### Test Infrastructure

```
tests/
├── run_tests.sh           # Main test runner
├── validate_self_learning.sh
├── test_capabilities.sh
├── test_duration_threshold.sh
└── generate_metrics.sh
```

## Performance Monitoring

### Key Metrics

- **Response Time**: P50, P90, P99 latencies
- **Throughput**: Words/second processing
- **Accuracy**: Error rate reduction
- **Resource Usage**: CPU, memory, I/O

### Benchmarking

```bash
# Run performance benchmarks
./tests/generate_metrics.sh

# Results stored in metrics.json
{
  "avgCorrectionTime": 142,
  "errorReduction": 75,
  "throughput": 1204
}
```

## Deployment Architecture

### Standalone JAR

```
voxcompose-0.3.0-all.jar
├── Application classes
├── Dependencies (shaded)
├── Resources
└── Manifest
```

### System Requirements

- **Java**: 17+ (OpenJDK or Oracle)
- **Memory**: 512MB minimum
- **Disk**: 100MB for application + cache
- **OS**: macOS, Linux, Windows

## Future Architecture Enhancements

### Planned Improvements

1. **WebAssembly Target**: Browser-based corrections
2. **gRPC Service**: Microservice deployment
3. **Distributed Learning**: Federated pattern sharing
4. **GPU Acceleration**: CUDA/Metal for batch processing

### Scalability Roadmap

```
Current: Single-user desktop (1-10 req/sec)
    ↓
Phase 1: Multi-user server (100 req/sec)
    ↓
Phase 2: Distributed cluster (1,000 req/sec)
    ↓
Phase 3: Edge deployment (10,000 req/sec)
```

## Conclusion

VoxCompose's architecture prioritizes performance, privacy, and extensibility. The modular design enables easy enhancement while maintaining the core promise of fast, accurate, local transcription refinement.

---

*Architecture documented for VoxCompose v0.3.0*
