# Self-Learning Dictionary Plugin

## Vision
A plugin that learns user-specific speech patterns from transcription history, automatically identifies systematic errors, and builds a personalized correction dictionary that improves over time.

## Why This Matters
1. **Whisper has consistent blind spots** - Same speaker + same word = same error, repeatedly
2. **Manual curation doesn't scale** - Users won't maintain dictionaries; automation is key
3. **Personalization is the differentiator** - Generic post-processing only goes so far

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                         VoxCompose                                   │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────────────┐  │
│  │ Corpus Miner │───▶│ Error Detect │───▶│ Correction Suggester │  │
│  └──────────────┘    └──────────────┘    └──────────────────────┘  │
│         │                   │                       │               │
│         ▼                   ▼                       ▼               │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │              Learned Dictionary Store (JSON/SQLite)          │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                              │                                      │
│                              ▼                                      │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │              Standard API (OpenAPI/JSON Schema)               │  │
│  └──────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
                               │
                               ▼
              ┌────────────────────────────────┐
              │  Consumers (VoxCore, others)   │
              └────────────────────────────────┘
```

## Core Components

### 1. Corpus Miner
Scans transcription history to build a dataset.

**Data sources:**
- `~/Documents/VoiceNotes/` - Raw recordings + transcriptions
- Clipboard history (if available) - What user actually pasted vs original
- User corrections - Edits made after transcription

**Output:**
```json
{
  "transcription_id": "2025-Nov-26_01.22.40_PM",
  "segments": [
    {
      "timestamp": [0.0, 2.5],
      "raw_text": "the vox core project",
      "final_text": "the VoxCore project",
      "confidence": 0.87,
      "was_edited": true
    }
  ]
}
```

### 2. Error Detector
Identifies likely transcription errors using multiple signals.

**Detection strategies:**
- **Edit distance analysis** - If user consistently changes "vox core" → "VoxCore"
- **Statistical anomalies** - Word appears 10x more often than in general English
- **Low confidence segments** - Whisper's own uncertainty signal
- **Phonetic clustering** - Multiple similar-sounding outputs for same context
- **N-gram violations** - "the the" or "I is" patterns

**Output:**
```json
{
  "error_type": "consistent_edit",
  "original": "vox core",
  "observed_corrections": ["VoxCore", "Vox Core", "voxcore"],
  "frequency": 47,
  "confidence": 0.92,
  "contexts": ["the _ project", "using _ to"]
}
```

### 3. Correction Suggester
Generates and ranks correction candidates.

**Candidate sources:**
- User's historical corrections (highest weight)
- Phonetic similarity (Soundex, Metaphone, Double Metaphone)
- Edit distance to known words
- LLM suggestions (optional, for novel cases)
- Domain dictionary lookups (tech terms, proper nouns)

**Ranking features:**
- Frequency of this correction in history
- Phonetic similarity score
- Context match score
- User approval history

### 4. Learned Dictionary Store
Persistent storage for learned corrections.

**Schema:**
```json
{
  "version": "1.0",
  "created": "2025-11-26T00:00:00Z",
  "updated": "2025-11-26T12:00:00Z",
  "entries": [
    {
      "id": "uuid",
      "source": "vox core",
      "target": "VoxCore",
      "confidence": 0.95,
      "learned_from": "user_edits",
      "context_hints": ["project", "using", "the _ is"],
      "frequency": 47,
      "first_seen": "2025-10-01",
      "last_seen": "2025-11-26",
      "approved": true
    }
  ],
  "metadata": {
    "total_transcriptions_analyzed": 1250,
    "total_corrections_learned": 89,
    "accuracy_estimate": 0.94
  }
}
```

## Standard API Schema

### Dictionary Entry Format (for interop)
Following common patterns from spell-checkers and translation memory:

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "source": {
      "type": "string",
      "description": "Original text (what was transcribed)"
    },
    "target": {
      "type": "string",
      "description": "Corrected text (what it should be)"
    },
    "confidence": {
      "type": "number",
      "minimum": 0,
      "maximum": 1,
      "description": "How confident we are in this correction"
    },
    "case_sensitive": {
      "type": "boolean",
      "default": false
    },
    "whole_word": {
      "type": "boolean",
      "default": true,
      "description": "Match whole words only vs substring"
    },
    "context": {
      "type": "array",
      "items": { "type": "string" },
      "description": "Context patterns where this applies (regex or _ placeholder)"
    },
    "tags": {
      "type": "array",
      "items": { "type": "string" },
      "description": "Categories: technical, proper_noun, slang, etc."
    },
    "metadata": {
      "type": "object",
      "description": "Plugin-specific data"
    }
  },
  "required": ["source", "target"]
}
```

### REST API (future)
```
GET  /dictionary              - List all entries
GET  /dictionary/{id}         - Get single entry
POST /dictionary              - Add entry (manual or learned)
PUT  /dictionary/{id}         - Update entry
DELETE /dictionary/{id}       - Remove entry

POST /dictionary/suggest      - Get suggestions for text
POST /dictionary/learn        - Trigger learning from corpus
GET  /dictionary/export       - Export in standard format
POST /dictionary/import       - Import from standard format
```

