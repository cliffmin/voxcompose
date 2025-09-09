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
    String modelSource = "default";
    int timeoutMs = 10000;
    String format = "markdown";
    Path memoryPath = null;
    String outPath = null;
    String sidecarPath = null;
    String provider = "ollama";
    String apiUrlArg = null;
    String endpoint = null;
    String endpointSource = "default";
    boolean showHelp = false;

    // Parse args
    for (int i = 0; i < args.length; i++) {
      switch (args[i]) {
        case "--model":
          if (i + 1 < args.length) { model = args[++i]; modelSource = "flag"; }
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
        case "--out":
          if (i + 1 < args.length) outPath = args[++i];
          break;
        case "--sidecar":
          if (i + 1 < args.length) sidecarPath = args[++i];
          break;
        case "--provider":
          if (i + 1 < args.length) provider = args[++i];
          break;
        case "--api-url":
          if (i + 1 < args.length) apiUrlArg = args[++i];
          break;
        case "--help":
        case "-h":
          showHelp = true;
          break;
      }
    }

    if (showHelp) {
      String usage = String.join("\n",
        "VoxCompose - local LLM Markdown refiner (Ollama)",
        "",
        "Usage:",
        "  voxcompose [flags] < input.txt > output.md",
        "",
        "Flags:",
        "  --model <name>         Model name (default: llama3.1)",
        "  --timeout-ms <ms>      HTTP call timeout (default: 10000)",
        "  --memory <jsonl-path>  Optional JSONL memory file",
        "  --format <fmt>         Output format (default: markdown)",
        "  --out <file>           Also write output to file",
        "  --sidecar <file>       Write JSON sidecar with metadata",
        "  --provider <name>      Provider name (default: ollama)",
        "  --api-url <url>        Override endpoint (base or full /api/generate)",
        "  --help, -h             Show this help and exit",
        "",
        "Environment (overridden by flags):",
        "  AI_AGENT_MODEL         Default model name",
        "  AI_AGENT_URL           Base URL (or full /api/generate)",
        "  OLLAMA_HOST            Ollama base URL",
        "  VOX_REFINE             Set 0/false to disable refinement"
      );
      System.err.println(usage);
      System.exit(2);
    }

    String input = readAll(System.in).trim();
    if (input.isEmpty()) { System.out.print(""); return; }

    // VOX_REFINE env toggle (default enabled)
    String refineEnv = System.getenv("VOX_REFINE");
    boolean refineEnabled = true;
    if (refineEnv != null) {
      String v = refineEnv.trim().toLowerCase(Locale.ROOT);
      if (v.equals("0") || v.equals("false") || v.equals("no") || v.equals("off")) {
        refineEnabled = false;
      }
    }

    if (!refineEnabled) {
      System.err.println("INFO: LLM refinement disabled via VOX_REFINE=" + (refineEnv == null ? "" : refineEnv));
      System.out.print(input);
      return;
    }

    // Build system prompt (style + memory)
    StringBuilder system = new StringBuilder();
    system.append("You are VoxCompose, a local note refiner. Output ")
          .append(format)
          .append(" with clear structure. Use headings, bullets, short paragraphs. Preserve meaning; fix disfluencies.\n");

    int memoryUsedCount = 0;
    if (memoryPath != null) {
      List<String> mem = readMemoryLines(memoryPath, 20);
      memoryUsedCount = mem.size();
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

    // Resolve model from env if not provided via flag
    if (!"flag".equals(modelSource)) {
      String envModel = System.getenv("AI_AGENT_MODEL");
      if (envModel != null && !envModel.isBlank()) {
        model = envModel.trim();
        modelSource = "AI_AGENT_MODEL";
      }
    }

    // Resolve endpoint with precedence: --api-url > AI_AGENT_URL > OLLAMA_HOST > default
    String base = null;
    if (apiUrlArg != null && !apiUrlArg.isBlank()) {
      base = apiUrlArg.trim();
      endpointSource = "flag";
    } else {
      String envApi = System.getenv("AI_AGENT_URL");
      String envOllama = System.getenv("OLLAMA_HOST");
      if (envApi != null && !envApi.isBlank()) {
        base = envApi.trim();
        endpointSource = "AI_AGENT_URL";
      } else if (envOllama != null && !envOllama.isBlank()) {
        base = envOllama.trim();
        endpointSource = "OLLAMA_HOST";
      } else {
        base = "http://127.0.0.1:11434";
        endpointSource = "default";
      }
    }
    String normalizedBase = base.replaceAll("/+$", "");
    if (normalizedBase.endsWith("/api/generate")) {
      endpoint = normalizedBase;
    } else {
      endpoint = normalizedBase + "/api/generate";
    }

    // Logging: configuration sources
    System.err.println("INFO: Using LLM model: " + model + " (source=" + modelSource + ")");
    System.err.println("INFO: Using LLM endpoint: " + endpoint + " (source=" + endpointSource + ")");

    // Distinctive log line to assert in tests (kept for backward compatibility)
    if (memoryPath != null) {
      System.err.println("INFO: Running LLM refinement with model: " + model + " (memory=" + memoryPath.toString() + ")");
    } else {
      System.err.println("INFO: Running LLM refinement with model: " + model);
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
      .url(endpoint)
      .post(RequestBody.create(req.toString(), MediaType.parse("application/json")))
      .build();

    long tStart = System.currentTimeMillis();
    boolean ok = true;
    String finalOut = input;

    try (Response resp = client.newCall(request).execute()) {
      if (!resp.isSuccessful()) {
        System.err.println("Ollama error: " + resp.code() + " " + resp.message());
        ok = false;
      } else {
        String body = resp.body().string();
        JsonObject json = JsonParser.parseString(body).getAsJsonObject();
        finalOut = json.has("response") ? json.get("response").getAsString() : input;
      }
    } catch (Exception e) {
      System.err.println("Ollama call failed: " + e.getMessage());
      ok = false;
    }

    long refineMs = System.currentTimeMillis() - tStart;

    // Always print something to stdout for backward compatibility
    System.out.print(ok ? finalOut : input);

    // Optional file outputs
    if (outPath != null) {
      try { Files.write(Paths.get(outPath), (ok ? finalOut : input).getBytes(StandardCharsets.UTF_8)); } catch (Exception ignored) {}
    }
    if (sidecarPath != null) {
      try {
        JsonObject sc = new JsonObject();
        sc.addProperty("ok", ok);
        sc.addProperty("provider", provider);
        sc.addProperty("model", model);
        sc.addProperty("model_source", modelSource);
        sc.addProperty("endpoint", endpoint);
        sc.addProperty("endpoint_source", endpointSource);
        sc.addProperty("refine_ms", refineMs);
        sc.addProperty("memory_items_used", memoryUsedCount);
        Files.write(Paths.get(sidecarPath), sc.toString().getBytes(StandardCharsets.UTF_8));
      } catch (Exception ignored) {}
    }

    if (!ok) System.exit(1);
  }
}

