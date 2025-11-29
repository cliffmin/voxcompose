package dev.voxcompose;

import dev.voxcompose.cache.RefineCache;
import dev.voxcompose.client.OllamaClient;
import dev.voxcompose.config.Configuration;
import dev.voxcompose.io.InputReader;
import dev.voxcompose.memory.MemoryManager;
import dev.voxcompose.model.Capabilities;
import dev.voxcompose.learning.LearningService;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * VoxCompose - Optimized main class with performance improvements.
 * Uses connection pooling, caching, and efficient I/O for better performance.
 */
public class Main {
  private static RefineCache cache = null;
  private static final String VERSION = resolveVersion();

  private static String resolveVersion() {
    Package pkg = Main.class.getPackage();
    if (pkg != null && pkg.getImplementationVersion() != null) {
      return pkg.getImplementationVersion();
    }
    return "0.4.4";
  }


  public static void main(String[] args) throws Exception {
    // Handle --version request early
    if (args.length > 0 && ("--version".equals(args[0]) || "-V".equals(args[0]))) {
      System.out.println(VERSION);
      System.exit(0);
    }

    // Handle --capabilities request
    if (args.length > 0 && args[0].equals("--capabilities")) {
      Capabilities caps = Capabilities.loadFromProfile();
      System.out.println(caps.toJson());
      System.exit(0);
    }
    
    // Parse configuration efficiently
    Configuration config = Configuration.parse(args);
    
    // Handle help flag
    if (config.isShowHelp()) {
      System.err.println(Configuration.getUsageText());
      System.exit(2);
    }

    
    // Read input efficiently
    String input;
    try {
      input = InputReader.readStdin().trim();
    } catch (IOException e) {
      System.err.println("Error reading input: " + e.getMessage());
      System.exit(1);
      return;
    }
    
    if (input.isEmpty()) {
      System.out.print("");
      return;
    }
    
    // Apply learned corrections even if refinement is disabled
    LearningService learner = LearningService.getInstance();
    String corrected = learner.applyCorrections(input);

    String finalOut = corrected;  // Start with corrected version
    boolean ok = false;
    long refineMs = 0;

    // Track whether we skip LLM refinement
    boolean skippedForDuration = false;
    boolean skippedByEnv = false;
    
    // Check duration threshold if provided
    if (config.getInputDurationSeconds() > 0) {
      int threshold = learner.getProfile().getMinDurationForRefinement();
      if (config.getInputDurationSeconds() < threshold) {
        skippedForDuration = true;
        System.err.println("INFO: Skipping LLM refinement - duration " +
                          config.getInputDurationSeconds() + "s below threshold " + threshold + "s");
      }
    }
    
    // Check if refinement is disabled
    if (!config.isRefineEnabled()) {
      skippedByEnv = true;
      System.err.println("INFO: LLM refinement disabled via VOX_REFINE");
    }

    int memoryUsedCount = 0;
    String cacheKey = null;

    if (!skippedForDuration && !skippedByEnv) {
      // Initialize cache if enabled
      if (config.isCacheEnabled()) {
        cache = new RefineCache(config.getCacheMaxSize(), config.getCacheTtlMs());
      }
      
      // Build system prompt with memory
      StringBuilder systemPrompt = new StringBuilder();
      systemPrompt.append("You are VoxCompose, a local note refiner. Output ")
                  .append(config.getFormat())
                  .append(" with clear structure. Use headings, bullets, short paragraphs. Preserve meaning; fix disfluencies.\n");
      
      // Process memory file efficiently
      if (config.getMemoryPath() != null) {
        List<String> memoryLines = MemoryManager.readMemoryLines(config.getMemoryPath(), 20);
        memoryUsedCount = memoryLines.size();
        String memoryPrompt = MemoryManager.buildMemoryPrompt(memoryLines);
        if (!memoryPrompt.isEmpty()) {
          systemPrompt.append(memoryPrompt);
        }
      }

      // Log configuration
      System.err.println("INFO: Using LLM model: " + config.getModel() + " (source=" + config.getModelSource() + ")");
      System.err.println("INFO: Using LLM endpoint: " + config.getEndpoint() + " (source=" + config.getEndpointSource() + ")");
      
      // Log refinement start (for backward compatibility with tests)
      if (config.getMemoryPath() != null) {
        System.err.println("INFO: Running LLM refinement with model: " + config.getModel() + 
                          " (memory=" + config.getMemoryPath().toString() + ")");
      } else {
        System.err.println("INFO: Running LLM refinement with model: " + config.getModel());
      }
      
      String finalSystemPrompt = systemPrompt.toString();
      
      // Check cache if enabled
      if (cache != null) {
        cacheKey = cache.generateKey(config.getModel(), input, finalSystemPrompt);
        String cachedResult = cache.get(cacheKey);
        if (cachedResult != null) {
          System.err.println("INFO: Using cached result");
          String correctedCached = learner.applyCorrections(cachedResult);
          finalOut = correctedCached;
          ok = true;
          System.out.print(finalOut);
          writeOptionalOutputs(config, finalOut, ok, refineMs, memoryUsedCount);
          return;
        }
      }
  
      // Create optimized Ollama client
      OllamaClient ollamaClient = new OllamaClient(config.getEndpoint(), config.getTimeoutMs());
      
      try {
        OllamaClient.RefineResult result = ollamaClient.refine(
          config.getModel(), 
          corrected,  // Use corrected input
          finalSystemPrompt
        );
        
        ok = result.success;
        refineMs = result.responseTimeMs;
        
        if (result.success && result.text != null) {
          finalOut = result.text;
          // Cache the result if caching is enabled
          if (cache != null && cacheKey != null) {
            cache.put(cacheKey, finalOut);
          }
          // Learn from this refinement (async)
          if (!input.equals(finalOut)) {
            learner.learnAsync(input, finalOut);
          }
        } else if (!result.success) {
          System.err.println(result.error != null ? result.error : "Refinement failed");
        }
      } catch (Exception e) {
        System.err.println("Ollama call failed: " + e.getMessage());
        ok = false;
      }
    } else {
      // No LLM run; still learn from deterministic corrections
      if (!input.equals(corrected)) {
        learner.learnAsync(input, corrected);
      }
      ok = true;
    }

    // Always apply corrections to final output
    if (!ok) {
      finalOut = corrected;  // Use corrected version if LLM failed
    }
    
    // Always print something to stdout
    System.out.print(finalOut);
    
    // Write optional outputs
    writeOptionalOutputs(config, finalOut, ok, refineMs, memoryUsedCount);
    
    // Cleanup
    OllamaClient.shutdown();
    
    if (!ok && !skippedForDuration && !skippedByEnv) System.exit(1);
  }
  
