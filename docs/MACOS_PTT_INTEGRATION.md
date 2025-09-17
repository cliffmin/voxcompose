# macOS PTT Dictation Integration Guide

## Overview
VoxCompose now supports duration-aware refinement, allowing macos-ptt-dictation to optimize when LLM refinement is triggered based on audio duration.

## Key Features

### 1. Duration Threshold Support
VoxCompose can now receive audio duration and skip LLM refinement for short clips:
```bash
# Short clip (10s) - only corrections applied, no LLM
echo "transcript" | java -jar voxcompose.jar --duration 10

# Long clip (30s) - full LLM refinement
echo "transcript" | java -jar voxcompose.jar --duration 30
```

### 2. Capabilities Negotiation
Query VoxCompose for its current settings:
```bash
java -jar voxcompose.jar --capabilities
```

Returns:
```json
{
  "activation": {
    "long_form": {
      "min_duration": 21,  // Learned threshold
      "optimal_duration": 30
    }
  }
}
```

### 3. Self-Learning Corrections
VoxCompose learns from refinements and applies corrections even when LLM is skipped:
- Fixes concatenations: "pushto" → "push to"
- Corrects capitalization: "github" → "GitHub"
- Preserves technical terms: "JSON", "API"

## Integration in ptt_config.lua

Update your Hammerspoon configuration to pass duration:

```lua
-- In macos-ptt-dictation/hammerspoon/ptt_config.lua

-- Query VoxCompose capabilities on startup
function getVoxComposeCapabilities()
    local handle = io.popen("java -jar " .. VOXCOMPOSE_JAR .. " --capabilities 2>/dev/null")
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
    local cmd = {
        "/usr/bin/java", "-jar", VOXCOMPOSE_JAR,
        "--model", "llama3.1",
        "--duration", tostring(math.floor(duration_seconds))  -- Add duration
    }
    
    -- VoxCompose will automatically decide whether to use LLM
    -- based on duration and learned patterns
    local refined = runCommand(cmd, text)
    return refined
end
```

## Performance Optimizations

### Duration-Based Strategy
- **≤21s**: Apply corrections only (instant, no LLM)
- **>21s**: Full LLM refinement (better quality for long content)

### Benefits
1. **Faster response** for short clips (hold-to-talk mode)
2. **Better quality** for long recordings (toggle mode)
3. **Self-improving** through learning patterns
4. **Resource efficient** - no unnecessary LLM calls

## Testing Integration

Test the integration with different durations:

```bash
# Test short clip (corrections only)
echo "i wanna pushto github" | java -jar voxcompose.jar --duration 10
# Output: "I wanna push to GitHub"

# Test long clip (full refinement)
echo "long transcript here..." | java -jar voxcompose.jar --duration 30
# Output: Fully refined markdown

# Check current threshold
java -jar voxcompose.jar --capabilities | jq '.activation.long_form.min_duration'
# Output: 21
```

### Monitoring & Debugging

View VoxCompose logs:
```bash
# Enable debug logging
export VOX_DEBUG=1

# Check learned corrections (new location precedence)
python3 tools/show_learning.py --json | jq '.'
```

## Migration Notes

### From Old Integration
If upgrading from previous version:
1. Update ptt_config.lua to pass `--duration` flag
2. Run capabilities check on startup
3. Remove hardcoded 21s threshold

### Backward Compatibility
VoxCompose remains backward compatible:
- Without `--duration`: Always runs LLM (old behavior)
- With `--duration`: Smart threshold-based decision

## Troubleshooting

### Issue: LLM runs for short clips
**Solution**: Ensure `--duration` is being passed correctly

### Issue: Corrections not applied
**Solution**: Check learned profile exists at `~/.config/voxcompose/learned_profile.json`

### Issue: Threshold not updating
**Solution**: Profile learns over time. Manual adjustment:
```bash
# Edit profile
vim ~/.config/voxcompose/learned_profile.json
# Change "minDurationForRefinement": 21
```

## Future Enhancements
- [ ] Dynamic threshold learning from user feedback
- [ ] Per-context thresholds (meeting vs coding)
- [ ] Whisper model recommendations in capabilities