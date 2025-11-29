# Performance

## Processing Strategy

| Input Duration | Strategy | Typical Time |
|----------------|----------|--------------|
| < 21 seconds | Corrections only | ~140ms |
| ≥ 21 seconds | Corrections + LLM | ~2.5s |

The 21-second threshold was determined through analysis of real transcriptions—short utterances rarely benefit from LLM restructuring.

## Progress vs previous release

| Release | Short-clip path | Typical latency (<21s) | Common concat/casing errors |
|---------|-----------------|------------------------|-----------------------------|
| Pre-0.4.4 | Always LLM | ~1.8s | Baseline (1.0x) |
| 0.4.4 | Corrections-only | ~140ms | ~0.25x (≈75% fewer) |

Numbers come from `./tests/generate_metrics.sh` (short-clip benchmarks) plus manual verification on the sample phrases in `tests/generate_metrics.sh`.

## Correction Engine

| Input Size | Processing Time |
|------------|-----------------|
| 10 words | ~140ms |
| 50 words | ~150ms |
| 100 words | ~160ms |

Processing time scales well due to:
- Pre-compiled regex patterns
- Lazy LLM initialization (only when needed)
- Efficient string building

## Correction Categories

**Word concatenations** (instant fix):
- `pushto` → `push to`
- `committhis` → `commit this`

**Technical terms** (capitalization):
- `github` → `GitHub`
- `json` → `JSON`

## Validation

Run performance tests:
```bash
./tests/generate_metrics.sh
```