  /**
   * Write optional output files (--out and --sidecar flags).
   */
  private static void writeOptionalOutputs(Configuration config, String output, boolean success, 
                                           long refineMs, int memoryUsedCount) {
    // Write to output file if specified
    if (config.getOutPath() != null) {
      try {
        Files.write(Paths.get(config.getOutPath()), output.getBytes(StandardCharsets.UTF_8));
      } catch (IOException e) {
        System.err.println("Failed to write output file: " + e.getMessage());
      }
    }
    
    // Write sidecar JSON if specified
    if (config.getSidecarPath() != null) {
      try {
        JsonObject sidecar = new JsonObject();
        sidecar.addProperty("ok", success);
        sidecar.addProperty("provider", config.getProvider());
        sidecar.addProperty("model", config.getModel());
        sidecar.addProperty("model_source", config.getModelSource());
        sidecar.addProperty("endpoint", config.getEndpoint());
        sidecar.addProperty("endpoint_source", config.getEndpointSource());
        sidecar.addProperty("refine_ms", refineMs);
        sidecar.addProperty("memory_items_used", memoryUsedCount);
        if (cache != null) {
          RefineCache.CacheStats stats = cache.getStats();
          sidecar.addProperty("cache_hits", stats.valid);
          sidecar.addProperty("cache_size", stats.total);
        }
        Files.write(Paths.get(config.getSidecarPath()), 
                   sidecar.toString().getBytes(StandardCharsets.UTF_8));
      } catch (IOException e) {
        System.err.println("Failed to write sidecar file: " + e.getMessage());
      }
    }
  }
}
