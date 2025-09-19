package com.voxcompose.cli;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public final class DataDirResolver {
    private DataDirResolver() {}

    public static Path resolveDataDir() {
        return resolveDataDir(System.getenv(), System.getProperty("os.name"), System.getProperty("user.home"));
    }

    // Visible for tests
    static Path resolveDataDir(Map<String, String> env, String osName, String userHome) {
        String override = env.get("VOXCOMPOSE_DATA_DIR");
        if (override != null && !override.isEmpty()) {
            return Paths.get(override);
        }
        String xdg = env.get("XDG_DATA_HOME");
        if (xdg != null && !xdg.isEmpty()) {
            return Paths.get(xdg).resolve("voxcompose");
        }
        boolean isMac = osName != null && osName.toLowerCase().contains("mac");
        if (isMac) {
            return Paths.get(userHome, "Library", "Application Support", "VoxCompose");
        }
        return Paths.get(userHome, ".local", "share", "voxcompose");
    }
}
