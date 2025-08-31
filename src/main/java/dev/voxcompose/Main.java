package dev.voxcompose;

import okhttp3.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import com.google.gson.*;

public class Main {
  private static String readAll(InputStream in) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    byte[] buf = new byte[8192];
    int r;
    while ((r = in.read(buf)) != -1) bos.write(buf, 0, r);
    return bos.toString(StandardCharsets.UTF_8);
  }

  private static List<String> readMemoryLines(Path p, int max) {
    List<String> out = new ArrayList<>();
    if (p == null) return out;
    try {
      if (!Files.exists(p)) return out;
      try (BufferedReader br = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
        String line;
        while ((line = br.readLine()) != null) {
          line = line.trim();
          if (!line.isEmpty()) out.add(line);
        }
      }
      if (out.size() > max) {
        return out.subList(out.size() - max, out.size());
      }
      return out;
    } catch (Exception e) {
      return Collections.emptyList();
    }
  }

  public static void main(String[] args) throws Exception {
    // Defaults
    String model = "llama3.1";
    int timeoutMs = 10000;
    String format = "markdown";
    Path memoryPath = null;

    // Parse args
    for (int i = 0; i < args.length; i++) {
      switch (args[i]) {
        case "--model":
          if (i + 1 < args.length) model = args[++i];
          break;
        case "--timeout-ms":
          if (i + 1 < args.length) timeoutMs = Integer.parseInt(args[++i]);
          break;
        case "--format":
          if (i + 1 < args.length) format = args[++i];
          break;
        case "--memory":
          if (i + 1 < args.length) memoryPath = Paths.get(args[++i]);
          break;
      }
    }

    String input = readAll(System.in).trim();
    if (input.isEmpty()) { System.out.print(""); return; }

    // Build system prompt (style + memory)
    StringBuilder system = new StringBuilder();
    system.append("You are VoxCompose, a local note refiner. Output ")
          .append(format)
          .append(" with clear structure. Use headings, bullets, short paragraphs. Preserve meaning; fix disfluencies.\n");

    if (memoryPath != null) {
      List<String> mem = readMemoryLines(memoryPath, 20);
      if (!mem.isEmpty()) {
        system.append("Incorporate these user preferences/glossary items when appropriate (do not hallucinate):\n");
        for (String line : mem) {
          try {
            JsonObject obj = JsonParser.parseString(line).getAsJsonObject();
            String text = obj.has("text") ? obj.get("text").getAsString() : null;
            if (text != null && !text.isBlank()) {
              system.append("- ").append(text.trim()).append("\n");
            }
          } catch (Exception ignored) {
            // Ignore malformed lines
          }
        }
      }
    }

    OkHttpClient client = new OkHttpClient.Builder()
      .callTimeout(java.time.Duration.ofMillis(timeoutMs))
      .build();

    JsonObject req = new JsonObject();
    req.addProperty("model", model);
    req.addProperty("prompt", input);
    req.addProperty("system", system.toString());
    req.addProperty("stream", false);

    Request request = new Request.Builder()
      .url("http://127.0.0.1:11434/api/generate")
      .post(RequestBody.create(req.toString(), MediaType.parse("application/json")))
      .build();

    try (Response resp = client.newCall(request).execute()) {
      if (!resp.isSuccessful()) {
        System.err.println("Ollama error: " + resp.code() + " " + resp.message());
        System.out.print(input); // fallback to raw
        return;
      }
      String body = resp.body().string();
      JsonObject json = JsonParser.parseString(body).getAsJsonObject();
      String out = json.has("response") ? json.get("response").getAsString() : input;
      System.out.print(out);
    } catch (Exception e) {
      System.err.println("Ollama call failed: " + e.getMessage());
      System.out.print(input); // fallback to raw
    }
  }
}

