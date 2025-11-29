package dev.voxcompose.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * Configuration management for VoxCompose.
 * Efficiently parses and stores all configuration values.
 */
public class Configuration {
    // Configuration values
    private String model = "llama3.1";
    private String modelSource = "default";
    private int timeoutMs = 10000;
    private String format = "markdown";
    private Path memoryPath = null;
    private String outPath = null;
    private String sidecarPath = null;
    private String provider = "ollama";
    private String endpoint = null;
    private String endpointSource = "default";
    private boolean refineEnabled = true;
    private boolean showHelp = false;
    private boolean enableCache = false;
    private int cacheMaxSize = 100;
    private long cacheTtlMs = 3600000; // 1 hour default
    private int inputDurationSeconds = 0; // Audio duration from caller
    
    /**
     * Parse configuration from command-line arguments and environment variables.
     */
    public static Configuration parse(String[] args) {
        Configuration config = new Configuration();
        
        // Parse command-line arguments first
        config.parseArgs(args);
        
        // If help was requested, return immediately
        if (config.showHelp) {
            return config;
        }
        
        // Parse environment variables
        config.parseEnvironment();
        
        // Build final endpoint
        config.buildEndpoint();
        
        return config;
    }
    
    private void parseArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--model":
                    if (i + 1 < args.length) {
                        model = args[++i];
                        modelSource = "flag";
                    }
                    break;
                case "--timeout-ms":
                    if (i + 1 < args.length) {
                        timeoutMs = Integer.parseInt(args[++i]);
                    }
                    break;
                case "--format":
                    if (i + 1 < args.length) {
                        format = args[++i];
                    }
                    break;
                case "--memory":
                    if (i + 1 < args.length) {
                        memoryPath = Paths.get(args[++i]);
                    }
                    break;
                case "--out":
                    if (i + 1 < args.length) {
                        outPath = args[++i];
                    }
                    break;
                case "--sidecar":
                    if (i + 1 < args.length) {
                        sidecarPath = args[++i];
                    }
                    break;
                case "--provider":
                    if (i + 1 < args.length) {
                        provider = args[++i];
                    }
                    break;
                case "--api-url":
                    if (i + 1 < args.length) {
                        String url = args[++i];
                        endpoint = normalizeEndpoint(url);
                        endpointSource = "flag";
                    }
                    break;
                case "--cache":
                    enableCache = true;
                    break;
                case "--cache-size":
                    if (i + 1 < args.length) {
                        cacheMaxSize = Integer.parseInt(args[++i]);
                    }
                    break;
                case "--cache-ttl-ms":
                    if (i + 1 < args.length) {
                        cacheTtlMs = Long.parseLong(args[++i]);
                    }
                    break;
                case "--duration":
                    if (i + 1 < args.length) {
                        inputDurationSeconds = Integer.parseInt(args[++i]);
                    }
                    break;
                case "--help":
                case "-h":
                    showHelp = true;
                    break;
            }
        }
    }
    
    private void parseEnvironment() {
        // Check VOX_REFINE toggle
        String refineEnv = System.getenv("VOX_REFINE");
        if (refineEnv != null) {
            String v = refineEnv.trim().toLowerCase(Locale.ROOT);
            refineEnabled = !(v.equals("0") || v.equals("false") || v.equals("no") || v.equals("off"));
        }
        
        // Model configuration
        if (!"flag".equals(modelSource)) {
            String envModel = System.getenv("AI_AGENT_MODEL");
            if (envModel != null && !envModel.isBlank()) {
                model = envModel.trim();
                modelSource = "AI_AGENT_MODEL";
            }
        }
        
        // Endpoint configuration (if not set via flag)
        if (endpoint == null) {
            String base = resolveEndpointBase();
            endpoint = normalizeEndpoint(base);
        }
        
        // Cache configuration from environment
        String cacheEnv = System.getenv("VOX_CACHE_ENABLED");
        if (cacheEnv != null && cacheEnv.trim().equals("1")) {
            enableCache = true;
        }
    }
    
    private String resolveEndpointBase() {
        String envApi = System.getenv("AI_AGENT_URL");
        String envOllama = System.getenv("OLLAMA_HOST");
        
        if (envApi != null && !envApi.isBlank()) {
            endpointSource = "AI_AGENT_URL";
            return envApi.trim();
        } else if (envOllama != null && !envOllama.isBlank()) {
            endpointSource = "OLLAMA_HOST";
            return envOllama.trim();
        } else {
            endpointSource = "default";
            return "http://127.0.0.1:11434";
        }
    }
    
    private String normalizeEndpoint(String base) {
        String normalized = base.replaceAll("/+$", "");
        if (!normalized.endsWith("/api/generate")) {
            return normalized + "/api/generate";
        }
        return normalized;
    }
    
    private void buildEndpoint() {
        // Endpoint is already built during parsing
    }
    
    public static String getUsageText() {
        return String.join("\n",
            "VoxCompose - local LLM Markdown refiner (Ollama)",
            "",
            "Usage:",
            "  voxcompose [flags] < input.txt > output.md",
            "",
            "Flags:",
            "  --model <name>         Model name (default: llama3.1)",
            "  --timeout-ms <ms>      HTTP call timeout (default: 10000)",
            "  --memory <jsonl-path>  Optional JSONL memory file",
            "  --format <fmt>         Output format (default: markdown)",
            "  --out <file>           Also write output to file",
            "  --sidecar <file>       Write JSON sidecar with metadata",
            "  --provider <name>      Provider name (default: ollama)",
            "  --api-url <url>        Override endpoint (base or full /api/generate)",
            "  --cache                Enable response caching",
            "  --cache-size <n>       Max cache entries (default: 100)",
            "  --cache-ttl-ms <ms>    Cache TTL in milliseconds (default: 3600000)",
            "  --duration <seconds>   Input audio duration (for threshold checking)",
            "  --version, -V          Print version and exit",
            "  --help, -h             Show this help and exit",
            "",
            "Environment (overridden by flags):",
            "  AI_AGENT_MODEL         Default model name",
            "  AI_AGENT_URL           Base URL (or full /api/generate)",
            "  OLLAMA_HOST            Ollama base URL",
            "  VOX_REFINE             Set 0/false to disable refinement",
            "  VOX_CACHE_ENABLED      Set 1 to enable caching"
        );
    }
    
    // Getters
    public String getModel() { return model; }
    public String getModelSource() { return modelSource; }
    public int getTimeoutMs() { return timeoutMs; }
    public String getFormat() { return format; }
    public Path getMemoryPath() { return memoryPath; }
    public String getOutPath() { return outPath; }
    public String getSidecarPath() { return sidecarPath; }
    public String getProvider() { return provider; }
    public String getEndpoint() { return endpoint; }
    public String getEndpointSource() { return endpointSource; }
    public boolean isRefineEnabled() { return refineEnabled; }
    public boolean isShowHelp() { return showHelp; }
    public boolean isCacheEnabled() { return enableCache; }
    public int getCacheMaxSize() { return cacheMaxSize; }
    public long getCacheTtlMs() { return cacheTtlMs; }
    public int getInputDurationSeconds() { return inputDurationSeconds; }
}
