#!/usr/bin/env bash
# Generate golden test dataset for VoxCompose refinement testing
# Creates synthetic audio files 21+ seconds in length for testing with macos-ptt-dictation
# Follows the model switching threshold at 21 seconds

set -euo pipefail

GOLDEN_DIR="tests/fixtures/golden"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "=== VoxCompose Golden Dataset Generator ==="
echo "Creating test audio files (21+ seconds) for refinement testing"
echo ""

# Create directory structure
mkdir -p "$GOLDEN_DIR"/{short_threshold,medium_length,long_form,technical,natural_speech,meeting_notes}

# Function to generate audio with specific voice and metadata
generate_sample() {
    local category="$1"
    local name="$2"
    local voice="$3"
    local rate="$4"
    local text="$5"
    local output_dir="$GOLDEN_DIR/$category"
    
    echo "Generating $category/$name (voice: $voice, rate: $rate wpm)..."
    
    # Save the exact transcript (input to refiner)
    echo "$text" > "$output_dir/${name}.txt"
    
    # Generate audio using macOS text-to-speech
    say -v "$voice" -r "$rate" -o "$output_dir/${name}_raw.aiff" "$text" 2>/dev/null || {
        echo "  Warning: Failed to generate audio for $name"
        return 1
    }
    
    # Convert to 16kHz mono WAV (Whisper standard)
    ffmpeg -i "$output_dir/${name}_raw.aiff" \
           -ar 16000 \
           -ac 1 \
           -c:a pcm_s16le \
           -y \
           "$output_dir/${name}.wav" 2>/dev/null
    
    rm -f "$output_dir/${name}_raw.aiff"
    
    # Get actual duration
    local duration=$(ffprobe -v error -show_entries format=duration \
                    -of default=noprint_wrappers=1:nokey=1 \
                    "$output_dir/${name}.wav" 2>/dev/null || echo "0")
    
    # Create metadata JSON
    cat > "$output_dir/${name}.json" << EOF
{
  "name": "$name",
  "category": "$category",
  "voice": "$voice",
  "rate": $rate,
  "duration_seconds": $duration,
  "word_count": $(echo "$text" | wc -w),
  "char_count": ${#text},
  "purpose": "refinement_test",
  "model_threshold": "21_seconds",
  "expected_model": $(awk "BEGIN {print ($duration > 21) ? \"medium.en\" : \"base.en\"}")
}
EOF
    
    # Create expected refined output (what voxcompose should produce)
    # This simulates perfect refinement - proper punctuation, paragraphs, clean text
    create_expected_refinement "$output_dir" "$name" "$text"
    
    echo "  ✓ Generated: $(printf "%.1f" $duration)s duration"
}

# Function to create expected refined output
create_expected_refinement() {
    local dir="$1"
    local name="$2"
    local raw_text="$3"
    
    # For now, save a cleaned version
    # In practice, you'd manually review and perfect these
    echo "$raw_text" | \
        sed 's/\. \+/.\n\n/g' | \
        sed 's/um, //g' | \
        sed 's/uh, //g' | \
        sed 's/, you know,//g' > "$dir/${name}_expected.md"
}

# Check voice availability
check_voice() {
    local voice="$1"
    say -v "?" 2>/dev/null | grep -q "^$voice" && echo "$voice" || echo "Samantha"
}

# Available voices
VOICE_SAMANTHA=$(check_voice "Samantha")      # American female
VOICE_DANIEL=$(check_voice "Daniel")          # British male
VOICE_ALEX=$(check_voice "Alex")              # American male
VOICE_KAREN=$(check_voice "Karen")            # Australian female

# Speaking rates for different durations
RATE_SLOW=140      # Slower for longer content
RATE_NORMAL=170    # Normal pace
RATE_FAST=200      # Fast for dense content

# ============ SHORT THRESHOLD (21-25 seconds) ============
# These test the boundary of model switching

generate_sample "short_threshold" "meeting_intro_21s" "$VOICE_SAMANTHA" "$RATE_NORMAL" \
"Good morning everyone. Let's begin today's standup meeting. First, I'll provide an update on the API refactoring project. We've successfully migrated three endpoints to the new architecture. The authentication service is now fully operational with OAuth two point zero support. Performance testing shows a thirty percent improvement in response times. Next, we need to discuss the database migration timeline. The staging environment is ready for testing. Quality assurance will begin their review process tomorrow morning. Please ensure all your code is committed to the feature branch by end of day. Are there any blockers or concerns we should address?"

generate_sample "short_threshold" "technical_explanation_23s" "$VOICE_DANIEL" "$RATE_NORMAL" \
"Let me explain how the caching mechanism works in our application. When a request comes in, we first check the Redis cache for the data. If it's a cache hit, we return the data immediately with a response time under fifty milliseconds. For cache misses, we query the PostgreSQL database and then store the result in Redis with a time to live of five minutes. This approach significantly reduces database load during peak traffic periods. We also implemented cache warming strategies for frequently accessed data. The cache invalidation happens through a pub sub mechanism whenever data is updated. This ensures consistency across all application instances."

# ============ MEDIUM LENGTH (30-40 seconds) ============
# Typical dictation length for detailed thoughts

generate_sample "medium_length" "code_review_notes_35s" "$VOICE_ALEX" "$RATE_NORMAL" \
"During the code review, I noticed several areas that need improvement. First, the error handling in the authentication module is inconsistent. Some functions return null while others throw exceptions. We should standardize on a single approach. Second, the database queries in the user service are not optimized. We're seeing N plus one query problems in several endpoints. Consider using eager loading or batch fetching to resolve this. Third, the logging statements lack contextual information. We need to add request IDs and user identifiers to make debugging easier. Fourth, the test coverage for the payment module is below our threshold. Please add unit tests for the edge cases we discussed. Finally, the documentation needs updating to reflect the recent API changes. The swagger definitions are out of sync with the actual implementation."

generate_sample "medium_length" "project_update_38s" "$VOICE_KAREN" "$RATE_SLOW" \
"I wanted to provide a comprehensive update on the migration project. Phase one, which involved setting up the new infrastructure, is now complete. All servers are provisioned and configured according to the specifications. Phase two began last week with the data migration scripts. We've successfully migrated fifty percent of the historical data without any issues. The validation scripts confirm data integrity is maintained. Phase three will focus on migrating the application services. We're planning a gradual rollout starting with non-critical services. The monitoring dashboards are ready and will track key metrics during the migration. Risk mitigation strategies are in place, including rollback procedures and data backups. The estimated completion date remains on track for the end of next month. The team has been working exceptionally well together, and stakeholder feedback has been positive."

# ============ LONG FORM (40-50+ seconds) ============
# Extended dictation for detailed documentation

generate_sample "long_form" "architecture_decision_45s" "$VOICE_SAMANTHA" "$RATE_NORMAL" \
"Today we're documenting our decision to adopt a microservices architecture for the platform redesign. After extensive evaluation, we've identified several key benefits that align with our business objectives. First, independent deployment capability will allow teams to release features without coordinating with other services. This addresses our current bottleneck where monolithic deployments require extensive regression testing. Second, technology diversity enables us to choose the best tool for each service. The recommendation engine can use Python for machine learning while the API gateway uses Node.js for high throughput. Third, horizontal scaling becomes more cost-effective as we can scale individual services based on demand. The user service might need ten instances while the reporting service needs only two. Fourth, fault isolation improves system resilience. If the notification service fails, the core application continues functioning. However, we must also consider the trade-offs. Distributed systems complexity requires sophisticated monitoring and tracing. Network latency between services could impact performance. Data consistency across services needs careful design. The team will need training on distributed systems concepts. Despite these challenges, the benefits outweigh the costs for our use case."

generate_sample "long_form" "retrospective_summary_48s" "$VOICE_DANIEL" "$RATE_SLOW" \
"Let me summarize the key points from today's sprint retrospective. The team identified three major successes this sprint. First, we delivered all committed user stories despite the unexpected production issue mid-sprint. This demonstrates our improved capacity planning. Second, pair programming sessions led to better code quality and knowledge sharing. Junior developers reported feeling more confident with the codebase. Third, our automated testing caught several critical bugs before they reached staging. The investment in test infrastructure is paying dividends. Moving to areas for improvement, communication between frontend and backend teams needs attention. We had several instances where API changes weren't communicated promptly, causing integration delays. The team suggests daily sync meetings between tech leads. Another concern is technical debt accumulation. We're adding features quickly but not allocating time for refactoring. The proposal is to dedicate twenty percent of each sprint to debt reduction. Finally, the deployment process remains manual and error-prone. We need to prioritize the CI CD pipeline improvements discussed last quarter. For the next sprint, we're committing to address the communication issues and begin the deployment automation work."

# ============ TECHNICAL CONTENT (Complex terminology) ============
# Tests refinement of technical jargon and acronyms

generate_sample "technical" "devops_discussion_42s" "$VOICE_ALEX" "$RATE_FAST" \
"The DevOps transformation requires several infrastructure changes. We're implementing infrastructure as code using Terraform for AWS resource provisioning. The EKS cluster configuration includes auto-scaling groups with spot instances for cost optimization. Kubernetes manifests are managed through Helm charts with GitOps workflows via ArgoCD. The CI CD pipeline uses Jenkins with Blue Ocean for visualization. Container images are scanned for vulnerabilities using Trivy before pushing to ECR. Monitoring is handled by Prometheus with Grafana dashboards for visualization. Log aggregation uses the ELK stack: Elasticsearch, Logstash, and Kibana. For secrets management, we're using HashiCorp Vault with dynamic credentials for database access. Service mesh implementation with Istio provides traffic management and security policies. The backup strategy includes automated snapshots of EBS volumes and RDS instances. Disaster recovery procedures are documented with RTO of four hours and RPO of one hour. Load testing with K6 ensures the system handles expected traffic patterns."

# ============ NATURAL SPEECH (With disfluencies) ============
# Tests handling of natural speaking patterns

generate_sample "natural_speech" "thinking_aloud_40s" "$VOICE_KAREN" "$RATE_NORMAL" \
"So, um, I've been thinking about the architecture problem we discussed yesterday. You know, the issue with the, uh, message queue getting backed up during peak hours. I think, well, what if we, like, implemented a circuit breaker pattern? That way, um, when the queue reaches a certain threshold, we could, you know, temporarily redirect messages to a secondary queue. Or, uh, maybe we could use a different approach altogether. What about, um, implementing back pressure? That would, like, slow down the producers when the consumers can't keep up. Actually, wait, I just remembered something. We tried that last year with the, uh, what was it called, the notification service? And it, well, it didn't work out because of the, you know, the timeout issues. Hmm, maybe we need to, like, reconsider the whole architecture. What if we moved to an event-driven model instead?"

# ============ MEETING NOTES (Business context) ============
# Tests refinement for business documentation

generate_sample "meeting_notes" "quarterly_planning_50s" "$VOICE_SAMANTHA" "$RATE_SLOW" \
"Welcome to the Q3 planning session. Our primary objective this quarter is launching the mobile application while maintaining stability in the web platform. The mobile app development is currently sixty percent complete with the core features implemented. The remaining work includes payment integration, push notifications, and offline synchronization. We're targeting a soft launch in select markets by the end of August. For the web platform, we're planning incremental improvements based on user feedback. The analytics dashboard shows engagement metrics are up fifteen percent from last quarter. Customer satisfaction scores have improved to four point two out of five. However, we're seeing increased support tickets related to the checkout process. This will be a priority fix in the first sprint. Budget allocation for the quarter is three hundred thousand dollars, with sixty percent dedicated to development and forty percent to marketing initiatives. The marketing team will focus on user acquisition campaigns in preparation for the mobile launch. We need to hire two additional developers to meet our timeline. HR is actively recruiting with interviews scheduled next week. Risk factors include potential delays in app store approval and third-party API dependencies. We have contingency plans for both scenarios."

# ============ PERFORMANCE TEST SAMPLES ============
# Specific lengths for benchmarking

generate_sample "short_threshold" "exactly_21_seconds" "$VOICE_DANIEL" "$RATE_NORMAL" \
"This is a carefully crafted message designed to be exactly twenty-one seconds when spoken at a normal rate. The purpose is to test the threshold behavior of the model selection logic. At twenty-one seconds, the system should switch from the base model to the medium model for better accuracy. This transition point was determined through extensive testing and analysis. We found that shorter clips work well with the base model while longer clips benefit from the medium model's improved context understanding."

generate_sample "medium_length" "exactly_30_seconds" "$VOICE_ALEX" "$RATE_NORMAL" \
"This thirty-second sample represents a typical medium-length dictation. It contains enough content to test the refiner's ability to maintain context across multiple sentences. The text includes various punctuation marks, numbers like twenty-three and forty-seven, and technical terms such as API, JSON, and OAuth. We're also testing the handling of abbreviations like CEO, CTO, and PM. The refiner should preserve the structure while improving readability. This includes proper paragraph breaks where appropriate. It should also handle domain-specific terminology correctly without over-correcting technical jargon."

generate_sample "long_form" "exactly_45_seconds" "$VOICE_KAREN" "$RATE_NORMAL" \
"This forty-five second recording tests the refiner's performance on longer content. When processing extended transcripts, the refiner must maintain consistency throughout the document. It should identify natural break points for paragraphs based on topic changes. The system should handle various speaking styles, from formal presentation language to casual conversational tone. Technical accuracy is crucial when dealing with specialized vocabulary. Terms like Kubernetes, PostgreSQL, and WebSocket should be preserved correctly. Numbers and measurements need special attention. For example, ninety-nine point nine percent uptime, five hundred millisecond response time, and two terabytes of data. The refiner should also recognize and format lists appropriately. First, identify the list structure. Second, maintain consistent formatting. Third, preserve the logical flow. Finally, ensure readability. Acronyms present another challenge. NASA, FBI, API, REST, SOAP, and GraphQL should remain uppercase. The system must distinguish between acronyms and regular words. This level of detail is essential for professional documentation."

# ============ EDGE CASES ============

generate_sample "technical" "mixed_accents_terminologies_25s" "$VOICE_SAMANTHA" "$RATE_FAST" \
"The Façade pattern résumé includes naïve implementations. Señor García's café uses the FIFO queue. The Möbius strip algorithm has O(n²) complexity. MySQL's InnoDB storage engine handles UTF-8 encoding. The Pokémon GO API uses José's OAuth library. Björk's Ångström measurements. École Polytechnique's research on Bézier curves."

# ============ SUMMARY ============

echo ""
echo "=== Golden Dataset Generation Complete ==="
echo ""

# Count files by category
for dir in "$GOLDEN_DIR"/*; do
    if [ -d "$dir" ]; then
        category=$(basename "$dir")
        count=$(find "$dir" -name "*.wav" | wc -l)
        printf "%-20s: %d samples\n" "$category" "$count"
    fi
done

echo ""
echo "Duration distribution:"
for wav in "$GOLDEN_DIR"/*/*.wav; do
    if [ -f "$wav" ]; then
        duration=$(ffprobe -v error -show_entries format=duration \
                  -of default=noprint_wrappers=1:nokey=1 "$wav" 2>/dev/null || echo "0")
        name=$(basename "$wav" .wav)
        printf "  %-35s: %6.1f seconds\n" "$name" "$duration"
    fi
done | sort -t: -k2 -n

echo ""
echo "Statistics:"
total_duration=$(find "$GOLDEN_DIR" -name "*.wav" -exec \
    ffprobe -v error -show_entries format=duration \
    -of default=noprint_wrappers=1:nokey=1 {} \; 2>/dev/null | \
    awk '{sum+=$1} END {printf "%.1f", sum}')
total_files=$(find "$GOLDEN_DIR" -name "*.wav" | wc -l)
echo "  Total audio files: $total_files"
echo "  Total duration: ${total_duration}s"
echo "  Average duration: $(awk "BEGIN {printf \"%.1f\", $total_duration / $total_files}")s"

echo ""
echo "Next steps:"
echo "1. Run transcription tests: tests/test_transcription_accuracy.sh"
echo "2. Run refinement tests: tests/test_refinement_quality.sh"
echo "3. Run performance benchmark: tests/benchmark_performance.sh"