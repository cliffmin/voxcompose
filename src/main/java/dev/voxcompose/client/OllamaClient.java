package dev.voxcompose.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Optimized Ollama API client with connection pooling and efficient HTTP handling.
 * Reuses connections and maintains a connection pool for better performance.
 */
public class OllamaClient {
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json");
    private static final Gson GSON = new Gson();
    
    // Singleton OkHttpClient for connection reuse across all requests
    private static final OkHttpClient SHARED_CLIENT = new OkHttpClient.Builder()
        .connectionPool(new ConnectionPool(5, 5, TimeUnit.MINUTES))
        .connectTimeout(Duration.ofSeconds(5))
        .readTimeout(Duration.ofSeconds(30))
        .writeTimeout(Duration.ofSeconds(5))
        .retryOnConnectionFailure(true)
        .build();
    
    private final String endpoint;
    private final int timeoutMs;
    private final OkHttpClient client;
    
    /**
     * Create a new OllamaClient with specified endpoint and timeout.
     * Uses a shared connection pool for optimal performance.
     */
    public OllamaClient(String endpoint, int timeoutMs) {
        this.endpoint = endpoint;
        this.timeoutMs = timeoutMs;
        
        // Create client with custom timeout but shared connection pool
        this.client = SHARED_CLIENT.newBuilder()
            .callTimeout(Duration.ofMillis(timeoutMs))
            .build();
    }
    
    /**
     * Refine text using the Ollama API.
     * 
     * @param model The model name to use
     * @param prompt The input text to refine
     * @param systemPrompt The system prompt with instructions
     * @return The refined text, or null if the request failed
     * @throws IOException if the network request fails
     */
    public RefineResult refine(String model, String prompt, String systemPrompt) throws IOException {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", model);
        requestBody.addProperty("prompt", prompt);
        requestBody.addProperty("system", systemPrompt);
        requestBody.addProperty("stream", false);
        
        Request request = new Request.Builder()
            .url(endpoint)
            .post(RequestBody.create(GSON.toJson(requestBody), JSON_MEDIA_TYPE))
            .build();
        
        long startTime = System.currentTimeMillis();
        
        try (Response response = client.newCall(request).execute()) {
            long responseTime = System.currentTimeMillis() - startTime;
            
            if (!response.isSuccessful()) {
                return new RefineResult(false, null, responseTime, 
                    "Ollama error: " + response.code() + " " + response.message());
            }
            
            String body = response.body().string();
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            String refined = json.has("response") ? json.get("response").getAsString() : null;
            
            return new RefineResult(true, refined, responseTime, null);
        }
    }
    
    /**
     * Check if the Ollama service is available.
     * 
     * @return true if the service responds successfully
     */
    public boolean isAvailable() {
        Request request = new Request.Builder()
            .url(endpoint.replace("/api/generate", "/api/tags"))
            .get()
            .build();
        
        try (Response response = SHARED_CLIENT.newCall(request).execute()) {
            return response.isSuccessful();
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Result of a refinement operation.
     */
    public static class RefineResult {
        public final boolean success;
        public final String text;
        public final long responseTimeMs;
        public final String error;
        
        public RefineResult(boolean success, String text, long responseTimeMs, String error) {
            this.success = success;
            this.text = text;
            this.responseTimeMs = responseTimeMs;
            this.error = error;
        }
    }
    
    /**
     * Shutdown the shared connection pool.
     * Should be called when the application exits.
     */
    public static void shutdown() {
        SHARED_CLIENT.dispatcher().executorService().shutdown();
        SHARED_CLIENT.connectionPool().evictAll();
    }
}