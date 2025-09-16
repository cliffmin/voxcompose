package dev.voxcompose.dictionary;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Manages dictionaries for term correction and text refinement.
 * Supports loading from YAML/JSON files, priority-based layering, and pattern matching.
 */
public class DictionaryManager {
  private final List<Dictionary> dictionaries = new ArrayList<>();
  private final Path dictionaryPath;
  private final Gson gson = new Gson();

  public DictionaryManager() {
    this(Paths.get(System.getProperty("user.home"), ".voxcompose", "dictionaries"));
  }

  public DictionaryManager(Path dictionaryPath) {
    this.dictionaryPath = dictionaryPath;
    loadDefaultDictionaries();
  }

  /**
   * Load all dictionaries from the default directory
   */
  private void loadDefaultDictionaries() {
    if (!Files.exists(dictionaryPath)) {
      try {
        Files.createDirectories(dictionaryPath);
        // Copy built-in dictionaries on first run
        copyBuiltInDictionaries();
      } catch (IOException e) {
        System.err.println("Failed to create dictionary directory: " + e.getMessage());
      }
    }

    // Load all .yaml and .json files from dictionary directory
    try (Stream<Path> paths = Files.walk(dictionaryPath, 1)) {
      paths
          .filter(Files::isRegularFile)
          .filter(p -> p.toString().endsWith(".yaml") || p.toString().endsWith(".json"))
          .forEach(this::loadDictionary);
    } catch (IOException e) {
      System.err.println("Failed to load dictionaries: " + e.getMessage());
    }

    // Sort by priority
    dictionaries.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));
  }

  /**
   * Copy built-in dictionaries from resources
   */
  private void copyBuiltInDictionaries() {
    // In a real implementation, copy from resources/dictionaries/
    System.out.println("Initializing default dictionaries...");
  }

  /**
   * Load a dictionary from file
   */
  public void loadDictionary(Path file) {
    try {
      String content = Files.readString(file);
      Dictionary dict;

      if (file.toString().endsWith(".yaml")) {
        dict = parseYamlDictionary(content);
      } else {
        dict = gson.fromJson(content, Dictionary.class);
      }

      if (dict != null && dict.isEnabled()) {
        dictionaries.add(dict);
        System.out.println("Loaded dictionary: " + dict.getName());
      }
    } catch (IOException e) {
      System.err.println("Failed to load dictionary " + file + ": " + e.getMessage());
    }
  }

  /**
   * Simple YAML parser for dictionary files
   */
  private Dictionary parseYamlDictionary(String yaml) {
    // Simplified YAML parsing - in production use SnakeYAML
    Dictionary dict = new Dictionary();
    Map<String, String> terms = new HashMap<>();
    List<BoundaryRule> boundaries = new ArrayList<>();

    String[] lines = yaml.split("\n");
    String currentSection = "";
    BoundaryRule currentBoundary = null;

    for (String line : lines) {
      if (line.startsWith("name:")) {
        dict.setName(line.substring(5).trim());
      } else if (line.startsWith("priority:")) {
        dict.setPriority(Integer.parseInt(line.substring(9).trim()));
      } else if (line.startsWith("enabled:")) {
        dict.setEnabled(Boolean.parseBoolean(line.substring(8).trim()));
      } else if (line.trim().equals("terms:")) {
        currentSection = "terms";
      } else if (line.trim().equals("boundaries:")) {
        currentSection = "boundaries";
      } else if (currentSection.equals("terms") && line.contains(":") && line.startsWith("  ")) {
        String[] parts = line.trim().split(":", 2);
        if (parts.length == 2 && !parts[0].startsWith("#")) {
          terms.put(parts[0].trim(), parts[1].trim());
        }
      } else if (currentSection.equals("boundaries")) {
        if (line.trim().startsWith("- pattern:")) {
          // Start of new boundary rule
          if (currentBoundary != null) {
            boundaries.add(currentBoundary);
          }
          currentBoundary = new BoundaryRule();
          String pattern = line.substring(line.indexOf("pattern:") + 8).trim();
          // Remove quotes if present
          if (pattern.startsWith("\"") && pattern.endsWith("\"")) {
            pattern = pattern.substring(1, pattern.length() - 1);
          }
          currentBoundary.pattern = pattern;
        } else if (line.trim().startsWith("replacement:") && currentBoundary != null) {
          String replacement = line.substring(line.indexOf("replacement:") + 12).trim();
          // Remove quotes if present
          if (replacement.startsWith("\"") && replacement.endsWith("\"")) {
            replacement = replacement.substring(1, replacement.length() - 1);
          }
          currentBoundary.replacement = replacement;
        } else if (line.trim().startsWith("context:") && currentBoundary != null) {
          String context = line.substring(line.indexOf("context:") + 8).trim();
          if (context.startsWith("\"") && context.endsWith("\"")) {
            context = context.substring(1, context.length() - 1);
          }
          currentBoundary.context = context;
        }
      }
    }

    // Add last boundary if exists
    if (currentBoundary != null && currentBoundary.pattern != null) {
      boundaries.add(currentBoundary);
    }

    dict.setTerms(terms);
    dict.setBoundaries(boundaries);
    return dict;
  }

  /**
   * Apply all dictionaries to input text
   */
  public String refine(String input) {
    if (input == null || input.isEmpty()) {
      return input;
    }

    String result = input;

    // Apply dictionaries in priority order
    for (Dictionary dict : dictionaries) {
      if (!dict.isEnabled()) continue;

      // Apply term corrections
      result = applyTerms(result, dict.getTerms());

      // Apply boundary fixes
      result = applyBoundaries(result, dict.getBoundaries());
    }

    return result;
  }

  /**
   * Apply term corrections from a dictionary
   */
  private String applyTerms(String input, Map<String, String> terms) {
    String result = input;

    for (Map.Entry<String, String> entry : terms.entrySet()) {
      String pattern = "\\b" + Pattern.quote(entry.getKey()) + "\\b";
      result = result.replaceAll("(?i)" + pattern, entry.getValue());
    }

    return result;
  }

  /**
   * Apply boundary rules to fix word concatenations
   */
  private String applyBoundaries(String input, List<BoundaryRule> boundaries) {
    String result = input;

    for (BoundaryRule rule : boundaries) {
      result = result.replaceAll(rule.getPattern(), rule.getReplacement());
    }

    return result;
  }

  /**
   * Get list of loaded dictionaries
   */
  public List<Dictionary> getDictionaries() {
    return new ArrayList<>(dictionaries);
  }

  /**
   * Enable/disable a dictionary by name
   */
  public void setDictionaryEnabled(String name, boolean enabled) {
    dictionaries.stream()
        .filter(d -> d.getName().equals(name))
        .forEach(d -> d.setEnabled(enabled));
  }

  /**
   * Dictionary data class
   */
  public static class Dictionary {
    private String name;
    private String version;
    private int priority = 100;
    private boolean enabled = true;
    private String description;
    private Map<String, String> terms = new HashMap<>();
    private List<BoundaryRule> boundaries = new ArrayList<>();

    // Getters and setters
    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public int getPriority() {
      return priority;
    }

    public void setPriority(int priority) {
      this.priority = priority;
    }

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public Map<String, String> getTerms() {
      return terms;
    }

    public void setTerms(Map<String, String> terms) {
      this.terms = terms;
    }

    public List<BoundaryRule> getBoundaries() {
      return boundaries;
    }

    public void setBoundaries(List<BoundaryRule> boundaries) {
      this.boundaries = boundaries;
    }
  }

  /**
   * Boundary rule for fixing word concatenations
   */
  public static class BoundaryRule {
    String pattern;
    String replacement;
    String context;

    public String getPattern() {
      return pattern;
    }

    public String getReplacement() {
      return replacement;
    }
    
    public String getContext() {
      return context;
    }
  }
}