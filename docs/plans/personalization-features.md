# VoxCompose Personalization Features Implementation Plan

## Overview
Features moved from VoxCore to VoxCompose to maintain VoxCore's stateless architecture.
These features require user state, learning, or ML capabilities.

## Features to Implement

### 1. Smart Reflow (Line Break Intelligence)
**Problem:** Wall-of-text output from transcription lacks natural paragraph breaks.
**Solution:** Use local LLM to detect sentence boundaries and add appropriate line breaks.

**Implementation:**
- Add `--reflow` flag to VoxCompose CLI
- Prompt template: "Add paragraph breaks to this transcription where natural pauses occur. Keep the text exactly as-is otherwise."
- Use Ollama with fast model (e.g., phi3, llama3.2)
- Estimated latency: 200-500ms additional

**Priority:** Medium
**Effort:** Small (1-2 hours)

### 2. User Dictionary / Custom Corrections
**Problem:** Users have personal vocabulary (names, products, jargon) that Whisper misrecognizes.
**Solution:** User-editable corrections file that VoxCompose applies.

**Implementation:**
- Config location: `~/.config/voxcompose/dictionary.json`
- Schema:
```json
{
  "replacements": {
    "wrong": "Right",
    "misheard": "Correct"
  },
  "splits": ["andthen", "thenyou"],
  "no_split": ["CompanyName", "ProductX"]
}
```
- Load at startup, apply after ML refinement
- Provide `voxcompose dictionary add "wrong" "Right"` CLI

**Priority:** High
**Effort:** Medium (2-4 hours)

### 3. Corpus Mining / Auto-Suggest
**Problem:** Users don't know what corrections they need.
**Solution:** Analyze transcription history to suggest common misrecognitions.

**Implementation:**
- Scan `~/Documents/VoiceNotes/` for `.txt` files
- Build frequency map of words
- Identify likely errors (low-frequency variants of high-frequency words)
- Output suggestions: `voxcompose suggest-corrections`
- User reviews and adds to dictionary

**Priority:** Low
**Effort:** Medium (3-5 hours)

### 4. Self-Learning Corrections
**Problem:** Manual curation is tedious.
**Solution:** VoxCompose learns from user edits over time.

**Current State:** Already partially implemented in VoxCompose (corrections.json with confidence scores).

**Enhancement:**
- Improve confidence scoring
- Add decay for old corrections
- Integrate with corpus mining suggestions

**Priority:** Medium
**Effort:** Medium (already started)

## Integration with VoxCore

VoxCore remains stateless. VoxCompose hooks in via:

1. **Plugin interface** (future): VoxCore calls VoxCompose after transcription
2. **Standalone refinement**: `voxcompose refine < input.txt > output.txt`
3. **Hammerspoon integration**: Lua calls VoxCompose jar after VoxCore processing

## Timeline

| Feature | Priority | Effort | Status |
|---------|----------|--------|--------|
| User Dictionary | High | Medium | Not started |
| Smart Reflow | Medium | Small | Not started |
| Self-Learning | Medium | Medium | Partial |
| Corpus Mining | Low | Medium | Not started |

## Notes

- All features should be optional and disabled by default
- Performance budget: <500ms additional latency for any feature
- VoxCompose requires Ollama for ML features; dictionary-only mode works without it
