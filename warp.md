# Warp Project Index

Repo: voxcompose — Java CLI to refine transcripts into Markdown using a local LLM (Ollama)

Summary
- Reads transcript from stdin and produces refined Markdown; optional memory JSONL; can be wrapped as a REST service.

Key directories
- src/main/java/: Java sources
- build.gradle.kts, settings.gradle.kts: build configuration
- tests/: shell test harnesses (fixtures optional)

Quick start
- ./gradlew --no-daemon clean fatJar
- echo "draft notes..." | java -jar build/libs/voxcompose-1.0.0-all.jar --model llama3.1 --timeout-ms 8000

Indexing guidance for Warp
- Prioritize: src/main/java/, build.gradle.kts, README.md
- Skip: go/, demos/, docs/, .gradle/, build/, .idea/, .vscode/

Notes
- Integrates well with macos-ptt-dictation for long-form editing (Shift+F13 flow).

