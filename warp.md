# VoxCompose Canonical Directory Structure

This document defines the canonical directory and file structure for VoxCompose. Any files not listed here should be considered non-canonical and potentially removable.

## Purpose

This reference helps identify:
- Files that belong in the repository
- Files that should be gitignored
- Unexpected files that may have been accidentally created
- The intended purpose of each directory

## Quick Reference

**Repository**: voxcompose — Smart transcript refinement with self-learning corrections  
**Language**: Java 17+  
**Build**: `./gradlew --no-daemon clean fatJar`  
**Test**: `./tests/run_tests.sh`

## Canonical Structure

```
voxcompose/
│
├── .git/                       [IGNORED] Git repository data
├── .gradle/                    [IGNORED] Gradle build cache
├── .idea/                      [IGNORED] IntelliJ IDEA settings
├── build/                      [IGNORED] Build output directory
│   └── libs/
│       └── voxcompose-*.jar   [GENERATED] Built JAR files
│
├── docs/                       Documentation (all public-facing)
│   ├── ARCHITECTURE.md         Technical architecture overview
│   ├── MACOS_PTT_INTEGRATION.md macOS push-to-talk integration guide
│   ├── PERFORMANCE.md          Performance improvements documentation
│   ├── SELF_LEARNING.md        Self-learning system documentation
│   └── generate_performance_charts.sh Performance visualization script
│
├── gradle/                     Gradle wrapper files
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
│
├── packaging/                  Distribution packaging
│   └── homebrew/
│       └── voxcompose.rb       Homebrew formula template
│
├── src/                        Source code
│   └── main/
│       └── java/
│           └── dev/
│               └── voxcompose/
│                   ├── cache/
│                   │   └── RefineCache.java
│                   ├── client/
│                   │   └── OllamaClient.java
│                   ├── config/
│                   │   └── Configuration.java
│                   ├── io/
│                   │   └── InputReader.java
│                   ├── learning/
│                   │   ├── Correction.java
│                   │   ├── LearningService.java
│                   │   └── UserProfile.java
│                   ├── memory/
│                   │   └── MemoryManager.java
│                   ├── model/
│                   │   └── Capabilities.java
│                   └── Main.java
│
├── tests/                      Test scripts (bash)
│   ├── fixtures/               [IGNORED] Test data directory
│   │   ├── golden/            [IGNORED] Golden dataset files
│   │   └── *.json/txt         [IGNORED] Test fixtures
│   ├── results/               [IGNORED] Test output directory
│   ├── metrics.json           [IGNORED] Generated metrics file
│   ├── generate_ascii_graphs.sh ASCII chart generator
│   ├── generate_metrics.sh    Performance metrics generator
│   ├── refine_fixtures.sh     Fixture refinement test
│   ├── run_tests.sh           Main test runner
│   ├── select_fixtures.sh     Fixture selection utility
│   ├── test_capabilities.sh   Capabilities endpoint test
│   ├── test_corrections.sh    Corrections validation test
│   ├── test_duration_threshold.sh Duration threshold test
│   ├── test_integration.sh    Integration test suite
│   ├── test_llm_smoke.sh      LLM smoke test
│   └── validate_self_learning.sh Self-learning validation
│
├── .github/                    GitHub configuration
│   └── workflows/
│       └── release.yml         Release automation workflow
│
├── .gitignore                  Git ignore patterns
├── build.gradle                Gradle build configuration
├── CHANGELOG.md                Version history and changes
├── gradlew                     Gradle wrapper script (Unix)
├── gradlew.bat                 Gradle wrapper script (Windows)
├── LICENSE                     MIT License
├── README.md                   Project overview and quick start
├── settings.gradle             Gradle settings
└── WARP.md                     This file - canonical structure
```

## Files That Should NOT Exist

### Temporary Files
- `*.tmp`, `*.bak`, `*.backup`, `*.old`, `*.orig`
- `*~` (editor backup files)
- `.DS_Store` (macOS finder metadata)

### Build Artifacts (outside of build/)
- `*.jar` files in root or other directories
- `*.class` files anywhere

### Test Output (outside of gitignored locations)
- `*.log` files
- `*.out` files
- Test result JSONs outside of tests/

### Personal/Development Files
- `TODO.txt`, `NOTES.txt`, `IDEAS.md`
- Personal configuration files
- IDE-specific files outside of .idea/

### Old/Removed Files (as of v0.3.0)
- `fixtures/sample_*.txt` (moved to tests/fixtures/)
- `docs/IMPLEMENTATION_PLAN.md` (internal doc, removed)
- `docs/MAIN_CAPABILITIES_PATCH.md` (internal doc, removed)
- `tests/benchmark.sh` (consolidated)
- `tests/generate_golden_dataset.sh` (consolidated)
- `tests/test_accuracy_comprehensive.sh` (consolidated)
- `tests/run_all_tests.sh` (replaced with run_tests.sh)
- `tests/test_*_golden.sh` (consolidated)
- `fixes/` directory (temporary patches)

## Gitignored Patterns

The following patterns are gitignored and should never be committed:

```
# Build outputs
/build/
/.gradle/

# IDE files
.idea/**/workspace.xml
.idea/**/tasks.xml
*.iws
*.iml
*.ipr

# Test fixtures and output
/tests/fixtures/
/tests/results/
/tests/metrics.json
/tests/graphs/

# User data
~/.config/voxcompose/
*.cache
*.profile.json
*.learned

# Temporary files
/fixes/
*.tmp
*.bak
*.log

# OS files
.DS_Store
```

## Directory Purposes

| Directory | Purpose | Should Contain |
|-----------|---------|----------------|
| `/docs` | Public documentation | Only .md files and doc scripts |
| `/src` | Source code | Only .java files |
| `/tests` | Test scripts | Only .sh scripts (no data) |
| `/tests/fixtures` | Test data | Gitignored, never committed |
| `/packaging` | Distribution configs | Template files only |
| `/build` | Build output | Gitignored, never committed |

## Validation Commands

To check for non-canonical files:

```bash
# Find files not in git and not gitignored
git ls-files --others --exclude-standard

# Find large files that shouldn't be committed
find . -type f -size +1M -not -path "./.git/*" -not -path "./build/*"

# Find suspicious file types
find . -type f \( -name "*.log" -o -name "*.tmp" -o -name "*.bak" \) 2>/dev/null

# Check for files that don't match canonical structure
# (Lists everything, compare against this document)
find . -type f -not -path "./.git/*" -not -path "./build/*" -not -path "./.gradle/*" | sort
```

## Maintenance

This document should be updated when:
- New files or directories are added to the canonical structure
- Files are permanently removed from the project
- Directory purposes change
- New file types need to be gitignored

Last updated: 2025-09-16 (v0.3.0)

