package com.agi.emotion;

import com.agi.neural.core.NeuralModel;
import com.agi.neural.core.ModelManager;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Neural network-powered emotion engine for emotion detection, empathetic responses, and emotional intelligence
 */
@Slf4j
@Component
public class NeuralEmotionEngine {
    
    @Autowired
    private ModelManager modelManager;
    
    private final Map<String, EmotionSession> emotionSessions = new ConcurrentHashMap<>();
    private final Map<String, UserEmotionProfile> userProfiles = new ConcurrentHashMap<>();
    
    @Data
    public static class EmotionSession {
        private String sessionId;
        private String userId;
        private LocalDateTime startTime;
        private LocalDateTime lastActivity;
        private List<EmotionAnalysis> analyses;
        private EmotionContext currentContext;
        private Map<String, Object> sessionMetadata;
        private SessionStatus status;
        private double overallEmotionalTone;
        
        public EmotionSession(String userId) {
            this.sessionId = UUID.randomUUID().toString();
            this.userId = userId;
            this.startTime = LocalDateTime.now();
            this.lastActivity = LocalDateTime.now();
            this.analyses = new ArrayList<>();
            this.currentContext = new EmotionContext();
            this.sessionMetadata = new HashMap<>();
            this.status = SessionStatus.ACTIVE;
            this.overallEmotionalTone = 0.0;
        }
    }
    
    @Data
    public static class EmotionAnalysis {
        private String analysisId;
        private String inputText;
        private InputType inputType;
        private Map<String, Double> detectedEmotions;
        private EmotionIntensity overallIntensity;
        private double confidence;
        private List<EmotionIndicator> indicators;
        private LocalDateTime analysisTime;
        private long processingTimeMs;
        
        public EmotionAnalysis(String inputText, InputType inputType) {
            this.analysisId = UUID.randomUUID().toString();
            this.inputText = inputText;
            this.inputType = inputType;
            this.detectedEmotions = new HashMap<>();
            this.indicators = new ArrayList<>();
            this.analysisTime = LocalDateTime.now();
            this.confidence = 0.0;
        }
    }
    
    @Data
    public static class EmotionIndicator {
        private String indicatorType;
        private String textSpan;
        private double strength;
        private String emotion;
        private String explanation;
        
        public EmotionIndicator(String indicatorType, String textSpan, String emotion, double strength) {
            this.indicatorType = indicatorType;
            this.textSpan = textSpan;
            this.emotion = emotion;
            this.strength = strength;
        }
    }
    
    @Data
    public static class EmotionContext {
        private String contextId;
        private Map<String, Double> contextualEmotions;
        private List<String> emotionalHistory;
        private String dominantEmotion;
        private double emotionalStability;
        private LocalDateTime lastUpdate;
        
        public EmotionContext() {
            this.contextId = UUID.randomUUID().toString();
            this.contextualEmotions = new HashMap<>();
            this.emotionalHistory = new ArrayList<>();
            this.emotionalStability = 0.5;
            this.lastUpdate = LocalDateTime.now();
        }
    }
    
    @Data
    public static class UserEmotionProfile {
        private String userId;
        private Map<String, Double> emotionalTraits;
        private List<String> preferredEmotionalResponses;
        private double empathySensitivity;
        private Map<String, Integer> emotionFrequency;
        private LocalDateTime profileCreated;
        private LocalDateTime lastUpdated;
        
        public UserEmotionProfile(String userId) {
            this.userId = userId;
            this.emotionalTraits = new HashMap<>();
            this.preferredEmotionalResponses = new ArrayList<>();
            this.empathySensitivity = 0.7;
            this.emotionFrequency = new HashMap<>();
            this.profileCreated = LocalDateTime.now();
            this.lastUpdated = LocalDateTime.now();
        }
    }
    
    @Data
    public static class EmpatheticResponse {
        private String responseId;
        private String originalInput;
        private Map<String, Double> detectedEmotions;
        private String empatheticMessage;
        private ResponseTone responseTone;
        private double empathyScore;
        private List<String> emotionalCues;
        private LocalDateTime generatedAt;
        
