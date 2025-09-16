#!/usr/bin/env bash
# Comprehensive accuracy test for VoxCompose
# Tests: 1) Transcription accuracy (WER), 2) Refinement quality, 3) Performance metrics
# Uses golden dataset with 21+ second audio clips

set -euo pipefail

GOLDEN_DIR="tests/fixtures/golden"
RESULTS_DIR="tests/results/$(date +%Y%m%d_%H%M%S)_comprehensive"
WHISPER_CPP="/opt/homebrew/bin/whisper-cli"
VOXCOMPOSE_JAR="build/libs/voxcompose-0.1.0-all.jar"

# Model configuration (matches macos-ptt-dictation)
MODEL_THRESHOLD=21.0  # seconds
MODEL_SHORT="base"
MODEL_LONG="medium"

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo "=== VoxCompose Comprehensive Accuracy Test ==="
echo "Testing transcription accuracy, refinement quality, and performance"
echo ""

# Check prerequisites
if [[ ! -x "$WHISPER_CPP" ]]; then
    # Try alternative name
    WHISPER_CPP="/opt/homebrew/bin/whisper-cpp"
    if [[ ! -x "$WHISPER_CPP" ]]; then
        echo -e "${RED}Error: whisper-cpp not found${NC}"
        echo "Install with: brew install whisper-cpp"
        exit 1
    fi
fi

if [[ ! -f "$VOXCOMPOSE_JAR" ]]; then
    echo -e "${YELLOW}Building VoxCompose...${NC}"
    ./gradlew --no-daemon clean fatJar || exit 1
fi

# Create results directory
mkdir -p "$RESULTS_DIR"

# Initialize statistics
declare -A category_stats
declare -A model_stats
total_samples=0
total_wer=0
total_refinement_score=0

# Function to calculate Word Error Rate
calculate_wer() {
    local ref="$1"
    local hyp="$2"
    
    # Normalize for comparison
    local ref_norm=$(echo "$ref" | tr '[:upper:]' '[:lower:]' | tr -s ' ' | sed 's/[[:punct:]]//g')
    local hyp_norm=$(echo "$hyp" | tr '[:upper:]' '[:lower:]' | tr -s ' ' | sed 's/[[:punct:]]//g')
    
    # Simple WER calculation
    local ref_words=$(echo "$ref_norm" | wc -w)
    local hyp_words=$(echo "$hyp_norm" | wc -w)
    
    if [[ $ref_words -eq 0 ]]; then
        echo "0"
        return
    fi
    
    # Count matching words (simplified)
    local matches=0
    for word in $ref_norm; do
        if echo "$hyp_norm" | grep -qw "$word"; then
            ((matches++))
        fi
    done
    
    local wer=$(echo "scale=2; 100 * (1 - $matches / $ref_words)" | bc)
    echo "$wer"
}

# Function to evaluate refinement quality
evaluate_refinement() {
    local original="$1"
    local refined="$2"
    local expected="$3"
    
    local score=100
    
    # Check for disfluency removal
    if echo "$refined" | grep -q "um,\|uh,\|you know"; then
        ((score-=10))
    fi
    
    # Check for proper capitalization
    if [[ "${refined:0:1}" != "${refined:0:1^^}" ]]; then
        ((score-=5))
    fi
    
    # Check for paragraph structure (multiple lines)
    local line_count=$(echo "$refined" | wc -l)
    if [[ $line_count -lt 2 ]]; then
        ((score-=10))
    fi
    
    # Check for technical term preservation
    for term in API JSON OAuth Kubernetes PostgreSQL; do
        if echo "$original" | grep -qi "$term" && ! echo "$refined" | grep -q "$term"; then
            ((score-=5))
        fi
    done
    
    echo "$score"
}

