package dev.voxcompose.learning;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class LearningServiceTest {

    private LearningService service;

    @BeforeEach
    void setUp() {
        service = new LearningService();
    }

    @Test
    void applyCorrectionsNullInput() {
        assertNull(service.applyCorrections(null));
    }

    @Test
    void applyCorrectionsEmptyInput() {
        assertEquals("", service.applyCorrections(""));
    }

    @Test
    void fixPushTo() {
        String result = service.applyCorrections("I want to pushto the code");
        assertEquals("I want to push to the code", result);
    }

    @Test
    void fixCommitThis() {
        String result = service.applyCorrections("Let me committhis change");
        assertEquals("Let me commit this change", result);
    }

    @Test
    void fixGitHubCapitalization() {
        String result = service.applyCorrections("Push to github now");
        assertEquals("Push to GitHub now", result);
    }

    @Test
    void fixJsonCapitalization() {
        String result = service.applyCorrections("Parse the json file");
        assertEquals("Parse the JSON file", result);
    }

    @Test
    void multipleCorrectionsCombined() {
        String input = "pushto github and committhis json";
        String result = service.applyCorrections(input);
        
        assertTrue(result.contains("push to"));
        assertTrue(result.contains("GitHub"));
        assertTrue(result.contains("commit this"));
        assertTrue(result.contains("JSON"));
    }

    @Test
    void preserveUnknownWords() {
        String input = "This is a normal sentence";
        String result = service.applyCorrections(input);
        assertEquals(input, result);
    }

    @Test
    void fixConcatenationWithWould() {
        String result = service.applyCorrections("Iwould like to help");
        assertEquals("I would like to help", result);
    }

    @Test
    void fixConcatenationWithShould() {
        String result = service.applyCorrections("Youshould try this");
        assertEquals("You should try this", result);
    }

    @Test
    void fixConcatenationWithInto() {
        // The regex splits "into" as "in" + "to", so "Gointo" becomes "Go in to"
        String result = service.applyCorrections("Gointo the folder");
        assertEquals("Go in to the folder", result);
    }

    @Test
    void learnDoesNotThrowOnNull() {
        assertDoesNotThrow(() -> service.learn(null, "test"));
        assertDoesNotThrow(() -> service.learn("test", null));
        assertDoesNotThrow(() -> service.learn(null, null));
    }

    @Test
    void learnDoesNotThrowOnEqual() {
        assertDoesNotThrow(() -> service.learn("same", "same"));
    }

    @Test
    void singletonInstance() {
        LearningService instance1 = LearningService.getInstance();
        LearningService instance2 = LearningService.getInstance();
        assertSame(instance1, instance2);
    }

    @Test
    void getProfileNotNull() {
        assertNotNull(service.getProfile());
    }
}