        public EmpatheticResponse(String originalInput) {
            this.responseId = UUID.randomUUID().toString();
            this.originalInput = originalInput;
            this.detectedEmotions = new HashMap<>();
            this.emotionalCues = new ArrayList<>();
            this.empathyScore = 0.0;
            this.generatedAt = LocalDateTime.now();
        }
    }
    
    public enum InputType {
        TEXT, SPEECH, FACIAL_EXPRESSION, GESTURE, MULTIMODAL
    }
    
    public enum EmotionIntensity {
        VERY_LOW, LOW, MODERATE, HIGH, VERY_HIGH
    }
    
    public enum SessionStatus {
        ACTIVE, PAUSED, COMPLETED, EXPIRED
    }
    
    public enum ResponseTone {
        SUPPORTIVE, ENCOURAGING, CALMING, CELEBRATORY, NEUTRAL, PROFESSIONAL
    }
    
    // Primary emotions based on Plutchik's wheel
    private static final List<String> PRIMARY_EMOTIONS = Arrays.asList(
            "joy", "sadness", "anger", "fear", "surprise", "disgust", "trust", "anticipation"
    );
    
    // Secondary emotions
    private static final List<String> SECONDARY_EMOTIONS = Arrays.asList(
            "love", "guilt", "shame", "pride", "envy", "gratitude", "hope", "despair",
            "excitement", "boredom", "confusion", "relief", "frustration", "contentment"
    );
    
    /**
     * Start emotion analysis session
     */
    public EmotionSession startEmotionSession(String userId) {
        EmotionSession session = new EmotionSession(userId);
        emotionSessions.put(session.getSessionId(), session);
        
        // Initialize or update user profile
        userProfiles.computeIfAbsent(userId, UserEmotionProfile::new);
        
        log.info("Started emotion session: {} for user: {}", session.getSessionId(), userId);
        
        return session;
    }
    
    /**
     * Analyze emotions in input
     */
    public CompletableFuture<EmotionAnalysis> analyzeEmotions(String sessionId, 
                                                             String inputText, 
                                                             InputType inputType) {
        
        return CompletableFuture.supplyAsync(() -> {
            EmotionSession session = emotionSessions.get(sessionId);
            if (session == null) {
                throw new IllegalArgumentException("Emotion session not found: " + sessionId);
            }
            
            try {
                long startTime = System.currentTimeMillis();
                
                EmotionAnalysis analysis = new EmotionAnalysis(inputText, inputType);
                
                // Perform neural emotion analysis
                performNeuralEmotionAnalysis(analysis, session);
                
                // Update session context
                updateEmotionContext(session, analysis);
                
                // Update user profile
                updateUserEmotionProfile(session.getUserId(), analysis);
                
                session.getAnalyses().add(analysis);
                session.setLastActivity(LocalDateTime.now());
                
                analysis.setProcessingTimeMs(System.currentTimeMillis() - startTime);
                
                log.info("Analyzed emotions for session: {} with confidence: {}", 
                        sessionId, analysis.getConfidence());
                
                return analysis;
                
            } catch (Exception e) {
                log.error("Emotion analysis failed for session {}: {}", sessionId, e.getMessage(), e);
                throw new RuntimeException("Emotion analysis failed", e);
            }
        });
    }
    
