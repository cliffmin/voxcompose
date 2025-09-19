Internal Documentation (Private)

This directory is intended for internal, non-public documentation such as:
- Runbooks and operational procedures
- Incident response and on-call playbooks
- Credentials handling and deployment specifics (never commit secrets)
- Cost/ops notes and service topology details

Guidelines
- Treat contents as confidential. Do not publish or share externally.
- Avoid storing secrets in plain text; reference vault/secret managers instead.
- When possible, prefer a private repository or company wiki for sensitive materials.

Index
- docs/ARCHITECTURE_EVOLUTION.md — VoxCompose integration evolution (shim → CLI → Brew)
- docs/ARCHITECTURE.md — System architecture
- docs/PERFORMANCE.md — Historical performance notes
- docs/SELF_LEARNING.md — Learning system details
- docs/MACOS_PTT_INTEGRATION.md — PTT integration notes
- docs/LONG_TERM_CLI_INTEGRATION.md — Original CLI plan (historical)
- docs/DEVELOPMENT_GUIDE.md — Project structure and contributor guidance

