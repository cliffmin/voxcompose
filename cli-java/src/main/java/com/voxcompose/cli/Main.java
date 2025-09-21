package com.voxcompose.cli;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException {
        // Flags
        String duration = null;
        String dataDirArg = null;
        String stateDirArg = null; // reserved for future
        boolean learn = true;
        boolean dryRun = false;
        boolean stats = false;

        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            switch (a) {
                case "--version":
                    String v = Main.class.getPackage() != null ? Main.class.getPackage().getImplementationVersion() : null;
                    if (v == null || v.isBlank()) v = "dev";
                    System.out.println(v);
                    return;
                case "--duration":
                    if (i + 1 < args.length) duration = args[++i];
                    break;
                case "--data-dir":
                    if (i + 1 < args.length) dataDirArg = args[++i];
                    break;
                case "--state-dir":
                    if (i + 1 < args.length) stateDirArg = args[++i];
                    break;
                case "--learn":
                    if (i + 1 < args.length) {
                        String v2 = args[++i];
                        learn = !"off".equalsIgnoreCase(v2);
                    }
                    break;
                case "--dry-run":
                    dryRun = true; break;
                case "--stats":
                    stats = true; break;
                case "--help":
                case "-h":
                    printHelp();
                    return;
                default:
                    // ignore unknowns for now
            }
        }

        // Read stdin fully
        byte[] input = System.in.readAllBytes();
        String text = new String(input, StandardCharsets.UTF_8);

        // Write pass-through to stdout
        System.out.write(input);

        // Learning side-effect
        if (learn && !dryRun) {
            Path dataDir = (dataDirArg != null && !dataDirArg.isEmpty())
                    ? Path.of(dataDirArg)
                    : DataDirResolver.resolveDataDir();
            Path profilePath = dataDir.resolve("learned_profile.json");
            LearningProfile profile = ProfileIO.read(profilePath);
            boolean changed = MinimalLearner.applyLearning(text, profile);
            if (changed) {
                ProfileIO.atomicWrite(profilePath, profile);
            }
        }

        if (stats) {
            long bytes = input.length;
            String payload = String.format("{\"duration\": %s, \"bytes\": %d, \"learn\": \"%s\", \"dry_run\": %s}\n",
                    duration != null ? duration : "0",
                    bytes,
                    learn ? "on" : "off",
                    dryRun ? "true" : "false");
            System.err.print(payload);
        }
    }

    private static void printHelp() {
        System.out.println("voxcompose [OPTIONS]\n" +
                "  --version\n" +
                "  --duration <seconds>\n" +
                "  --data-dir <path>\n" +
                "  --state-dir <path>\n" +
                "  --learn <on|off> (default on)\n" +
                "  --dry-run\n" +
                "  --stats");
    }
}