# Function to test a single audio file
test_audio_file() {
    local wav_file="$1"
    local category=$(basename "$(dirname "$wav_file")")
    local name=$(basename "$wav_file" .wav)
    local txt_file="${wav_file%.wav}.txt"
    local json_file="${wav_file%.wav}.json"
    local expected_file="${wav_file%.wav}_expected.md"
    
    echo -e "${BLUE}Testing:${NC} $category/$name"
    
    # Read metadata
    local duration=$(jq -r '.duration_seconds // 0' "$json_file" 2>/dev/null || echo "0")
    local word_count=$(jq -r '.word_count // 0' "$json_file" 2>/dev/null || echo "0")
    
    # Determine model based on duration
    local model
    if (( $(echo "$duration <= $MODEL_THRESHOLD" | bc -l) )); then
        model="$MODEL_SHORT"
    else
        model="$MODEL_LONG"
    fi
    
    local model_path="/opt/homebrew/share/whisper-cpp/ggml-${model}.bin"
    if [[ ! -f "$model_path" ]]; then
        echo -e "  ${RED}⚠ Model not found: $model_path${NC}"
        return 1
    fi
    
    # Read reference transcript
    local reference=""
    if [[ -f "$txt_file" ]]; then
        reference=$(cat "$txt_file")
    fi
    
    # ========== STEP 1: TRANSCRIPTION ==========
    echo -e "  ${YELLOW}→${NC} Transcribing with $model model..."
    
    local t0=$(date +%s%N)
    local transcript_file="$RESULTS_DIR/${name}_transcript.txt"
    
    # Run whisper-cpp
    "$WHISPER_CPP" \
        -m "$model_path" \
        -l en \
        -oj \
        -of "${transcript_file%.txt}" \
        --beam-size 3 \
        -t 4 \
        -p 1 \
        "$wav_file" >/dev/null 2>&1
    
    local t1=$(date +%s%N)
    local transcribe_ms=$(( (t1 - t0) / 1000000 ))
    
    # Extract transcribed text
    local transcribed=""
    if [[ -f "${transcript_file%.txt}.json" ]]; then
        transcribed=$(jq -r '.transcription[]?.text // ""' "${transcript_file%.txt}.json" 2>/dev/null | tr '\n' ' ')
        echo "$transcribed" > "$transcript_file"
    fi
    
    # Calculate WER
    local wer=$(calculate_wer "$reference" "$transcribed")
    
    echo -e "    Transcription WER: ${wer}%"
    echo -e "    Transcription time: ${transcribe_ms}ms"
    
    # ========== STEP 2: REFINEMENT ==========
    echo -e "  ${YELLOW}→${NC} Refining with VoxCompose..."
    
    local t2=$(date +%s%N)
    local refined_file="$RESULTS_DIR/${name}_refined.md"
    
    # Run VoxCompose refiner
    echo "$transcribed" | java -jar "$VOXCOMPOSE_JAR" \
        --model llama3.1 \
        --cache \
        --timeout-ms 10000 > "$refined_file" 2>/dev/null
    
    local t3=$(date +%s%N)
    local refine_ms=$(( (t3 - t2) / 1000000 ))
    
    local refined=$(cat "$refined_file")
    
    # Evaluate refinement quality
    local expected=""
    if [[ -f "$expected_file" ]]; then
        expected=$(cat "$expected_file")
    fi
    
    local refinement_score=$(evaluate_refinement "$transcribed" "$refined" "$expected")
    
    echo -e "    Refinement quality: ${refinement_score}/100"
    echo -e "    Refinement time: ${refine_ms}ms"
    
    # ========== STEP 3: PERFORMANCE METRICS ==========
    local total_ms=$((transcribe_ms + refine_ms))
    local speed_ratio=$(echo "scale=2; $total_ms / 1000 / $duration" | bc)
    
    echo -e "  ${GREEN}✓${NC} Total processing: ${total_ms}ms (${speed_ratio}x realtime)"
    
    # Save detailed results
    cat > "$RESULTS_DIR/${name}_results.json" << EOF
{
  "file": "$name",
  "category": "$category",
  "duration_seconds": $duration,
  "word_count": $word_count,
  "model_used": "$model",
  "transcription": {
    "wer_percent": $wer,
    "time_ms": $transcribe_ms,
    "text": $(echo "$transcribed" | jq -Rs .)
  },
  "refinement": {
    "quality_score": $refinement_score,
    "time_ms": $refine_ms,
    "text": $(echo "$refined" | jq -Rs .)
  },
  "performance": {
    "total_time_ms": $total_ms,
    "speed_ratio": "$speed_ratio"
  },
  "reference_text": $(echo "$reference" | jq -Rs .)
}
EOF
    
    # Update statistics
    ((total_samples++))
    total_wer=$(echo "$total_wer + $wer" | bc)
    total_refinement_score=$((total_refinement_score + refinement_score))
    
    # Category stats
    if [[ -z "${category_stats[$category]:-}" ]]; then
        category_stats[$category]="0|0|0|0"
    fi
    IFS='|' read -r cat_count cat_wer cat_score cat_time <<< "${category_stats[$category]}"
    cat_count=$((cat_count + 1))
    cat_wer=$(echo "$cat_wer + $wer" | bc)
    cat_score=$((cat_score + refinement_score))
    cat_time=$((cat_time + total_ms))
    category_stats[$category]="$cat_count|$cat_wer|$cat_score|$cat_time"
    
    # Model stats
    if [[ -z "${model_stats[$model]:-}" ]]; then
        model_stats[$model]="0|0|0"
    fi
    IFS='|' read -r model_count model_wer model_time <<< "${model_stats[$model]}"
    model_count=$((model_count + 1))
    model_wer=$(echo "$model_wer + $wer" | bc)
    model_time=$((model_time + transcribe_ms))
    model_stats[$model]="$model_count|$model_wer|$model_time"
    
    echo ""
}

# ========== RUN TESTS ==========
echo "Running comprehensive tests on golden dataset..."
echo "================================================"
echo ""

