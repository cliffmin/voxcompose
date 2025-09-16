package dev.voxcompose.learning;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.time.LocalDateTime;
import java.util.*;

/** User profile containing learned corrections and patterns. */
public class UserProfile {
  // Learned corrections
  private Map<String, String> wordCorrections = new HashMap<>();
  private Map<String, String> capitalizations = new HashMap<>();
  private List<String> technicalVocabulary = new ArrayList<>();

  // Usage patterns
  private double averageInputLength = 0;
  private double averageRefinementTime = 0;
  private int totalRefinements = 0;

  // Optimization thresholds
  private int minDurationForRefinement = 21;
  private double refinementValueThreshold = 0.6;

  // Statistics
  private String lastUpdated;
  private int correctionsApplied = 0;
  private double accuracyImprovement = 0;

  public UserProfile() {
    updateTimestamp();
  }

  public void addCorrection(String wrong, String right) {
    if (wrong != null && right != null && !wrong.equals(right)) {
      // Detect capitalization vs word correction
      if (wrong.equalsIgnoreCase(right)) {
        capitalizations.put(wrong.toLowerCase(), right);
      } else {
        wordCorrections.put(wrong, right);
      }
      updateTimestamp();
    }
  }

  public void addTechnicalTerm(String term) {
    if (term != null && !technicalVocabulary.contains(term)) {
      technicalVocabulary.add(term);
      updateTimestamp();
    }
  }

  public void updateStatistics(int inputLength, long refinementTimeMs) {
    totalRefinements++;

    // Update rolling average
    averageInputLength =
        ((averageInputLength * (totalRefinements - 1)) + inputLength) / totalRefinements;
    averageRefinementTime =
        ((averageRefinementTime * (totalRefinements - 1)) + refinementTimeMs) / totalRefinements;

    updateTimestamp();
  }

  public void updateThreshold(int newThreshold) {
    this.minDurationForRefinement = newThreshold;
    updateTimestamp();
  }

  private void updateTimestamp() {
    this.lastUpdated = LocalDateTime.now().toString();
  }

  // Getters
  public Map<String, String> getWordCorrections() {
    return new HashMap<>(wordCorrections);
  }

  public Map<String, String> getCapitalizations() {
    return new HashMap<>(capitalizations);
  }

  public List<String> getTechnicalVocabulary() {
    return new ArrayList<>(technicalVocabulary);
  }

  public int getMinDurationForRefinement() {
    return minDurationForRefinement;
  }

  public int getTotalRefinements() {
    return totalRefinements;
  }

  public int getCorrectionsCount() {
    return wordCorrections.size() + capitalizations.size();
  }

  public String toJson() {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    return gson.toJson(this);
  }

  public static UserProfile fromJson(String json) {
    Gson gson = new Gson();
    return gson.fromJson(json, UserProfile.class);
  }
}
