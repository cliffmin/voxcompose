# VoxCompose Project Structure Guide

**Purpose**: Smart transcript refinement with self-learning corrections  
**Stack**: Java 21+ with Gradle  
**Quick Build**: `./gradlew --no-daemon clean fatJar`  
**Quick Test**: `./tests/run_tests.sh`

## Directory Structure & Decision Guide

### Core Directories

#### `/src/main/java/dev/voxcompose/`
**Purpose**: Production Java code  
**Put here**: Business logic, services, models, utilities  
**Don't put**: Tests, scripts, configs, documentation  
**Packages**:
- `cache/` - Caching mechanisms for performance
- `client/` - External API clients (Ollama, etc.)
- `config/` - Configuration parsing and management
- `dictionary/` - Dictionary plugins and management
- `io/` - Input/output operations
- `learning/` - Self-learning correction system
- `memory/` - Memory management utilities
- `model/` - Data models and capabilities
- `Main.java` - Application entry point

#### `/tests/`
**Purpose**: Bash test scripts and fixtures  
**Put here**: `.sh` test scripts only  
**Don't put**: Test data (use fixtures/), Java unit tests  
**Key files**: `run_tests.sh` (main runner), integration tests  
**Note**: `fixtures/` and `results/` are gitignored

#### `/docs/`
**Purpose**: User-facing technical documentation  
**Put here**: Architecture docs, integration guides, performance docs  
**Don't put**: Internal notes, TODOs, meeting notes  
**Format**: Markdown only, keep diagrams as ASCII art

#### `/packaging/`
**Purpose**: Distribution configurations  
**Put here**: Homebrew formulas, package templates  
**Don't put**: Built artifacts, temporary scripts

#### `/cli/`
**Purpose**: Runnable client entrypoints and shims  
**Put here**: CLI scripts or binaries (temporary Bash shim or long-term Java CLI)  
**Don't put**: Extensive business logic (keep logic in the client, not in Lua/Hammerspoon)

## Decision Framework: Should I Create This?

### Quick Decision Tree
1. **Is it code?** → `/src/main/java/dev/voxcompose/[package]/`
2. **Is it a test?** → `/tests/` (bash) or consider Java unit tests
3. **Is it documentation?** → `/docs/` if technical, README if user-facing
4. **Is it configuration?** → Root level (gradle) or `.github/workflows/`
5. **Is it temporary?** → Don't commit it

### What Gets Ignored
- Build outputs: `/build/`, `/.gradle/`, `*.jar`, `*.class`
- Test data: `/tests/fixtures/`, `/tests/results/`, `metrics.json`
- IDE files: Most `.idea/` contents, `*.iml`
- Temp files: `*.tmp`, `*.bak`, `*.log`, `.DS_Store`
- User data: `~/.config/voxcompose/`, `*.cache`, `*.learned`

## Common Workflows

### Adding a New Feature
1. **Plan**: Identify which package it belongs to (or create new one)
2. **Code**: Add classes to `/src/main/java/dev/voxcompose/[package]/`
3. **Test**: Create test script in `/tests/test_[feature].sh`
4. **Document**: 
   - Update `README.md` if user-facing
   - Add to `/docs/` if technical detail needed
   - Update `CHANGELOG.md` with feature entry
5. **Verify**: Run `./tests/run_tests.sh` before committing

### Editing Existing Code
1. **Understand**: Read existing code and identify dependencies
2. **Modify**: Make changes, keeping consistent style
3. **Test**: Run relevant test scripts
4. **Update**:
   - Fix any broken tests
   - Update docs if behavior changed
   - Add CHANGELOG entry if significant
5. **Verify**: Check nothing broke with full test suite

### Adding a Dictionary Plugin
1. **Create**: New class in `/src/main/java/dev/voxcompose/dictionary/`
2. **Implement**: DictionaryPlugin interface
3. **Register**: Add to plugin registry
4. **Test**: Add test case in `/tests/test_dictionary.sh`
5. **Document**: Update dictionary docs in `/docs/`

### Performance Optimization
1. **Measure**: Run `/tests/generate_metrics.sh` for baseline
2. **Optimize**: Make changes (usually in `cache/` or `memory/`)
3. **Verify**: Re-run metrics, compare results
4. **Document**: Update `/docs/PERFORMANCE.md` with findings
5. **Graph**: Generate visual with `/docs/generate_performance_charts.sh`

## Best Practices Checklist

**Before ANY change:**
- [ ] Is this the right directory/package?
- [ ] Does this follow existing patterns?
- [ ] Will this break existing functionality?

**After coding:**
- [ ] Tests pass? (`./tests/run_tests.sh`)
- [ ] Documentation updated?
- [ ] CHANGELOG.md entry added?
- [ ] No temporary files committed?
- [ ] Commit message follows convention? (feat:, fix:, docs:, etc.)

## Quick Commands

```bash
# Check for uncommitted changes
git status

# Find non-ignored new files
git ls-files --others --exclude-standard

# Run full test suite
./tests/run_tests.sh

# Build release JAR
./gradlew --no-daemon clean fatJar

# Check what would be ignored
git check-ignore -v *
```
