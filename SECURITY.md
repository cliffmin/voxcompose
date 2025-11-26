# Security and Privacy

## Privacy-First Design

- **Local-first**: All transcript processing happens locally via Ollama
- **No telemetry**: No network calls or tracking
- **Your data stays local**: Learned profiles stored at `~/.config/voxcompose/`

## LLM Integration

- Default: Local Ollama instance (127.0.0.1:11434)
- Optional: Can configure external endpoints via `--api-url` or `AI_AGENT_URL`
- Review your Ollama configuration before enabling remote endpoints

## File Storage

- Learned corrections: `~/.config/voxcompose/learned_profile.json`
- Response cache: `~/.config/voxcompose/cache/`
- No sensitive data is logged

## Reporting

Please report security issues privately or open a GitHub issue with minimal details and a way to contact you. Avoid sharing sensitive information.