    /**
     * Perform neural emotion analysis
     */
    private void performNeuralEmotionAnalysis(EmotionAnalysis analysis, EmotionSession session) {
        try {
            // Prepare neural input
            Map<String, Object> neuralInput = new HashMap<>();
            neuralInput.put("text", analysis.getInputText());
            neuralInput.put("input_type", analysis.getInputType().name());
            neuralInput.put("context", session.getCurrentContext().getContextualEmotions());
            neuralInput.put("user_profile", userProfiles.get(session.getUserId()));
            
            String modelId = "emotion_analysis_model";
            if (modelManager.isModelReady(modelId)) {
                Map<String, Object> emotionResult = modelManager.predict(modelId, neuralInput);
                
                // Extract primary emotions
                if (emotionResult.containsKey("primary_emotions")) {
                    Map<String, Double> primaryEmotions = (Map<String, Double>) emotionResult.get("primary_emotions");
                    analysis.getDetectedEmotions().putAll(primaryEmotions);
                }
                
                // Extract secondary emotions
                if (emotionResult.containsKey("secondary_emotions")) {
                    Map<String, Double> secondaryEmotions = (Map<String, Double>) emotionResult.get("secondary_emotions");
                    analysis.getDetectedEmotions().putAll(secondaryEmotions);
                }
                
                // Extract emotion indicators
                if (emotionResult.containsKey("indicators")) {
                    List<Map<String, Object>> indicators = (List<Map<String, Object>>) emotionResult.get("indicators");
                    for (Map<String, Object> indicator : indicators) {
                        EmotionIndicator ei = new EmotionIndicator(
                                (String) indicator.get("type"),
                                (String) indicator.get("text_span"),
                                (String) indicator.get("emotion"),
                                ((Number) indicator.get("strength")).doubleValue());
                        ei.setExplanation((String) indicator.get("explanation"));
                        analysis.getIndicators().add(ei);
                    }
                }
                
                // Set confidence and intensity
                analysis.setConfidence(((Number) emotionResult.getOrDefault("confidence", 0.8)).doubleValue());
                
                double maxEmotion = analysis.getDetectedEmotions().values().stream()
                        .mapToDouble(Double::doubleValue)
                        .max()
                        .orElse(0.0);
                analysis.setOverallIntensity(determineIntensity(maxEmotion));
                
            } else {
                // Fallback emotion analysis
                performFallbackEmotionAnalysis(analysis);
            }
            
        } catch (Exception e) {
            log.warn("Neural emotion analysis failed, using fallback: {}", e.getMessage());
            performFallbackEmotionAnalysis(analysis);
        }
    }
    
    /**
     * Generate empathetic response
     */
    public CompletableFuture<EmpatheticResponse> generateEmpatheticResponse(String sessionId, 
                                                                           String inputText,
                                                                           ResponseTone desiredTone) {
        
        return CompletableFuture.supplyAsync(() -> {
            EmotionSession session = emotionSessions.get(sessionId);
            if (session == null) {
                throw new IllegalArgumentException("Emotion session not found: " + sessionId);
            }
            
            try {
                EmpatheticResponse response = new EmpatheticResponse(inputText);
                
                // First analyze emotions in the input
                EmotionAnalysis analysis = analyzeEmotions(sessionId, inputText, InputType.TEXT).get();
                response.setDetectedEmotions(analysis.getDetectedEmotions());
                
                // Generate empathetic response
                generateNeuralEmpatheticResponse(response, session, desiredTone);
                
                log.info("Generated empathetic response for session: {} with empathy score: {}", 
                        sessionId, response.getEmpathyScore());
                
                return response;
                
            } catch (Exception e) {
                log.error("Empathetic response generation failed for session {}: {}", 
                        sessionId, e.getMessage(), e);
                throw new RuntimeException("Empathetic response generation failed", e);
            }
        });
    }
    
