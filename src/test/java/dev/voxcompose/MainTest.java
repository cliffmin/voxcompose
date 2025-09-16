package dev.voxcompose;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class MainTest {

  @Test
  public void testApplicationStarts() {
    // Basic smoke test to ensure the application can be instantiated
    assertNotNull(new Main());
  }

  @Test
  public void testSystemProperties() {
    // Test that we can access system properties
    String javaVersion = System.getProperty("java.version");
    assertNotNull(javaVersion);
    assertFalse(javaVersion.isEmpty());
  }
}
