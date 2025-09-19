package com.voxcompose.cli;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MinimalLearnerTest {
    @Test
    void appliesCapsAndSplits() {
        String sample = "i need to pushto github and update the json api";
        LearningProfile p = new LearningProfile();
        boolean changed = MinimalLearner.applyLearning(sample, p);
        assertTrue(changed, "Expected changes to profile");

        Map<String, String> wc = p.getWordCorrections();
        Map<String, String> cap = p.getCapitalizations();
        List<String> vocab = p.getTechnicalVocabulary();

        assertEquals("push to", wc.get("pushto"));
        assertEquals("JSON", cap.get("json"));
        assertEquals("API", cap.get("api"));
        assertEquals("GitHub", cap.get("github"));

        assertTrue(vocab.contains("JSON"));
        assertTrue(vocab.contains("API"));
        assertTrue(vocab.contains("GitHub"));
    }
}