    /**
     * Generate neural empathetic response
     */
    private void generateNeuralEmpatheticResponse(EmpatheticResponse response, 
                                                 EmotionSession session, 
                                                 ResponseTone desiredTone) {
        try {
            // Prepare neural input for empathetic response generation
            Map<String, Object> neuralInput = new HashMap<>();
            neuralInput.put("input_text", response.getOriginalInput());
            neuralInput.put("detected_emotions", response.getDetectedEmotions());
            neuralInput.put("desired_tone", desiredTone != null ? desiredTone.name() : ResponseTone.SUPPORTIVE.name());
            neuralInput.put("user_profile", userProfiles.get(session.getUserId()));
            neuralInput.put("emotion_context", session.getCurrentContext());
            
            String modelId = "empathetic_response_model";
            if (modelManager.isModelReady(modelId)) {
                Map<String, Object> responseResult = modelManager.predict(modelId, neuralInput);
                
                // Extract empathetic message
                if (responseResult.containsKey("empathetic_message")) {
                    response.setEmpatheticMessage((String) responseResult.get("empathetic_message"));
                }
                
                // Extract response tone
                if (responseResult.containsKey("response_tone")) {
                    String tone = (String) responseResult.get("response_tone");
                    response.setResponseTone(ResponseTone.valueOf(tone.toUpperCase()));
                }
                
                // Extract empathy score
                if (responseResult.containsKey("empathy_score")) {
                    response.setEmpathyScore(((Number) responseResult.get("empathy_score")).doubleValue());
                }
                
                // Extract emotional cues
                if (responseResult.containsKey("emotional_cues")) {
                    List<String> cues = (List<String>) responseResult.get("emotional_cues");
                    response.getEmotionalCues().addAll(cues);
                }
                
            } else {
                // Fallback empathetic response generation
                generateFallbackEmpatheticResponse(response, desiredTone);
            }
            
        } catch (Exception e) {
            log.warn("Neural empathetic response generation failed, using fallback: {}", e.getMessage());
            generateFallbackEmpatheticResponse(response, desiredTone);
        }
    }
    
    /**
     * Track user emotions over time
     */
    public CompletableFuture<Map<String, Object>> trackEmotionalJourney(String userId, 
                                                                       LocalDateTime startDate,
                                                                       LocalDateTime endDate) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> journey = new HashMap<>();
                
                // Get user's emotion sessions within date range
                List<EmotionSession> userSessions = emotionSessions.values().stream()
                        .filter(session -> session.getUserId().equals(userId))
                        .filter(session -> session.getStartTime().isAfter(startDate) && 
                                         session.getStartTime().isBefore(endDate))
                        .sorted(Comparator.comparing(EmotionSession::getStartTime))
                        .toList();
                
                // Analyze emotional patterns
                Map<String, List<Double>> emotionTimeline = new HashMap<>();
                List<String> emotionalMilestones = new ArrayList<>();
                
                for (EmotionSession session : userSessions) {
                    for (EmotionAnalysis analysis : session.getAnalyses()) {
                        for (Map.Entry<String, Double> emotion : analysis.getDetectedEmotions().entrySet()) {
                            emotionTimeline.computeIfAbsent(emotion.getKey(), k -> new ArrayList<>())
                                          .add(emotion.getValue());
                        }
                        
                        // Identify significant emotional events
                        if (analysis.getOverallIntensity() == EmotionIntensity.VERY_HIGH) {
                            String dominantEmotion = analysis.getDetectedEmotions().entrySet().stream()
                                    .max(Map.Entry.comparingByValue())
                                    .map(Map.Entry::getKey)
                                    .orElse("unknown");
                            emotionalMilestones.add("High " + dominantEmotion + " on " + 
                                                  analysis.getAnalysisTime().toLocalDate());
                        }
                    }
                }
                
                // Calculate emotional trends
                Map<String, Double> emotionalTrends = new HashMap<>();
                for (Map.Entry<String, List<Double>> entry : emotionTimeline.entrySet()) {
                    List<Double> values = entry.getValue();
                    if (values.size() > 1) {
                        double trend = (values.get(values.size() - 1) - values.get(0)) / values.size();
                        emotionalTrends.put(entry.getKey(), trend);
                    }
                }
                
                journey.put("emotion_timeline", emotionTimeline);
                journey.put("emotional_milestones", emotionalMilestones);
                journey.put("emotional_trends", emotionalTrends);
                journey.put("session_count", userSessions.size());
                journey.put("analysis_period", Map.of("start", startDate, "end", endDate));
                
                log.info("Generated emotional journey for user: {} covering {} sessions", 
                        userId, userSessions.size());
                
