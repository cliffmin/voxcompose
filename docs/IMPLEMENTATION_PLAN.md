# VoxCompose Implementation Plan: Self-Learning & Dynamic Configuration

## Overview
Transform VoxCompose from a simple refiner into an intelligent, self-improving system that learns from usage and dynamically adjusts its behavior.

## Phase 1: Capability Negotiation (1-2 hours)

### 1.1 Add --capabilities Endpoint
```java
// In Main.java
if (args.length > 0 && args[0].equals("--capabilities")) {
    Capabilities caps = new Capabilities();
    caps.setActivationThreshold(getUserLearnedThreshold());
    caps.setPreferredModel(getUserPreferredModel());
    System.out.println(gson.toJson(caps));
    System.exit(0);
}
```

### 1.2 Capabilities Response Structure
```json
{
  "version": "1.1",
  "activation": {
    "long_form": {
      "min_duration": 21,  // Learned from user patterns
      "optimal_duration": 30,  // Sweet spot for this user
      "confidence": 0.85
    }
  },
  "learning": {
    "enabled": true,
    "corrections_learned": 247,
    "last_updated": "2024-01-15T10:30:00Z"
  },
  "preferences": {
    "whisper_model": "medium.en",  // Based on accuracy needs
    "refine_aggressiveness": "moderate"  // User's style
  }
}
```

### 1.3 Implementation Tasks
- [ ] Create `Capabilities` class
- [ ] Add JSON serialization
- [ ] Load from user profile if exists
- [ ] Return sensible defaults if no profile

## Phase 2: Self-Learning Corrections (2-3 days)

### 2.1 Learning Pipeline
```
Input → Refine → Output
   ↓        ↓        ↓
   └────Analysis────┘
           ↓
    Learn Patterns
           ↓
    Update Profile
```

### 2.2 Core Learning System
```java
public class LearningService {
    private final Path profilePath = Paths.get(
        System.getProperty("user.home"), 
        ".config/voxcompose/learned_profile.json"
    );
    
    public void learn(String input, String refined) {
        // 1. Diff analysis
        List<Correction> corrections = extractCorrections(input, refined);
        
        // 2. Pattern recognition
        Map<String, String> patterns = identifyPatterns(corrections);
        
        // 3. Update profile
        UserProfile profile = loadProfile();
        profile.addCorrections(patterns);
        profile.updateStatistics();
        saveProfile(profile);
    }
    
    private List<Correction> extractCorrections(String input, String refined) {
        // Use diff algorithm to find changes
        // Focus on:
        // - Concatenated words: "pushto" → "push to"
        // - Capitalization: "github" → "GitHub"
        // - Technical terms: "json" → "JSON"
        // - Punctuation additions
    }
}
```

### 2.3 User Profile Structure
```java
public class UserProfile {
    // Learned corrections
    private Map<String, String> wordCorrections;  // "pushto" → "push to"
    private Map<String, String> capitalizations;  // "github" → "GitHub"
    private List<String> technicalVocabulary;     // ["API", "JSON", "GitHub"]
    
    // Usage patterns
    private double averageInputLength;
    private double averageRefinementTime;
    private int totalRefinements;
    
    // Optimization thresholds
    private int minDurationForRefinement = 21;  // Learned optimal
    private double refinementValueThreshold;     // When refinement adds value
    
    // Statistics
    private LocalDateTime lastUpdated;
    private int correctionsApplied;
    private double accuracyImprovement;
}
```

### 2.4 Apply Learned Corrections
```java
public class CorrectionApplier {
    private final UserProfile profile;
    
    public String preProcess(String input) {
        String corrected = input;
        
        // Apply learned word corrections
        for (Map.Entry<String, String> entry : profile.getWordCorrections()) {
            corrected = corrected.replaceAll(
                "\\b" + Pattern.quote(entry.getKey()) + "\\b",
                entry.getValue()
            );
        }
        
        // Apply technical vocabulary
        corrected = fixTechnicalTerms(corrected);
        
        return corrected;
    }
}
```

## Phase 3: Dynamic Duration Threshold (1 day)

### 3.1 Threshold Learning Algorithm
```java
public class ThresholdOptimizer {
    private static final int MIN_SAMPLES = 10;
    private List<RefinementMetric> history = new ArrayList<>();
    
    public int calculateOptimalThreshold() {
        if (history.size() < MIN_SAMPLES) {
            return 21; // Default
        }
        
        // Analyze refinement value vs duration
        Map<Integer, Double> valueByDuration = new HashMap<>();
        
        for (RefinementMetric m : history) {
            int bucket = (int)(m.duration / 5) * 5; // 5-second buckets
            double value = calculateRefinementValue(m);
            valueByDuration.merge(bucket, value, Double::sum);
        }
        
        // Find duration where refinement starts adding significant value
        return findValueThreshold(valueByDuration);
    }
    
    private double calculateRefinementValue(RefinementMetric m) {
        // Factors:
        // - Number of corrections made
        // - Improvement in structure (paragraphs, bullets)
        // - Technical term corrections
        // - User didn't manually edit after
        
        double value = 0.0;
        value += m.correctionCount * 0.2;
        value += m.structureImprovement * 0.3;
        value += m.technicalTermsFixed * 0.3;
        value += m.userAccepted ? 0.2 : 0.0;
        return value;
    }
}
```

### 3.2 Metrics Collection
```java
public class RefinementMetric {
    public final double duration;           // Input audio duration
    public final int inputWords;           // Word count before
    public final int outputWords;          // Word count after
    public final int correctionCount;      // Number of changes
    public final double structureImprovement; // Added formatting
    public final int technicalTermsFixed;  // Technical corrections
    public final boolean userAccepted;     // Not manually edited after
    public final long refinementTimeMs;    // Processing time
}
```

