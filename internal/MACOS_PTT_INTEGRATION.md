# macOS PTT Dictation Integration Guide

> Conceptual Reference
> The commands and Lua examples in this guide illustrate a potential integration.

## Overview
VoxCompose supports duration-aware refinement, allowing macos-ptt-dictation to optimize when LLM refinement is triggered based on audio duration.

## Key Features

### 1. Duration Threshold Support
```
# Example (concept):
echo "transcript" | voxcompose --duration 10
echo "long transcript" | voxcompose --duration 30
```

### 2. Capabilities Negotiation
```
voxcompose --capabilities
```

## Integration in ptt_config.lua (concept)
```lua
-- In macos-ptt-dictation/hammerspoon/ptt_config.lua

-- Query VoxCompose capabilities on startup
function getVoxComposeCapabilities()
    local handle = io.popen("/usr/local/bin/voxcompose --capabilities 2>/dev/null")
    local result = handle:read("*a")
    handle:close()
    
    local capabilities = hs.json.decode(result)
    if capabilities then
        -- Use learned threshold
        LLM_THRESHOLD_SECONDS = capabilities.activation.long_form.min_duration
        print("Using VoxCompose threshold: " .. LLM_THRESHOLD_SECONDS .. "s")
    else
        -- Fallback to default
        LLM_THRESHOLD_SECONDS = 21
    end
end

-- When calling VoxCompose, pass the duration
function refineTranscript(text, duration_seconds)
    local cmd = { "/usr/local/bin/voxcompose", "--duration", tostring(math.floor(duration_seconds)) }
    local refined = runCommand(cmd, text)
    return refined
end
```

## Testing Integration

```
echo "i wanna pushto github" | voxcompose --duration 10
echo "long transcript here..." | voxcompose --duration 30
```
