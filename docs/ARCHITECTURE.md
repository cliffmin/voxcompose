# Architecture

## System Overview

```
stdin → InputReader → LearningService → [LLM Client] → stdout
                           ↓
                      UserProfile
                           ↓
                  ~/.config/voxcompose/
```

## Core Components

### Main (`Main.java`)
Entry point and pipeline orchestration. Handles:
- CLI argument parsing
- Duration-based routing (corrections only vs corrections + LLM)
- stdin/stdout I/O

### Configuration (`config/Configuration.java`)
Manages settings from CLI args, environment variables, and defaults.

```
Precedence: CLI Arguments > Environment Variables > Defaults
```

### LearningService (`learning/LearningService.java`)
Applies and learns corrections:
- Pattern-based word corrections
- Technical term capitalizations
- Async learning from LLM refinements

### OllamaClient (`client/OllamaClient.java`)
HTTP client for Ollama LLM API:
- Connection pooling
- Timeout management
- Error handling

### RefineCache (`cache/RefineCache.java`)
LRU cache for LLM responses (optional).

## Data Flow

### Short input (< 21s)
```
Input → Apply corrections → Output
              (140ms)
```

### Long input (≥ 21s)
```
Input → Apply corrections → LLM refinement → Output
              (140ms)            (2.5s)
                                    ↓
                              Learn patterns
```

## File Layout

```
~/.config/voxcompose/
├── learned_profile.json   # Corrections database
└── cache/                 # Response cache (if enabled)
```