## Phase 4: Integration Architecture (1 day)

### 4.1 Refactored Main Class
```java
public class Main {
    private final Configuration config;
    private final LearningService learner;
    private final ThresholdOptimizer thresholdOpt;
    private final OllamaClient ollama;
    private final CorrectionApplier corrector;
    
    public static void main(String[] args) {
        // Handle --capabilities
        if (isCapabilitiesRequest(args)) {
            outputCapabilities();
            return;
        }
        
        // Load components
        Main app = new Main();
        
        // Process input
        String input = app.readInput();
        
        // Pre-process with learned corrections
        String preprocessed = app.corrector.preProcess(input);
        
        // Check if refinement needed (duration-based)
        if (app.shouldRefine(input, args)) {
            String refined = app.refine(preprocessed);
            
            // Learn from this refinement (async)
            app.learner.learnAsync(input, refined);
            
            System.out.print(refined);
        } else {
            System.out.print(preprocessed); // Just corrections
        }
    }
}
```

### 4.2 Storage Structure
```
~/.config/voxcompose/
├── learned_profile.json       # User corrections & patterns
├── metrics.jsonl              # Historical refinement metrics
├── thresholds.json           # Learned optimal thresholds
└── cache/                    # Cached refinements
    └── [hash].json
```

## Phase 5: Ollama Integration Improvements (1 day)

### 5.1 Smarter Prompting
```java
public class PromptBuilder {
    public String buildSystemPrompt(UserProfile profile) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are VoxCompose. ");
        
        // Add user-specific context
        if (!profile.getTechnicalVocabulary().isEmpty()) {
            prompt.append("User's technical vocabulary: ")
                  .append(String.join(", ", profile.getTechnicalVocabulary()))
                  .append(". ");
        }
        
        // Add learned style preferences
        if (profile.prefersButtons()) {
            prompt.append("User prefers bullet points. ");
        }
        
        // Add common corrections
        if (!profile.getCommonErrors().isEmpty()) {
            prompt.append("Common transcription errors to fix: ");
            for (Map.Entry<String, String> e : profile.getCommonErrors()) {
                prompt.append(e.getKey()).append("→").append(e.getValue()).append(", ");
            }
        }
        
        return prompt.toString();
    }
}
```

### 5.2 Retry with Backoff
```java
public class ResilientOllamaClient {
    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_BACKOFF = 500;
    
    public String refine(String input, String systemPrompt) {
        int attempt = 0;
        long backoff = INITIAL_BACKOFF;
        
        while (attempt < MAX_RETRIES) {
            try {
                return callOllama(input, systemPrompt);
            } catch (IOException e) {
                if (++attempt >= MAX_RETRIES) {
                    // Fall back to just corrections
                    return corrector.preProcess(input);
                }
                sleep(backoff);
                backoff *= 2;
            }
        }
        return input;
    }
}
```

## Phase 6: Testing Strategy

### 6.1 Unit Tests
```java
@Test
void testLearnsCommonCorrections() {
    LearningService learner = new LearningService();
    learner.learn("pushto github", "push to GitHub");
    
    UserProfile profile = learner.getProfile();
    assertEquals("push to", profile.getCorrection("pushto"));
    assertEquals("GitHub", profile.getCapitalization("github"));
}

@Test 
void testThresholdOptimization() {
    ThresholdOptimizer opt = new ThresholdOptimizer();
    // Add samples with clear value distinction
    opt.addMetric(new RefinementMetric(10, 0.2)); // Low value
    opt.addMetric(new RefinementMetric(25, 0.8)); // High value
    
    int threshold = opt.calculateOptimalThreshold();
    assertTrue(threshold >= 15 && threshold <= 25);
}
```

### 6.2 Integration Tests
```bash
# Test capability negotiation
voxcompose --capabilities | jq .activation.long_form.min_duration

# Test learning
echo "test pushto github" | voxcompose --learn
echo "another pushto test" | voxcompose  # Should auto-correct

# Test threshold adaptation
./test_various_durations.sh  # Feed different lengths
voxcompose --show-stats  # Check learned threshold
```

## Implementation Priority

### Week 1: Core Features
1. **Day 1**: Capability negotiation endpoint
2. **Day 2-3**: Basic learning system (word corrections)
3. **Day 4**: Dynamic threshold calculation
4. **Day 5**: Integration and testing

### Week 2: Enhancement
1. **Day 1-2**: Advanced pattern recognition
2. **Day 3**: Caching layer for performance
3. **Day 4**: Metrics and analytics
4. **Day 5**: Documentation and examples

## Success Metrics

1. **Correction Accuracy**: 90%+ of common errors auto-fixed
2. **Threshold Optimization**: Reduces unnecessary LLM calls by 30%
3. **Performance**: <100ms overhead for corrections
4. **Learning Rate**: Improves accuracy after 10+ uses
5. **User Satisfaction**: No manual config needed

## Configuration Example

```json
{
  "learning": {
    "enabled": true,
    "auto_apply": true,
    "min_samples": 5,
    "max_corrections": 500
  },
  "thresholds": {
    "initial": 21,
    "learning_enabled": true,
    "min_value_for_refinement": 0.6
  },
  "ollama": {
    "retry_enabled": true,
    "max_retries": 3,
    "timeout_ms": 10000
  }
}
```

## Key Design Decisions

1. **All learning is local** - Privacy first
2. **Profile is portable** - Can backup/restore
3. **Graceful degradation** - Works without Ollama
4. **Non-blocking learning** - Async processing
5. **Deterministic corrections** - Consistent results

## Next Steps

1. Start with Phase 1 (capabilities) - immediate value
2. Build Phase 2 (learning) incrementally
3. Test with real usage data
4. Iterate based on patterns observed