# Adding Capabilities to Main.java

## Quick Integration (Add to beginning of main method)

```java
// In Main.java, at the start of main() method:

public static void main(String[] args) throws Exception {
    // Handle --capabilities request
    if (args.length > 0 && args[0].equals("--capabilities")) {
        Capabilities caps = Capabilities.loadFromProfile();
        System.out.println(caps.toJson());
        System.exit(0);
    }
    
    // Rest of existing code...
```

## Complete Example with Learning Integration

```java
package dev.voxcompose;

import dev.voxcompose.model.Capabilities;
import dev.voxcompose.learning.LearningService;
// ... other imports

public class Main {
    public static void main(String[] args) throws Exception {
        // Handle --capabilities request
        if (args.length > 0 && args[0].equals("--capabilities")) {
            Capabilities caps = Capabilities.loadFromProfile();
            System.out.println(caps.toJson());
            System.exit(0);
        }
        
        // Handle --show-stats request
        if (args.length > 0 && args[0].equals("--show-stats")) {
            showLearningStats();
            System.exit(0);
        }
        
        // Existing help handling
        if (showHelp) {
            // ... existing help code
        }
        
        // Read input
        String input = readAll(System.in).trim();
        if (input.isEmpty()) {
            System.out.print("");
            return;
        }
        
        // Apply learned corrections before refinement
        LearningService learner = new LearningService();
        String corrected = learner.applyCorrections(input);
        
        // Check if refinement is needed (can be duration-based)
        boolean shouldRefine = shouldRefineContent(input, args);
        
        if (!shouldRefine) {
            // Just output with corrections
            System.out.print(corrected);
            return;
        }
        
        // Existing refinement logic...
        // ... rest of the code
        
        // After successful refinement, learn from it
        if (ok && refined != null && !refined.equals(input)) {
            learner.learnAsync(input, refined);
        }
    }
    
    private static boolean shouldRefineContent(String input, String[] args) {
        // Check if --no-refine flag is present
        for (String arg : args) {
            if (arg.equals("--no-refine")) {
                return false;
            }
        }
        
        // Check duration hint from args (if provided by PTT)
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equals("--duration") && i + 1 < args.length) {
                try {
                    double duration = Double.parseDouble(args[i + 1]);
                    
                    // Load learned threshold
                    int threshold = LearningService.getLearnedThreshold();
                    
                    if (duration < threshold) {
                        return false; // Too short for refinement
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        
        // Default to refining
        return true;
    }
    
    private static void showLearningStats() {
        LearningService learner = new LearningService();
        System.out.println("=== VoxCompose Learning Statistics ===");
        System.out.println("Corrections learned: " + learner.getCorrectionCount());
        System.out.println("Optimal threshold: " + learner.getLearnedThreshold() + "s");
        System.out.println("Total refinements: " + learner.getTotalRefinements());
        System.out.println("Profile location: ~/.config/voxcompose/");
    }
}
```

## Testing the Integration

```bash
# Test capabilities endpoint
voxcompose --capabilities

# Expected output:
{
  "version": "1.1",
  "activation": {
    "long_form": {
      "min_duration": 21,
      "optimal_duration": 30,
      "confidence": 0.85,
      "description": "Minimum seconds for LLM refinement"
    }
  },
  "learning": {
    "enabled": false,
    "corrections_learned": 0,
    "last_updated": null
  },
  "preferences": {
    "whisper_model": "medium.en",
    "whisper_impl": "whisper-cpp",
    "refine_aggressiveness": "moderate"
  }
}

# Test with PTT integration
echo "test input" | voxcompose --duration 15  # Should skip refinement
echo "longer test input that needs refinement" | voxcompose --duration 25  # Should refine
```

## Next Steps

1. Implement the `LearningService` class
2. Add profile persistence to disk
3. Implement threshold optimization
4. Add async learning to not block output
5. Test with real transcription data