# VoxCompose AI Agent Rules

## Task Classification

**SIMPLE** (just do it):
- <3 files, obvious solution, no architecture impact
- Bug fixes with clear root cause
- Documentation updates
- Adding tests

**COMPLEX** (plan first):
- >3 files or architectural decisions
- Multiple valid approaches with tradeoffs
- Changes to public APIs or core behavior
- Performance optimizations requiring measurement

**When in doubt:** Ask before executing.

## Git Workflow

### Default Behavior
1. AI stages changes (`git add`) automatically
2. AI shows diff and proposed commit message
3. AI waits for approval before committing
4. AI **never pushes** without explicit "push" or "ship it" command

### Commit Message Format
Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <description>

[optional body]
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation only
- `refactor`: Code restructuring (no behavior change)
- `perf`: Performance improvement
- `test`: Adding/updating tests
- `chore`: Tooling, deps, config

**Examples:**
- `feat(cache): add TTL-based invalidation`
- `fix(ollama): handle connection timeout gracefully`
- `docs: update installation instructions`
- `refactor(main): extract version resolution logic`

**Banned:**
- ❌ "Updated files"
- ❌ "Fixed stuff"
- ❌ "Made changes"
- ❌ Emoji spam (unless user explicitly requests)

## Engineering Principles

### Decision Transparency
Tag non-obvious decisions:
- `[BEST_PRACTICE]` - Industry standard approach
- `[CONSTRAINT]` - Required by existing system/dependency
- `[OPINION]` - Judgment call with alternatives
- `[PROJECT_CONVENTION]` - Following existing codebase patterns

### Code Changes
- Prefer existing patterns over new abstractions
- No backwards-compat hacks (delete unused code completely)
- Validate at boundaries (user input, external APIs), trust internal code
- No speculative features - only what's requested

### Confidence & Risk
- Call out confidence <80% explicitly
- Flag risky changes: public APIs, persistence, concurrency, auth, security
- Prefer minimal/safest approach under time pressure

## VoxCompose-Specific

### Build & Test
- Run `./gradlew test` before committing significant changes
- Integration tests: `./tests/run_tests.sh`
- Performance validation: `./tests/generate_metrics.sh`

### Dependencies
- Java 21 required
- Ollama for LLM operations
- Gradle for builds

### Key Modules
- `Main.java` - CLI entry point, orchestration
- `OllamaClient.java` - LLM integration
- `LearningService.java` - Self-learning corrections
- `Configuration.java` - Config management
