package dev.voxcompose.memory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages memory JSONL file reading and processing.
 * Optimized for performance with buffered I/O and efficient parsing.
 */
public class MemoryManager {
    private static final int DEFAULT_MAX_ITEMS = 20;
    
    /**
     * Read and parse memory lines from a JSONL file.
     * Returns the most recent items up to the specified maximum.
     */
    public static List<String> readMemoryLines(Path memoryPath, int maxItems) {
        if (memoryPath == null || !Files.exists(memoryPath)) {
            return Collections.emptyList();
        }
        
        List<String> lines = new ArrayList<>();
        
        try (BufferedReader reader = Files.newBufferedReader(memoryPath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            // Log error and return empty list
            System.err.println("Error reading memory file: " + e.getMessage());
            return Collections.emptyList();
        }
        
        // Return the most recent items
        if (lines.size() > maxItems) {
            return lines.subList(lines.size() - maxItems, lines.size());
        }
        return lines;
    }
    
    /**
     * Build system prompt additions from memory lines.
     * Extracts and formats the 'text' field from each JSON object.
     */
    public static String buildMemoryPrompt(List<String> memoryLines) {
        if (memoryLines.isEmpty()) {
            return "";
        }
        
        StringBuilder prompt = new StringBuilder();
        prompt.append("Incorporate these user preferences/glossary items when appropriate (do not hallucinate):\n");
        
        for (String line : memoryLines) {
            try {
                JsonObject obj = JsonParser.parseString(line).getAsJsonObject();
                if (obj.has("text")) {
                    String text = obj.get("text").getAsString();
                    if (text != null && !text.isBlank()) {
                        prompt.append("- ").append(text.trim()).append("\n");
                    }
                }
            } catch (Exception e) {
                // Skip malformed lines silently
            }
        }
        
        return prompt.toString();
    }
    
    /**
     * Read memory with default maximum items.
     */
    public static List<String> readMemoryLines(Path memoryPath) {
        return readMemoryLines(memoryPath, DEFAULT_MAX_ITEMS);
    }
    
    /**
     * Parse and validate a memory line.
     * Returns null if the line is invalid.
     */
    public static MemoryItem parseMemoryLine(String line) {
        try {
            JsonObject obj = JsonParser.parseString(line).getAsJsonObject();
            
            String text = obj.has("text") ? obj.get("text").getAsString() : null;
            if (text == null || text.isBlank()) {
                return null;
            }
            
            String kind = obj.has("kind") ? obj.get("kind").getAsString() : "unknown";
            String ts = obj.has("ts") ? obj.get("ts").getAsString() : null;
            
            return new MemoryItem(text, kind, ts);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Represents a single memory item.
     */
    public static class MemoryItem {
        public final String text;
        public final String kind;
        public final String timestamp;
        
        public MemoryItem(String text, String kind, String timestamp) {
            this.text = text;
            this.kind = kind;
            this.timestamp = timestamp;
        }
    }
}