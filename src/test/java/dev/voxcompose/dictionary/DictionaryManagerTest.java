package dev.voxcompose.dictionary;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class DictionaryManagerTest {
    
    @TempDir
    Path tempDir;
    
    private DictionaryManager dictionaryManager;
    
    @BeforeEach
    public void setUp() throws IOException {
        // Create test dictionary files
        createTestWebDevDictionary();
        createTestDevOpsDictionary();
        
        // Initialize manager with test directory
        dictionaryManager = new DictionaryManager(tempDir);
    }
    
    private void createTestWebDevDictionary() throws IOException {
        String content = """
            name: Web Development Test
            version: 1.0.0
            priority: 100
            enabled: true
            
            terms:
              github: GitHub
              gitlab: GitLab
              javascript: JavaScript
              nodejs: Node.js
              json: JSON
              api: API
              
            boundaries:
              - pattern: "pushto"
                replacement: "push to"
              - pattern: "pullrequest"
                replacement: "pull request"
            """;
        
        Path dictFile = tempDir.resolve("webdev.yaml");
        Files.writeString(dictFile, content);
    }
    
    private void createTestDevOpsDictionary() throws IOException {
        String content = """
            name: DevOps Test
            version: 1.0.0
            priority: 90
            enabled: true
            
            terms:
              docker: Docker
              kubernetes: Kubernetes
              aws: AWS
              postgresql: PostgreSQL
              
            boundaries:
              - pattern: "spinup"
                replacement: "spin up"
            """;
        
        Path dictFile = tempDir.resolve("devops.yaml");
        Files.writeString(dictFile, content);
    }
    
    @Test
    public void testLoadDictionaries() {
        List<DictionaryManager.Dictionary> dicts = dictionaryManager.getDictionaries();
        assertNotNull(dicts);
        assertEquals(2, dicts.size(), "Should load 2 dictionaries");
        
        // Check priority ordering (higher priority first)
        assertEquals("Web Development Test", dicts.get(0).getName());
        assertEquals(100, dicts.get(0).getPriority());
        assertEquals("DevOps Test", dicts.get(1).getName());
        assertEquals(90, dicts.get(1).getPriority());
    }
    
    @Test
    public void testTermCorrections() {
        // Test single term corrections
        assertEquals("GitHub", dictionaryManager.refine("github"));
        assertEquals("GitHub", dictionaryManager.refine("GITHUB"));
        assertEquals("GitHub", dictionaryManager.refine("Github"));
        
        // Test multiple terms in a sentence
        String input = "i use github for javascript and nodejs projects";
        String expected = "i use GitHub for JavaScript and Node.js projects";
        assertEquals(expected, dictionaryManager.refine(input));
    }
    
    @Test
    public void testBoundaryCorrections() {
        // Test word boundary fixes
        assertEquals("push to", dictionaryManager.refine("pushto"));
        assertEquals("pull request", dictionaryManager.refine("pullrequest"));
        assertEquals("spin up", dictionaryManager.refine("spinup"));
        
        // Test in context
        String input = "i need to pushto github and create a pullrequest";
        String expected = "i need to push to GitHub and create a pull request";
        assertEquals(expected, dictionaryManager.refine(input));
    }
    
    @Test
    public void testMixedCorrections() {
        String input = "let me pushto github with my nodejs api using docker";
        String expected = "let me push to GitHub with my Node.js API using Docker";
        assertEquals(expected, dictionaryManager.refine(input));
    }
    
    @Test
    public void testDevOpsTerms() {
        String input = "we use docker and kubernetes on aws with postgresql";
        String expected = "we use Docker and Kubernetes on AWS with PostgreSQL";
        assertEquals(expected, dictionaryManager.refine(input));
    }
    
    @Test
    public void testDisabledDictionary() {
        // Disable DevOps dictionary
        dictionaryManager.setDictionaryEnabled("DevOps Test", false);
        
        // Docker should not be corrected, but GitHub should still work
        String input = "docker and github";
        String expected = "docker and GitHub";  // Only GitHub corrected
        assertEquals(expected, dictionaryManager.refine(input));
    }
    
    @Test
    public void testEmptyInput() {
        assertNull(dictionaryManager.refine(null));
        assertEquals("", dictionaryManager.refine(""));
        assertEquals(" ", dictionaryManager.refine(" "));
    }
    
    @Test
    public void testJsonDictionary() throws IOException {
        // Create a JSON dictionary
        String jsonContent = """
            {
                "name": "JSON Test",
                "version": "1.0.0",
                "priority": 150,
                "enabled": true,
                "terms": {
                    "react": "React",
                    "vue": "Vue"
                },
                "boundaries": []
            }
            """;
        
        Path jsonFile = tempDir.resolve("frontend.json");
        Files.writeString(jsonFile, jsonContent);
        
        // Load the new dictionary
        dictionaryManager.loadDictionary(jsonFile);
        
        // Test that React correction works
        assertEquals("React", dictionaryManager.refine("react"));
        assertEquals("Vue", dictionaryManager.refine("vue"));
    }
    
    @Test
    public void testCasePreservation() {
        // Test that corrections preserve intended case
        String input = "The JSON API";
        String expected = "The JSON API";
        assertEquals(expected, dictionaryManager.refine(input));
        
        // Test lowercase preservation where appropriate
        String npmTest = "install with npm";
        assertEquals("install with npm", dictionaryManager.refine(npmTest));
    }
    
    @Test
    public void testWordBoundaries() {
        // Should not replace parts of words
        String input = "githubactions apipeline";
        // Should not change these as they're not word boundaries
        assertEquals("githubactions apipeline", dictionaryManager.refine(input));
        
        // But should replace complete words
        String input2 = "github actions api pipeline";
        String expected2 = "GitHub actions API pipeline";
        assertEquals(expected2, dictionaryManager.refine(input2));
    }
    
    @Test
    public void testComplexSentence() {
        String input = "i'm gonna pushto github and spinup a docker container for my nodejs api that uses json and postgresql";
        String expected = "i'm gonna push to GitHub and spin up a Docker container for my Node.js API that uses JSON and PostgreSQL";
        assertEquals(expected, dictionaryManager.refine(input));
    }
}