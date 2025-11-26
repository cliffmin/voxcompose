# Contributing

Thanks for your interest in improving VoxCompose.

## How to propose a change
- Open an issue describing the problem or the proposed change
- Fork and create a branch `feature/<short-name>`
- Keep diffs focused; avoid broad refactors

## Development quickstart

```bash
# Build the fat JAR
./gradlew --no-daemon clean fatJar

# Run tests
./tests/run_tests.sh

# Test with sample input
echo "test input" | java -jar build/libs/voxcompose-1.0.0-all.jar --model llama3.1
```

## Style
- Java: Follow existing code patterns in src/main/java
- Use 2-space indent and LF endings
- Keep functions focused and well-documented

## Tests and validation
- Run tests: `./tests/run_tests.sh`
- If you add a user-facing option or behavior change, add a note to README and CHANGELOG

## Commit messages
- Use a short area prefix: `refine:`, `learning:`, `cache:`, `docs:`, `tests:`
- Example: `learning: improve pattern extraction accuracy`

## License
- By contributing, you agree that your contributions are licensed under the MIT License in LICENSE