                return journey;
                
            } catch (Exception e) {
                log.error("Emotional journey tracking failed for user {}: {}", userId, e.getMessage(), e);
                throw new RuntimeException("Emotional journey tracking failed", e);
            }
        });
    }
    
    /**
     * Update emotion context
     */
    private void updateEmotionContext(EmotionSession session, EmotionAnalysis analysis) {
        EmotionContext context = session.getCurrentContext();
        
        // Update contextual emotions with weighted average
        for (Map.Entry<String, Double> emotion : analysis.getDetectedEmotions().entrySet()) {
            double currentValue = context.getContextualEmotions().getOrDefault(emotion.getKey(), 0.0);
            double newValue = (currentValue * 0.7) + (emotion.getValue() * 0.3); // Weighted update
            context.getContextualEmotions().put(emotion.getKey(), newValue);
        }
        
        // Update dominant emotion
        context.setDominantEmotion(
                context.getContextualEmotions().entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse("neutral"));
        
        // Add to emotional history
        if (context.getEmotionalHistory().size() >= 10) {
            context.getEmotionalHistory().remove(0); // Keep only last 10
        }
        context.getEmotionalHistory().add(context.getDominantEmotion());
        
        // Calculate emotional stability
        calculateEmotionalStability(context);
        
        context.setLastUpdate(LocalDateTime.now());
        
        // Update session overall tone
        updateSessionEmotionalTone(session);
    }
    
    /**
     * Update user emotion profile
     */
    private void updateUserEmotionProfile(String userId, EmotionAnalysis analysis) {
        UserEmotionProfile profile = userProfiles.get(userId);
        if (profile == null) return;
        
        // Update emotion frequency
        for (String emotion : analysis.getDetectedEmotions().keySet()) {
            profile.getEmotionFrequency().merge(emotion, 1, Integer::sum);
        }
        
        // Update emotional traits (long-term patterns)
        for (Map.Entry<String, Double> emotion : analysis.getDetectedEmotions().entrySet()) {
            double currentTrait = profile.getEmotionalTraits().getOrDefault(emotion.getKey(), 0.0);
            double newTrait = (currentTrait * 0.95) + (emotion.getValue() * 0.05); // Very slow update
            profile.getEmotionalTraits().put(emotion.getKey(), newTrait);
        }
        
        profile.setLastUpdated(LocalDateTime.now());
    }
    
    /**
     * Fallback methods
     */
    private void performFallbackEmotionAnalysis(EmotionAnalysis analysis) {
        String text = analysis.getInputText().toLowerCase();
        
        // Simple keyword-based emotion detection
        Map<String, List<String>> emotionKeywords = Map.of(
                "joy", Arrays.asList("happy", "glad", "excited", "wonderful", "great"),
                "sadness", Arrays.asList("sad", "depressed", "down", "unhappy", "terrible"),
                "anger", Arrays.asList("angry", "mad", "furious", "annoyed", "frustrated"),
                "fear", Arrays.asList("scared", "afraid", "worried", "anxious", "nervous"),
                "surprise", Arrays.asList("surprised", "amazed", "shocked", "unexpected"),
                "trust", Arrays.asList("trust", "confident", "sure", "reliable")
        );
        
        for (Map.Entry<String, List<String>> entry : emotionKeywords.entrySet()) {
            double score = entry.getValue().stream()
                    .mapToDouble(keyword -> text.contains(keyword) ? 0.7 : 0.0)
                    .max()
                    .orElse(0.1);
            
            if (score > 0.1) {
                analysis.getDetectedEmotions().put(entry.getKey(), score);
            }
        }
        
        // Default neutral if no emotions detected
        if (analysis.getDetectedEmotions().isEmpty()) {
            analysis.getDetectedEmotions().put("neutral", 0.8);
        }
        
        analysis.setConfidence(0.6);
        analysis.setOverallIntensity(EmotionIntensity.MODERATE);
    }
    
    private void generateFallbackEmpatheticResponse(EmpatheticResponse response, ResponseTone desiredTone) {
        String dominantEmotion = response.getDetectedEmotions().entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("neutral");
        
        Map<String, String> empathyTemplates = Map.of(
                "joy", "I can sense your happiness! That's wonderful to hear.",
                "sadness", "I understand this might be difficult for you. I'm here to help.",
                "anger", "I can see you're feeling frustrated. Let's work through this together.",
                "fear", "It's natural to feel concerned. You're not alone in this.",
                "surprise", "That does sound unexpected! How are you feeling about it?",
                "neutral", "I appreciate you sharing this with me."
        );
        
        response.setEmpatheticMessage(empathyTemplates.getOrDefault(dominantEmotion, 
                "Thank you for sharing your thoughts with me."));
        response.setResponseTone(desiredTone != null ? desiredTone : ResponseTone.SUPPORTIVE);
        response.setEmpathyScore(0.7);
        response.getEmotionalCues().add("Tone matching");
        response.getEmotionalCues().add("Emotional validation");
    }
    
    /**
     * Helper methods
     */
    private EmotionIntensity determineIntensity(double maxEmotion) {
        if (maxEmotion >= 0.9) return EmotionIntensity.VERY_HIGH;
        if (maxEmotion >= 0.7) return EmotionIntensity.HIGH;
        if (maxEmotion >= 0.5) return EmotionIntensity.MODERATE;
        if (maxEmotion >= 0.3) return EmotionIntensity.LOW;
        return EmotionIntensity.VERY_LOW;
    }
    
    private void calculateEmotionalStability(EmotionContext context) {
        if (context.getEmotionalHistory().size() < 3) {
            context.setEmotionalStability(0.5);
            return;
        }
        
        // Calculate stability based on emotion consistency
        Map<String, Long> emotionCounts = context.getEmotionalHistory().stream()
                .collect(HashMap::new, (map, emotion) -> map.merge(emotion, 1L, Long::sum), 
                        (map1, map2) -> { map1.putAll(map2); return map1; });
        
        double maxFreq = emotionCounts.values().stream().mapToLong(Long::longValue).max().orElse(1);
        double stability = maxFreq / context.getEmotionalHistory().size();
        
        context.setEmotionalStability(stability);
    }
    
    private void updateSessionEmotionalTone(EmotionSession session) {
        if (session.getAnalyses().isEmpty()) {
            session.setOverallEmotionalTone(0.0);
            return;
        }
        
        // Calculate weighted average of all emotions in session
        double totalPositive = 0.0;
        double totalNegative = 0.0;
        int count = 0;
        
        for (EmotionAnalysis analysis : session.getAnalyses()) {
            for (Map.Entry<String, Double> emotion : analysis.getDetectedEmotions().entrySet()) {
                if (Arrays.asList("joy", "trust", "anticipation", "love", "gratitude", "hope", "excitement", "contentment")
                          .contains(emotion.getKey())) {
                    totalPositive += emotion.getValue();
                } else if (Arrays.asList("sadness", "anger", "fear", "disgust", "guilt", "shame", "envy", "despair")
                                 .contains(emotion.getKey())) {
                    totalNegative += emotion.getValue();
                }
                count++;
            }
        }
        
        if (count > 0) {
            session.setOverallEmotionalTone((totalPositive - totalNegative) / count);
        }
    }
    
    /**
     * Get emotion session
     */
    public Optional<EmotionSession> getEmotionSession(String sessionId) {
        return Optional.ofNullable(emotionSessions.get(sessionId));
    }
    
    /**
     * Get user emotion profile
     */
    public Optional<UserEmotionProfile> getUserEmotionProfile(String userId) {
        return Optional.ofNullable(userProfiles.get(userId));
    }
    
    /**
     * Get system statistics
     */
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("active_emotion_sessions", emotionSessions.size());
        stats.put("user_profiles", userProfiles.size());
        
        // Calculate average emotional tone
        double avgTone = emotionSessions.values().stream()
                .mapToDouble(EmotionSession::getOverallEmotionalTone)
                .average()
                .orElse(0.0);
        stats.put("average_emotional_tone", avgTone);
        
        // Most common emotions
        Map<String, Long> emotionFrequency = new HashMap<>();
        emotionSessions.values().stream()
                .flatMap(session -> session.getAnalyses().stream())
                .flatMap(analysis -> analysis.getDetectedEmotions().keySet().stream())
                .forEach(emotion -> emotionFrequency.merge(emotion, 1L, Long::sum));
        
        stats.put("emotion_frequency", emotionFrequency);
        
        return stats;
    }
}