# Test all categories
for category_dir in "$GOLDEN_DIR"/*; do
    if [[ -d "$category_dir" ]]; then
        category=$(basename "$category_dir")
        echo -e "${GREEN}=== Testing $category samples ===${NC}"
        
        for wav in "$category_dir"/*.wav; do
            if [[ -f "$wav" ]]; then
                test_audio_file "$wav" || true
            fi
        done
    fi
done

# ========== GENERATE REPORT ==========
echo "================================================"
echo -e "${GREEN}=== COMPREHENSIVE TEST REPORT ===${NC}"
echo "================================================"
echo ""

# Overall statistics
if [[ $total_samples -gt 0 ]]; then
    avg_wer=$(echo "scale=2; $total_wer / $total_samples" | bc)
    avg_refinement=$(echo "scale=1; $total_refinement_score / $total_samples" | bc)
    
    echo "📊 Overall Results:"
    echo "  Total samples tested: $total_samples"
    echo "  Average WER: ${avg_wer}%"
    echo "  Average refinement quality: ${avg_refinement}/100"
    echo ""
fi

# Category breakdown
echo "📁 Results by Category:"
echo "--------------------------------------------"
printf "%-20s %6s %8s %10s %10s\n" "Category" "Count" "Avg WER" "Quality" "Avg Time"
for category in "${!category_stats[@]}"; do
    IFS='|' read -r count wer score time <<< "${category_stats[$category]}"
    if [[ $count -gt 0 ]]; then
        avg_wer=$(echo "scale=2; $wer / $count" | bc)
        avg_score=$(echo "scale=1; $score / $count" | bc)
        avg_time=$(echo "scale=0; $time / $count" | bc)
        printf "%-20s %6d %7.1f%% %9.1f %9dms\n" "$category" "$count" "$avg_wer" "$avg_score" "$avg_time"
    fi
done | sort

echo ""
echo "🎯 Model Performance:"
echo "--------------------------------------------"
printf "%-10s %6s %8s %10s\n" "Model" "Count" "Avg WER" "Avg Time"
for model in "${!model_stats[@]}"; do
    IFS='|' read -r count wer time <<< "${model_stats[$model]}"
    if [[ $count -gt 0 ]]; then
        avg_wer=$(echo "scale=2; $wer / $count" | bc)
        avg_time=$(echo "scale=0; $time / $count" | bc)
        printf "%-10s %6d %7.1f%% %9dms\n" "$model" "$count" "$avg_wer" "$avg_time"
    fi
done | sort

# Performance analysis
echo ""
echo "⚡ Performance Analysis:"
echo "--------------------------------------------"

# Find fastest and slowest samples
fastest=$(find "$RESULTS_DIR" -name "*_results.json" -exec jq -r \
    '[.file, .performance.total_time_ms] | @tsv' {} \; | sort -t$'\t' -k2 -n | head -1)
slowest=$(find "$RESULTS_DIR" -name "*_results.json" -exec jq -r \
    '[.file, .performance.total_time_ms] | @tsv' {} \; | sort -t$'\t' -k2 -nr | head -1)

if [[ -n "$fastest" ]]; then
    echo "  Fastest sample: $fastest"
fi
if [[ -n "$slowest" ]]; then
    echo "  Slowest sample: $slowest"
fi

# Check for samples with poor WER
echo ""
echo "⚠️  Samples Needing Attention (WER > 20%):"
find "$RESULTS_DIR" -name "*_results.json" -exec jq -r \
    'select(.transcription.wer_percent > 20) | 
    "  - \(.file): \(.transcription.wer_percent)% WER"' {} \; 2>/dev/null || echo "  None"

# Recommendations
echo ""
echo "📝 Recommendations:"
if (( $(echo "$avg_wer > 15" | bc -l) )); then
    echo "  - Consider using a larger Whisper model for better accuracy"
elif (( $(echo "$avg_wer > 10" | bc -l) )); then
    echo "  - Transcription accuracy is acceptable but could be improved"
else
    echo "  - Excellent transcription accuracy achieved"
fi

if (( $(echo "$avg_refinement < 80" | bc -l) )); then
    echo "  - Refinement quality needs improvement - review prompts"
elif (( $(echo "$avg_refinement < 90" | bc -l) )); then
    echo "  - Refinement quality is good with room for optimization"
else
    echo "  - Refinement quality is excellent"
fi

echo ""
echo "Detailed results saved to: $RESULTS_DIR"
echo ""

# Create summary JSON
cat > "$RESULTS_DIR/summary.json" << EOF
{
  "test_date": "$(date -Iseconds)",
  "total_samples": $total_samples,
  "average_wer": $avg_wer,
  "average_refinement_quality": $avg_refinement,
  "model_threshold_seconds": $MODEL_THRESHOLD,
  "categories_tested": $(echo "${!category_stats[@]}" | wc -w),
  "results_directory": "$RESULTS_DIR"
}
EOF

echo "Summary saved to: $RESULTS_DIR/summary.json"