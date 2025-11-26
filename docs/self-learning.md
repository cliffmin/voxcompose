# Self-Learning System

## Overview

VoxCompose automatically learns from corrections and applies them to future transcriptions without requiring cloud services.

## How It Works

1. **Input** → Transcription from Whisper
2. **Apply corrections** → Fix known patterns (instant, ~140ms)
3. **Duration check** → If ≥21s, also run LLM refinement
4. **Learn** → Compare LLM output to input, extract new patterns
5. **Update profile** → Store corrections for future use

## What It Learns

### Word Corrections
Commonly mis-transcribed word combinations:

| Transcribed | Corrected |
|-------------|-----------|
| pushto | push to |
| committhis | commit this |
| followup | follow up |

### Technical Vocabulary
Proper capitalization of technical terms:

| Transcribed | Corrected |
|-------------|-----------|
| github | GitHub |
| json | JSON |
| api | API |

### Personal Vocabulary
Over time, learns your unique vocabulary (company terms, project names, etc.).

## Storage

Learning data is stored locally at:
```
~/.config/voxcompose/learned_profile.json
```

You can inspect, modify, or delete this file. No data is sent to cloud services.

## Profile Schema

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
    "avgProcessingTime": 142
  }
}
```
