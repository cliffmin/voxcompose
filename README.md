# VoxCompose

A small, standalone Java CLI that refines transcripts into Markdown using a local Ollama model.

Features
- Reads transcript from stdin; outputs refined Markdown to stdout (or file in a later version).
- Optional JSONL memory file to include preferences/corrections/glossary.
- Ollama local model backend (http://127.0.0.1:11434/), no cloud keys required.
- Ships as a shaded JAR via Gradle shadow plugin.

Quick start
1) Install Ollama and pull a model (example: llama3.1)
   brew install ollama
   ollama serve >/dev/null 2>&1 &
   ollama pull llama3.1

2) Build (uses Gradle wrapper)
   cd ~/code/voxcompose
   ./gradlew --no-daemon clean fatJar

3) Run
   echo "draft notes about a meeting..." | \
     java -jar build/libs/voxcompose-0.1.0-all.jar \
       --model llama3.1 \
       --timeout-ms 8000 \
       --memory "$HOME/Library/Application Support/voxcompose/memory.jsonl" \
       --sidecar /tmp/refine.json --out /tmp/out.md && open /tmp/out.md

CLI flags
- --model <name>         # Model name (default: llama3.1)
- --timeout-ms <ms>      # HTTP call timeout (default: 10000)
- --memory <jsonl-path>  # Optional JSONL memory; recent lines influence style/terminology
- --format markdown      # Reserved; markdown is the default and only format today
- --out <file>           # Optional: also write the output to a file
- --sidecar <file>       # Optional: write a JSON sidecar with {ok, provider, model, endpoint, refine_ms, memory_items_used}
- --provider <name>      # Optional: provider name (default: ollama). For future expansion.
- --api-url <url>        # Optional: override endpoint (e.g., http://127.0.0.1:11434 or full /api/generate)
- --help, -h             # Show usage and exit (exit code 2)

Environment variables and precedence
- Model: --model > AI_AGENT_MODEL > default: llama3.1
- Endpoint: --api-url > AI_AGENT_URL > OLLAMA_HOST > default base http://127.0.0.1:11434
  - If the chosen value does not end with /api/generate, it is appended automatically.
- Toggle: VOX_REFINE=0 disables refinement and echoes input.

Logging and test toggle
- On refinement start, the CLI writes to stderr:
  INFO: Using LLM model: <name> (source=<flag|AI_AGENT_MODEL|default>)
  INFO: Using LLM endpoint: <url> (source=<flag|AI_AGENT_URL|OLLAMA_HOST|default>)
  INFO: Running LLM refinement with model: <name> (memory=<path>)
- To disable refinement for tests or debugging, set VOX_REFINE=0. The CLI logs:
  INFO: LLM refinement disabled via VOX_REFINE=0
  and echoes the raw input to stdout.

Memory file format (JSONL)
- One JSON object per line, e.g.:
  {"ts":"2025-08-31T02:00:00Z","kind":"preference","tags":["tone"],"text":"Prefer concise bullet points."}
  {"ts":"2025-08-31T02:05:00Z","kind":"glossary","tags":["product"],"text":"Apollo: internal tool for signal routing."}
- Only the most recent ~20 items are injected into the prompt.

Notes
- If Ollama is not running or the model is missing, VoxCompose will print the raw input as a fallback.
- Future: add OpenAI provider as an option; add --out <file> and structured prompt profiles.

Purpose and relationship to macos-ptt-dictation
- VoxCompose is a standalone refiner that turns raw transcripts into readable Markdown using a local LLM (Ollama).
- macos-ptt-dictation optionally shells out to VoxCompose for long-form sessions (Shift+F13), after Whisper transcription.
- Separation lets you keep macOS automation (Lua/Hammerspoon) independent from LLM provider logic (Java/Ollama), making each piece easier to test, reuse, and showcase.

Why separate repos
- Clean boundaries: platform automation vs. language-model refinement.
- Reuse: VoxCompose can be used by other tools beyond macos-ptt-dictation.
- Portfolio: a focused Java repo with its own release, tests, and roadmap.

Using with macos-ptt-dictation
1) Build this project (shaded jar):
   cd ~/code/voxcompose
   ./gradlew --no-daemon clean fatJar
2) In macos-ptt-dictation/hammerspoon/ptt_config.lua enable and point the refiner:
   LLM_REFINER = {
     ENABLED = true,
     CMD = { "/usr/bin/java", "-jar", (os.getenv("HOME") or "") .. "/code/voxcompose/build/libs/voxcompose-0.1.0-all.jar" },
     ARGS = { "--model", "llama3.1", "--timeout-ms", "8000", "--memory", (os.getenv("HOME") or "") .. "/Library/Application Support/voxcompose/memory.jsonl" },
     TIMEOUT_MS = 9000,
   }
3) Reload Hammerspoon, then use Shift+F13 to record → refine → open Markdown.

Build and requirements
- Java 17+: brew install openjdk@17 (or use your preferred JDK)
- Ollama running with a pulled model (e.g., llama3.1):
  ollama serve &
  ollama pull llama3.1
- Build with wrapper (recommended):
  ./gradlew --no-daemon clean fatJar
  ls build/libs/voxcompose-0.1.0-all.jar

Memory JSONL (optional)
- Location: ~/Library/Application Support/voxcompose/memory.jsonl
- One JSON object per line, recent ~20 items are injected into the system prompt.
  {"ts":"2025-08-31T02:50:00Z","kind":"preference","tags":["tone"],"text":"Prefer concise, actionable bullet points."}
  {"ts":"2025-08-31T02:51:00Z","kind":"glossary","tags":["project"],"text":"Apollo: internal tool for signal routing."}

Integration test reference
- macos-ptt-dictation provides an integration script: tests/integration/longform_to_markdown.sh
  - Uses your local WAV (not checked into git), runs Whisper → VoxCompose, asserts Markdown structure.

Fixture tests (this repo)
- Run tests/refine_fixtures.sh to refine text fixtures and assert the refinement log appears.
  - Requires the fat jar:
    ./gradlew --no-daemon clean fatJar
  - Place one or more .txt files under tests/fixtures/ (not committed) or run tests/select_fixtures.sh to collect a small set from ~/Documents/VoiceNotes.
  - The script asserts:
    - Non-empty output to stdout
    - A log line on stderr: "INFO: Running LLM refinement with model: …"
    - A disabled-path smoke check with VOX_REFINE=0

Install via Homebrew
- Once a release is published, you can install via a Homebrew Tap:
  - brew tap cliffmin/tap
  - brew install voxcompose
- On Apple Silicon, the installed binary is at /opt/homebrew/bin/voxcompose; on Intel Macs it is /usr/local/bin/voxcompose.
- The formula expects a released jar named voxcompose-<version>-all.jar.
- After each release, update the formula's sha256 with the jar's checksum shown in the GitHub Release body.

Using the wrapper
- voxcompose --model llama3.1 --timeout-ms 8000 --memory "$HOME/Library/Application Support/voxcompose/memory.jsonl"
- Or rely on env: AI_AGENT_MODEL, AI_AGENT_URL, OLLAMA_HOST (flags override env).

## Changelog

See [CHANGELOG.md](./CHANGELOG.md)
