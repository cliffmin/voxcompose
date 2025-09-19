package com.voxcompose.cli;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DataDirResolverTest {
    @Test
    void resolvesOverride() {
        Map<String, String> env = new HashMap<>();
        env.put("VOXCOMPOSE_DATA_DIR", "/tmp/voxdata");
        Path p = DataDirResolver.resolveDataDir(env, "Mac OS X", "/Users/test");
        assertEquals(Path.of("/tmp/voxdata"), p);
    }

    @Test
    void resolvesXdg() {
        Map<String, String> env = new HashMap<>();
        env.put("XDG_DATA_HOME", "/xdg");
        Path p = DataDirResolver.resolveDataDir(env, "Mac OS X", "/Users/test");
        assertEquals(Path.of("/xdg/voxcompose"), p);
    }

    @Test
    void resolvesMacDefault() {
        Map<String, String> env = new HashMap<>();
        Path p = DataDirResolver.resolveDataDir(env, "Mac OS X", "/Users/test");
        assertEquals(Path.of("/Users/test/Library/Application Support/VoxCompose"), p);
    }

    @Test
    void resolvesLinuxDefault() {
        Map<String, String> env = new HashMap<>();
        Path p = DataDirResolver.resolveDataDir(env, "Linux", "/home/test");
        assertEquals(Path.of("/home/test/.local/share/voxcompose"), p);
    }
}
