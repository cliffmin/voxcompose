package com.voxcompose.cli;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class ProfileIO {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final ObjectWriter WRITER = MAPPER.writer(new DefaultPrettyPrinter());

    private ProfileIO() {}

    public static LearningProfile read(Path path) {
        if (Files.exists(path)) {
            try {
                return MAPPER.readValue(path.toFile(), LearningProfile.class);
            } catch (IOException e) {
                // fall-through to fresh profile
            }
        }
        return new LearningProfile();
    }

    public static void atomicWrite(Path path, LearningProfile profile) throws IOException {
        Files.createDirectories(path.getParent());
        Path tmp = path.resolveSibling(path.getFileName().toString() + ".tmp");
        try {
            WRITER.writeValue(tmp.toFile(), profile);
            try {
                Files.move(tmp, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                // ATOMIC_MOVE may not be supported; retry without it
                Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            try { Files.deleteIfExists(tmp); } catch (IOException ignore) {}
        }
    }
}