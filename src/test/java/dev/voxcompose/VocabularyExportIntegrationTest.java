package dev.voxcompose;

import dev.voxcompose.learning.UserProfile;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.*;

/**
 * Integration tests for vocabulary export CLI command.
 * Uses fresh UserProfile instances to avoid singleton state pollution.
 */
class VocabularyExportIntegrationTest {

    private Path tempDir;
    private Path testVocabPath;
    private UserProfile profile;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("voxcompose-integration-test");
        testVocabPath = tempDir.resolve("vocabulary.txt");
        profile = new UserProfile();  // Fresh instance for each test
    }

    @AfterEach
    void tearDown() throws IOException {
        // Clean up temp directory
        if (Files.exists(tempDir)) {
            Files.walk(tempDir)
                .sorted((a, b) -> -a.compareTo(b))
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        // Ignore cleanup errors
                    }
                });
        }
    }

    @Test
    @DisplayName("CLI export creates vocabulary file")
    void testCLIExportCreatesFile() {
        profile.addTechnicalTerm("GitHub");
        profile.addTechnicalTerm("JSON");

        try {
            profile.exportVocabularyToFile(testVocabPath);

            assertTrue(Files.exists(testVocabPath));
            String content = Files.readString(testVocabPath);
            assertTrue(content.contains("GitHub"));
            assertTrue(content.contains("JSON"));
        } catch (IOException e) {
            fail("Export should not throw exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("CLI export handles empty profile gracefully")
    void testCLIExportEmptyProfile() {
        try {
            profile.exportVocabularyToFile(testVocabPath);

            assertTrue(Files.exists(testVocabPath));
            String content = Files.readString(testVocabPath);
            assertEquals("", content);
        } catch (IOException e) {
            fail("Export should not throw exception for empty profile: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("CLI export creates parent directory if missing")
    void testCLIExportCreatesParentDirectory() {
        Path nestedPath = tempDir.resolve("nested/dir/vocabulary.txt");
        assertFalse(Files.exists(nestedPath.getParent()));

        profile.addTechnicalTerm("Test");

        try {
            profile.exportVocabularyToFile(nestedPath);

            assertTrue(Files.exists(nestedPath.getParent()));
            assertTrue(Files.exists(nestedPath));
        } catch (IOException e) {
            fail("Export should create parent directories: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("CLI export produces valid Whisper format")
    void testCLIExportProducesValidFormat() {
        profile.addTechnicalTerm("GitHub");
        profile.addTechnicalTerm("JSON");
        profile.addTechnicalTerm("API");
        profile.addCorrection("pushto", "push to");

        try {
            profile.exportVocabularyToFile(testVocabPath);

            String content = Files.readString(testVocabPath);

            // Validate Whisper-compatible format
            assertFalse(content.trim().isEmpty());
            assertTrue(content.contains(", "), "Should be comma-separated");
            assertFalse(content.endsWith(","), "Should not end with comma");
            assertFalse(content.startsWith(","), "Should not start with comma");

            // Validate content
            assertTrue(content.contains("GitHub"));
            assertTrue(content.contains("JSON"));
            assertTrue(content.contains("API"));
            assertTrue(content.contains("push to"));
        } catch (IOException e) {
            fail("Export failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("CLI export is idempotent")
    void testCLIExportIsIdempotent() {
        profile.addTechnicalTerm("GitHub");

        try {
            // Export twice
            profile.exportVocabularyToFile(testVocabPath);
            String firstContent = Files.readString(testVocabPath);

            profile.exportVocabularyToFile(testVocabPath);
            String secondContent = Files.readString(testVocabPath);

            assertEquals(firstContent, secondContent,
                "Multiple exports should produce identical output");
        } catch (IOException e) {
            fail("Export failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("CLI export handles profile updates")
    void testCLIExportHandlesProfileUpdates() {
        profile.addTechnicalTerm("GitHub");

        try {
            profile.exportVocabularyToFile(testVocabPath);
            String firstContent = Files.readString(testVocabPath);
            assertEquals("GitHub", firstContent);

            // Update profile
            profile.addTechnicalTerm("JSON");
            profile.exportVocabularyToFile(testVocabPath);

            String secondContent = Files.readString(testVocabPath);
            assertTrue(secondContent.contains("GitHub"));
            assertTrue(secondContent.contains("JSON"));
        } catch (IOException e) {
            fail("Export failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("CLI export respects 1000 term limit")
    void testCLIExportRespectsTermLimit() {
        // Add 1500 terms
        for (int i = 0; i < 1500; i++) {
            profile.addTechnicalTerm("term" + i);
        }

        try {
            profile.exportVocabularyToFile(testVocabPath);
            String content = Files.readString(testVocabPath);

            String[] terms = content.split(", ");
            assertTrue(terms.length <= 1000,
                "Should limit to 1000 terms, got " + terms.length);
        } catch (IOException e) {
            fail("Export failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("CLI export handles UTF-8 characters")
    void testCLIExportHandlesUTF8() {
        profile.addTechnicalTerm("café");
        profile.addTechnicalTerm("naïve");
        profile.addTechnicalTerm("résumé");

        try {
            profile.exportVocabularyToFile(testVocabPath);
            String content = Files.readString(testVocabPath);

            assertTrue(content.contains("café"));
            assertTrue(content.contains("naïve"));
            assertTrue(content.contains("résumé"));
        } catch (IOException e) {
            fail("Export should handle UTF-8: " + e.getMessage());
        }
    }
}
