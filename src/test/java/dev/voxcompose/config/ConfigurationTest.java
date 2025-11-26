package dev.voxcompose.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class ConfigurationTest {

    @Test
    void parseDefaults() {
        Configuration config = Configuration.parse(new String[]{});
        
        assertEquals("llama3.1", config.getModel());
        assertEquals(10000, config.getTimeoutMs());
        assertEquals("markdown", config.getFormat());
        assertEquals("ollama", config.getProvider());
        assertNull(config.getMemoryPath());
        assertNull(config.getOutPath());
        assertTrue(config.isRefineEnabled());
        assertFalse(config.isShowHelp());
        assertFalse(config.isCacheEnabled());
    }

    @Test
    void parseModelFlag() {
        Configuration config = Configuration.parse(new String[]{"--model", "mistral"});
        
        assertEquals("mistral", config.getModel());
        assertEquals("flag", config.getModelSource());
    }

    @Test
    void parseTimeoutFlag() {
        Configuration config = Configuration.parse(new String[]{"--timeout-ms", "5000"});
        
        assertEquals(5000, config.getTimeoutMs());
    }

    @Test
    void parseDurationFlag() {
        Configuration config = Configuration.parse(new String[]{"--duration", "15"});
        
        assertEquals(15, config.getInputDurationSeconds());
    }

    @Test
    void parseCacheFlags() {
        Configuration config = Configuration.parse(new String[]{
            "--cache", "--cache-size", "200", "--cache-ttl-ms", "7200000"
        });
        
        assertTrue(config.isCacheEnabled());
        assertEquals(200, config.getCacheMaxSize());
        assertEquals(7200000, config.getCacheTtlMs());
    }

    @Test
    void parseHelpFlag() {
        Configuration config = Configuration.parse(new String[]{"--help"});
        assertTrue(config.isShowHelp());
        
        Configuration config2 = Configuration.parse(new String[]{"-h"});
        assertTrue(config2.isShowHelp());
    }

    @Test
    void parseOutputFlags() {
        Configuration config = Configuration.parse(new String[]{
            "--out", "/tmp/out.md", "--sidecar", "/tmp/meta.json"
        });
        
        assertEquals("/tmp/out.md", config.getOutPath());
        assertEquals("/tmp/meta.json", config.getSidecarPath());
    }

    @Test
    void parseMemoryFlag() {
        Configuration config = Configuration.parse(new String[]{"--memory", "/path/to/memory.jsonl"});
        
        assertNotNull(config.getMemoryPath());
        assertEquals("memory.jsonl", config.getMemoryPath().getFileName().toString());
    }

    @Test
    void endpointNormalization() {
        // Default endpoint should have /api/generate
        Configuration config = Configuration.parse(new String[]{});
        assertTrue(config.getEndpoint().endsWith("/api/generate"));
    }

    @Test
    void usageTextNotEmpty() {
        String usage = Configuration.getUsageText();
        
        assertNotNull(usage);
        assertTrue(usage.contains("VoxCompose"));
        assertTrue(usage.contains("--model"));
        assertTrue(usage.contains("--help"));
    }
}