### Export Formats
Support common formats for interoperability:
- **JSON** (native) - Full schema with metadata
- **CSV** - Simple source,target for spreadsheet editing
- **TMX** (Translation Memory eXchange) - Industry standard
- **TBX** (TermBase eXchange) - For terminology

## Neural Network Exploration

### Why a Neural Network?
This is a good learning project because:
1. **Clear I/O** - (wrong_word, context) → correct_word
2. **Bounded problem** - Not general NLP, just correction mapping
3. **Rich features** - Phonetics, frequency, context all available
4. **Small data is fine** - Hundreds of examples, not millions

### Simple Architecture
```
Input Features:
├── Phonetic encoding (Soundex/Metaphone) → embedding
├── Character n-grams → embedding  
├── Context words → embedding (pretrained or learned)
├── Frequency features → normalized scalar
└── Confidence score → normalized scalar

Model:
├── Concatenate embeddings
├── Dense(128, relu)
├── Dropout(0.3)
├── Dense(64, relu)
├── Dense(vocab_size, softmax)  # or Dense(1, sigmoid) for binary

Output:
├── P(correction | input, context)
└── Or: Is this a valid correction? (binary)
```

### Training Data Generation
```python
# Positive examples: actual corrections made
{"input": "vox core", "context": "the _ project", "target": "VoxCore", "label": 1}

# Negative examples: random word pairs
{"input": "hello", "context": "say _ world", "target": "VoxCore", "label": 0}
```

### Alternative: Simpler ML First
Before neural networks, try:
1. **Naive Bayes** - P(correction | features)
2. **Random Forest** - Feature importance is interpretable
3. **KNN with phonetic distance** - No training needed

### Evaluation Metrics
- Precision: Of suggested corrections, how many are right?
- Recall: Of actual errors, how many did we catch?
- User acceptance rate: Did user approve the suggestion?

## Integration with Existing System

### VoxCore (Baseline)
```
DictionaryProcessor (priority: 90)
├── Loads static dictionary (current behavior)
├── NEW: Also loads learned dictionary from VoxCompose
├── Applies corrections in order: learned (high confidence) → static
```

### VoxCompose (Enhancement)
```
Learning Pipeline (background job):
├── Runs periodically or on-demand
├── Analyzes new transcriptions
├── Updates learned dictionary
├── Exports to format VoxCore can read
```

### Data Flow
```
1. User speaks → Whisper transcribes → VoxCore post-processes
                                              │
                                              ▼
2. User edits (optional) ──────────────────────┐
                                               │
3. VoxCompose corpus miner ◀───────────────────┘
         │
         ▼
4. Error detection + learning
         │
         ▼
5. Updated dictionary → VoxCore uses on next transcription
```

## Roadmap

### Phase 1: Corpus Mining (MVP)
- [ ] Scan VoiceNotes directory
- [ ] Parse existing transcriptions
- [ ] Build frequency analysis
- [ ] Output candidate list for manual review

### Phase 2: Manual Learning
- [ ] CLI tool: `voxcompose learn --approve`
- [ ] Review candidates, approve/reject
- [ ] Export to dictionary format
- [ ] VoxCore loads learned dictionary

### Phase 3: Automatic Detection
- [ ] Implement edit distance analysis
- [ ] Add phonetic similarity scoring
- [ ] Detect statistical anomalies
- [ ] Confidence thresholding

### Phase 4: Neural Network (Learning Project)
- [ ] Build training dataset from phases 1-3
- [ ] Implement simple PyTorch model
- [ ] Train and evaluate
- [ ] Compare to rule-based approach

### Phase 5: API & Interop
- [ ] REST API for dictionary management
- [ ] Export/import in standard formats
- [ ] Documentation for third-party use

## Ideas & Future Directions

### Context-Aware Corrections
Not just "vox core" → "VoxCore" always, but:
- "the vox core project" → "the VoxCore project"
- "vox core is great" → "VoxCore is great"
- "my vox core" → "my voice core" (different meaning!)

### Speaker Profiles
Different corrections for different contexts:
- Work mode: technical jargon, project names
- Personal mode: names of friends, places
- Creative mode: unusual words, neologisms

### Feedback Loop
- Track which corrections user accepts/rejects
- Adjust confidence scores based on feedback
- Remove entries that are consistently rejected

### Collaborative Dictionaries
- Share domain-specific dictionaries (medical, legal, tech)
- Community-contributed corrections
- Privacy-preserving aggregation

### Real-Time Learning
- Learn during transcription, not just after
- "Did you mean X?" prompts
- Immediate feedback integration

## Open Questions

1. **Privacy**: How to learn without storing raw audio?
2. **Conflicts**: What if learned correction contradicts static dictionary?
3. **Decay**: Should old corrections lose confidence over time?
4. **Context scope**: How much context is enough? Too much?
5. **Cold start**: What to do with no history yet?

## References

- [Hunspell](http://hunspell.github.io/) - Open source spell checker
- [SymSpell](https://github.com/wolfgarbe/SymSpell) - Fast spelling correction
- [Translation Memory](https://en.wikipedia.org/wiki/Translation_memory) - Industry approach
- [Phonetic Algorithms](https://en.wikipedia.org/wiki/Phonetic_algorithm) - Soundex, Metaphone
- [fastText](https://fasttext.cc/) - Word embeddings for context

---

*This is an idea document. Implementation details will evolve.*
