package dev.voxcompose.learning;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * Service for learning from refinements and applying corrections.
 */
public class LearningService {
    private static final Path PROFILE_PATH = Paths.get(
        System.getProperty("user.home"), ".config", "voxcompose", "learned_profile.json"
    );
    
    private static final int MIN_WORD_LENGTH = 3;
    private static final double MIN_SIMILARITY = 0.5;
    
    private UserProfile profile;
    private static LearningService instance;
    
    public LearningService() {
        loadProfile();
    }
    
    public static synchronized LearningService getInstance() {
        if (instance == null) {
            instance = new LearningService();
        }
        return instance;
    }
    
    /**
     * Load profile from disk or create new one.
     */
    private void loadProfile() {
        try {
            if (Files.exists(PROFILE_PATH)) {
                String json = new String(Files.readAllBytes(PROFILE_PATH), StandardCharsets.UTF_8);
                profile = UserProfile.fromJson(json);
            } else {
                profile = new UserProfile();
            }
        } catch (IOException e) {
            System.err.println("Failed to load profile: " + e.getMessage());
            profile = new UserProfile();
        }
    }
    
    /**
     * Save profile to disk.
     */
    private void saveProfile() {
        try {
            Files.createDirectories(PROFILE_PATH.getParent());
            Files.write(PROFILE_PATH, profile.toJson().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.err.println("Failed to save profile: " + e.getMessage());
        }
    }
    
    /**
     * Apply learned corrections to input text.
     */
    public String applyCorrections(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        String corrected = input;
        
        // Apply word corrections
        for (Map.Entry<String, String> entry : profile.getWordCorrections().entrySet()) {
            String pattern = "\\b" + Pattern.quote(entry.getKey()) + "\\b";
            corrected = corrected.replaceAll(pattern, entry.getValue());
        }
        
        // Apply capitalizations
        for (Map.Entry<String, String> entry : profile.getCapitalizations().entrySet()) {
            String pattern = "\\b(?i)" + Pattern.quote(entry.getKey()) + "\\b";
            corrected = corrected.replaceAll(pattern, entry.getValue());
        }
        
        // Fix common concatenations
        corrected = fixCommonConcatenations(corrected);
        
        return corrected;
    }
    
    /**
     * Fix common word concatenations.
     */
    private String fixCommonConcatenations(String text) {
        // Common patterns
        String[] suffixes = {"would", "should", "could", "will", "have", "been", "into", "with", "to"};
        
        for (String suffix : suffixes) {
            // Pattern: word+suffix without space
            String pattern = "\\b([a-z]+)(" + suffix + ")\\b";
            text = text.replaceAll(pattern, "$1 $2");
        }
        
        // Fix specific known issues
        text = text.replaceAll("\\bpushto\\b", "push to");
        text = text.replaceAll("\\bcommitthis\\b", "commit this");
        text = text.replaceAll("\\bgithub\\b", "GitHub");
        text = text.replaceAll("\\bjson\\b", "JSON");
        
        return text;
    }
    
    /**
     * Learn from a refinement (async).
     */
    public void learnAsync(String input, String refined) {
        CompletableFuture.runAsync(() -> learn(input, refined));
    }
    
    /**
     * Learn from a refinement.
     */
    public void learn(String input, String refined) {
        if (input == null || refined == null || input.equals(refined)) {
            return;
        }
        
        // Extract corrections
        List<Correction> corrections = extractCorrections(input, refined);
        
        // Add to profile
        for (Correction c : corrections) {
            profile.addCorrection(c.wrong, c.right);
        }
        
        // Update statistics
        profile.updateStatistics(input.length(), System.currentTimeMillis());
        
        // Save profile
        saveProfile();
    }
    
    /**
     * Extract corrections from input/refined pair.
     */
    private List<Correction> extractCorrections(String input, String refined) {
        List<Correction> corrections = new ArrayList<>();
        
        // Simple word-level comparison
        String[] inputWords = input.toLowerCase().split("\\s+");
        String[] refinedWords = refined.toLowerCase().split("\\s+");
        
        // Use simple algorithm for now
        int minLen = Math.min(inputWords.length, refinedWords.length);
        
        for (int i = 0; i < minLen; i++) {
            String inputWord = inputWords[i];
            String refinedWord = refinedWords[i];
            
            if (!inputWord.equals(refinedWord) && isValidCorrection(inputWord, refinedWord)) {
                corrections.add(new Correction(inputWord, refinedWord));
            }
        }
        
        return corrections;
    }
    
    /**
     * Check if a correction is valid.
     */
    private boolean isValidCorrection(String wrong, String right) {
        if (wrong.length() < MIN_WORD_LENGTH && right.length() < MIN_WORD_LENGTH) {
            return false;
        }
        
        // Check similarity
        double similarity = calculateSimilarity(wrong, right);
        if (similarity < MIN_SIMILARITY && !wrong.equalsIgnoreCase(right)) {
            return false;
        }
        
        // Don't correct numbers
        if (wrong.matches("\\d+") || right.matches("\\d+")) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Calculate string similarity (simple algorithm).
     */
    private double calculateSimilarity(String s1, String s2) {
        if (s1.equalsIgnoreCase(s2)) {
            return 1.0;
        }
        
        int maxLen = Math.max(s1.length(), s2.length());
        int distance = levenshteinDistance(s1, s2);
        return 1.0 - (double) distance / maxLen;
    }
    
    /**
     * Calculate Levenshtein distance.
     */
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(dp[i - 1][j] + 1,
                           Math.min(dp[i][j - 1] + 1, dp[i - 1][j - 1] + cost));
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
    
    // Static helper methods for easy access
    public static int getLearnedThreshold() {
        return getInstance().profile.getMinDurationForRefinement();
    }
    
    public int getCorrectionCount() {
        return profile.getCorrectionsCount();
    }
    
    public int getTotalRefinements() {
        return profile.getTotalRefinements();
    }
    
    public UserProfile getProfile() {
        return profile;
    }
    
    /**
     * Simple correction pair.
     */
    private static class Correction {
        final String wrong;
        final String right;
        
        Correction(String wrong, String right) {
            this.wrong = wrong;
            this.right = right;
        }
    }
}