package dev.voxcompose.learning;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.*;

/**
 * Unit tests for UserProfile vocabulary export functionality.
 */
class UserProfileTest {

    private UserProfile profile;
    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        profile = new UserProfile();
        tempDir = Files.createTempDirectory("voxcompose-test");
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
    @DisplayName("Empty profile exports empty string")
    void testExportEmptyProfile() {
        String result = profile.exportVocabularyForWhisper();
        assertEquals("", result);
    }

    @Test
    @DisplayName("Export technical vocabulary only")
    void testExportTechnicalVocabulary() {
        profile.addTechnicalTerm("GitHub");
        profile.addTechnicalTerm("JSON");
        profile.addTechnicalTerm("API");

        String result = profile.exportVocabularyForWhisper();
        assertEquals("GitHub, JSON, API", result);
    }

    @Test
    @DisplayName("Export capitalizations only")
    void testExportCapitalizations() {
        profile.addCorrection("github", "GitHub");
        profile.addCorrection("json", "JSON");

        String result = profile.exportVocabularyForWhisper();
        assertTrue(result.contains("GitHub"));
        assertTrue(result.contains("JSON"));
    }

    @Test
    @DisplayName("Export word corrections only")
    void testExportWordCorrections() {
        profile.addCorrection("pushto", "push to");
        profile.addCorrection("committhis", "commit this");

        String result = profile.exportVocabularyForWhisper();
        assertTrue(result.contains("push to"));
        assertTrue(result.contains("commit this"));
    }

    @Test
    @DisplayName("Export combines all sources")
    void testExportCombinesSources() {
        profile.addTechnicalTerm("VoxCore");
        profile.addCorrection("github", "GitHub");
        profile.addCorrection("pushto", "push to");

        String result = profile.exportVocabularyForWhisper();
        assertTrue(result.contains("VoxCore"));
        assertTrue(result.contains("GitHub"));
        assertTrue(result.contains("push to"));
    }

    @Test
    @DisplayName("Export removes duplicates")
    void testExportRemovesDuplicates() {
        profile.addTechnicalTerm("GitHub");
        profile.addCorrection("github", "GitHub");  // Same term, different case

        String result = profile.exportVocabularyForWhisper();
        // Should contain GitHub only once (from technical vocabulary)
        long count = result.chars().filter(ch -> ch == 'G').count();
        assertEquals(1, count);
    }

    @Test
    @DisplayName("Export limits to 1000 terms")
    void testExportLimitsTerms() {
        // Add 1500 terms
        for (int i = 0; i < 1500; i++) {
            profile.addTechnicalTerm("term" + i);
        }

        String result = profile.exportVocabularyForWhisper();
        String[] terms = result.split(", ");

        // Should be limited to 1000
        assertTrue(terms.length <= 1000,
            "Expected <= 1000 terms, got " + terms.length);
    }

    @Test
    @DisplayName("Export to file creates parent directory")
    void testExportToFileCreatesDirectory() throws IOException {
        profile.addTechnicalTerm("GitHub");

        Path vocabFile = tempDir.resolve("nested/dir/vocabulary.txt");
        assertFalse(Files.exists(vocabFile.getParent()));

        profile.exportVocabularyToFile(vocabFile);

        assertTrue(Files.exists(vocabFile.getParent()));
        assertTrue(Files.exists(vocabFile));
    }

    @Test
    @DisplayName("Export to file writes correct content")
    void testExportToFileWritesContent() throws IOException {
        profile.addTechnicalTerm("GitHub");
        profile.addTechnicalTerm("JSON");
        profile.addCorrection("pushto", "push to");

        Path vocabFile = tempDir.resolve("vocabulary.txt");
        profile.exportVocabularyToFile(vocabFile);

        String content = Files.readString(vocabFile);
        assertTrue(content.contains("GitHub"));
        assertTrue(content.contains("JSON"));
        assertTrue(content.contains("push to"));
    }

    @Test
    @DisplayName("Export to file handles empty profile")
    void testExportToFileEmptyProfile() throws IOException {
        Path vocabFile = tempDir.resolve("vocabulary.txt");
        profile.exportVocabularyToFile(vocabFile);

        assertTrue(Files.exists(vocabFile));
        String content = Files.readString(vocabFile);
        assertEquals("", content);
    }

    @Test
    @DisplayName("Export to file overwrites existing file")
    void testExportToFileOverwrites() throws IOException {
        profile.addTechnicalTerm("First");

        Path vocabFile = tempDir.resolve("vocabulary.txt");
        profile.exportVocabularyToFile(vocabFile);

        String firstContent = Files.readString(vocabFile);
        assertEquals("First", firstContent);

        // Update profile
        profile.addTechnicalTerm("Second");
        profile.exportVocabularyToFile(vocabFile);

        String secondContent = Files.readString(vocabFile);
        assertTrue(secondContent.contains("Second"));
    }

    @Test
    @DisplayName("Export handles special characters in terms")
    void testExportHandlesSpecialCharacters() {
        profile.addTechnicalTerm("C++");
        profile.addTechnicalTerm("Node.js");
        profile.addCorrection("couldve", "could've");

        String result = profile.exportVocabularyForWhisper();
        assertTrue(result.contains("C++"));
        assertTrue(result.contains("Node.js"));
        assertTrue(result.contains("could've"));
    }

    @Test
    @DisplayName("Export maintains insertion order")
    void testExportMaintainsOrder() {
        profile.addTechnicalTerm("First");
        profile.addTechnicalTerm("Second");
        profile.addTechnicalTerm("Third");

        String result = profile.exportVocabularyForWhisper();
        assertEquals("First, Second, Third", result);
    }

    @Test
    @DisplayName("Export format is Whisper-compatible")
    void testExportFormatIsWhisperCompatible() {
        profile.addTechnicalTerm("GitHub");
        profile.addTechnicalTerm("JSON");

        String result = profile.exportVocabularyForWhisper();

        // Should be comma-separated
        assertTrue(result.contains(", "));

        // Should not have trailing comma
        assertFalse(result.endsWith(","));

        // Should not have leading comma
        assertFalse(result.startsWith(","));
    }

    @Test
    @DisplayName("Add technical term ignores null")
    void testAddTechnicalTermIgnoresNull() {
        profile.addTechnicalTerm(null);
        String result = profile.exportVocabularyForWhisper();
        assertEquals("", result);
    }

    @Test
    @DisplayName("Add technical term ignores duplicates")
    void testAddTechnicalTermIgnoresDuplicates() {
        profile.addTechnicalTerm("GitHub");
        profile.addTechnicalTerm("GitHub");

        String result = profile.exportVocabularyForWhisper();
        assertEquals("GitHub", result);
    }

    @Test
    @DisplayName("Export handles large vocabulary sets efficiently")
    void testExportHandlesLargeVocabulary() {
        // Add 500 terms to each source
        for (int i = 0; i < 500; i++) {
            profile.addTechnicalTerm("tech" + i);
            profile.addCorrection("wrong" + i, "right" + i);
        }

        long startTime = System.currentTimeMillis();
        String result = profile.exportVocabularyForWhisper();
        long duration = System.currentTimeMillis() - startTime;

        // Should complete in reasonable time (< 100ms)
        assertTrue(duration < 100, "Export took " + duration + "ms, expected < 100ms");

        // Should have content
        assertFalse(result.isEmpty());
    }
}
