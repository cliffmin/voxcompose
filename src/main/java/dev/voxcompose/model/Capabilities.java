package dev.voxcompose.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import dev.voxcompose.learning.LearningService;
import dev.voxcompose.learning.UserProfile;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents VoxCompose capabilities for plugin negotiation.
 * This allows VoxCompose to communicate its requirements and preferences
 * to the calling application (e.g., macos-ptt-dictation).
 */
public class Capabilities {
    private String version = "1.1";
    private Activation activation;
    private Learning learning;
    private Preferences preferences;

    public Capabilities() {
        // Initialize with defaults
        this.activation = new Activation();
        this.learning = new Learning();
        this.preferences = new Preferences();
    }

    public static class Activation {
        @SerializedName("long_form")
        private LongForm longForm;

        public Activation() {
            this.longForm = new LongForm();
        }

        public static class LongForm {
            @SerializedName("min_duration")
            private int minDuration = 21;
            
            @SerializedName("optimal_duration")
            private int optimalDuration = 30;
            
            private double confidence = 0.85;
            
            private String description = "Minimum seconds for LLM refinement";

            // Getters and setters
            public int getMinDuration() { return minDuration; }
            public void setMinDuration(int minDuration) { this.minDuration = minDuration; }
            
            public int getOptimalDuration() { return optimalDuration; }
            public void setOptimalDuration(int optimalDuration) { this.optimalDuration = optimalDuration; }
            
            public double getConfidence() { return confidence; }
            public void setConfidence(double confidence) { this.confidence = confidence; }
            
            public String getDescription() { return description; }
        }

        public LongForm getLongForm() { return longForm; }
    }

    public static class Learning {
        private boolean enabled = false;  // Will be true once implemented
        
        @SerializedName("corrections_learned")
        private int correctionsLearned = 0;
        
        @SerializedName("last_updated")
        private String lastUpdated = null;

        // Getters and setters
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        
        public int getCorrectionsLearned() { return correctionsLearned; }
        public void setCorrectionsLearned(int count) { this.correctionsLearned = count; }
        
        public String getLastUpdated() { return lastUpdated; }
        public void setLastUpdated(String timestamp) { this.lastUpdated = timestamp; }
    }

    public static class Preferences {
        @SerializedName("whisper_model")
        private String whisperModel = "medium.en";
        
        @SerializedName("whisper_impl")
        private String whisperImpl = "whisper-cpp";
        
        @SerializedName("refine_aggressiveness")
        private String refineAggressiveness = "moderate";

        // Getters and setters
        public String getWhisperModel() { return whisperModel; }
        public void setWhisperModel(String model) { this.whisperModel = model; }
        
        public String getWhisperImpl() { return whisperImpl; }
        public void setWhisperImpl(String impl) { this.whisperImpl = impl; }
        
        public String getRefineAggressiveness() { return refineAggressiveness; }
        public void setRefineAggressiveness(String level) { this.refineAggressiveness = level; }
    }

    // Main getters
    public String getVersion() { return version; }
    public Activation getActivation() { return activation; }
    public Learning getLearning() { return learning; }
    public Preferences getPreferences() { return preferences; }

    /**
     * Load capabilities from user profile if it exists.
     */
    public static Capabilities loadFromProfile() {
        Capabilities caps = new Capabilities();
        
        // Load from learning service
        LearningService learner = LearningService.getInstance();
        UserProfile profile = learner.getProfile();
        
        if (profile != null) {
            // Update threshold from learned profile
            caps.activation.longForm.minDuration = profile.getMinDurationForRefinement();
            
            // Update learning stats
            caps.learning.enabled = true;
            caps.learning.correctionsLearned = profile.getCorrectionsCount();
            caps.learning.lastUpdated = LocalDateTime.now().format(
                DateTimeFormatter.ISO_LOCAL_DATE_TIME
            );
            
            // Log for debugging
            System.err.println("INFO: Loaded profile with threshold: " + 
                             profile.getMinDurationForRefinement() + "s, " +
                             profile.getCorrectionsCount() + " corrections");
        } else {
            System.err.println("INFO: No profile found, using defaults");
        }
        
        return caps;
    }

    /**
     * Convert to JSON string for output.
     */
    public String toJson() {
        Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();
        return gson.toJson(this);
    }
}