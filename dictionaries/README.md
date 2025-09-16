# VoxCompose Dictionary System

## Overview

Dictionaries are YAML/JSON files that define term corrections, capitalizations, and expansions. They can be layered and prioritized.

## Structure

```yaml
# ~/.voxcompose/dictionaries/webdev.yaml
name: Web Development
version: 1.0.0
priority: 100  # Higher priority overrides lower
enabled: true

# Term corrections (case-insensitive input -> correct output)
terms:
  github: GitHub
  gitlab: GitLab
  javascript: JavaScript
  typescript: TypeScript
  nodejs: Node.js
  npm: npm  # Explicitly lowercase
  css: CSS
  html: HTML
  json: JSON
  yaml: YAML
  api: API
  oauth: OAuth
  jwt: JWT
  postgresql: PostgreSQL
  mongodb: MongoDB
  redis: Redis
  nginx: NGINX
  docker: Docker
  kubernetes: Kubernetes

# Common word boundary fixes
boundaries:
  - pattern: "pushto"
    replacement: "push to"
  - pattern: "committhis"
    replacement: "commit this"
  - pattern: "pullrequest"
    replacement: "pull request"
  - pattern: "letme"
    replacement: "let me"

# Contextual replacements (regex patterns)
contexts:
  - pattern: "\\bgit (push|pull|commit|checkout|merge|rebase)\\b"
    bias: technical  # Hint to prefer technical interpretation
  - pattern: "\\b(GET|POST|PUT|DELETE|PATCH)\\b"
    format: uppercase  # Force uppercase for HTTP methods

# Expansion shortcuts
expansions:
  pr: pull request
  repo: repository
  env: environment
  config: configuration
  db: database
  auth: authentication
  impl: implementation
```

## Installation Methods

### 1. Built-in Dictionaries
```bash
# Install from registry
voxcompose dict install webdev
voxcompose dict install devops
voxcompose dict install python
```

### 2. From URL
```bash
voxcompose dict add https://raw.githubusercontent.com/user/repo/dict.yaml
```

### 3. Local File
```bash
voxcompose dict add ./my-company-terms.yaml
```

### 4. Community Dictionaries
```bash
# Browse available
voxcompose dict search

# Install popular pack
voxcompose dict install-pack developer-essentials
```

## Priority System

Dictionaries are applied in priority order (highest first):
1. User overrides (~/.voxcompose/dictionaries/user.yaml) - Priority 1000
2. Project-specific (./.voxcompose.yaml) - Priority 500  
3. Installed dictionaries - Priority 100-400
4. Built-in defaults - Priority 0

## Creating Custom Dictionaries

```bash
# Generate template
voxcompose dict create my-terms

# Edit the generated file
$EDITOR ~/.voxcompose/dictionaries/my-terms.yaml

# Test it
echo "test github api" | voxcompose --dict my-terms

# Share it
voxcompose dict publish my-terms
```

## API for Developers

```java
// In VoxCompose code
public interface Dictionary {
    String getName();
    int getPriority();
    Map<String, String> getTerms();
    List<BoundaryRule> getBoundaries();
    List<ContextRule> getContexts();
}

public class DictionaryManager {
    public void loadDictionary(Path file);
    public void downloadDictionary(String name);
    public String applyDictionaries(String input);
    public void setEnabled(String name, boolean enabled);
}
```