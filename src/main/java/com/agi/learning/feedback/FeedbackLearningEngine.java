package com.agi.learning.feedback;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;

/**
 * Feedback Learning Engine for learning from user feedback and improving system responses
 */
@Slf4j
@Component
public class FeedbackLearningEngine {
    
    private final Map<String, FeedbackSession> feedbackSessions = new ConcurrentHashMap<>();
    private final Map<String, FeedbackModel> feedbackModels = new ConcurrentHashMap<>();
    private final List<FeedbackEntry> feedbackHistory = Collections.synchronizedList(new ArrayList<>());
    
    @Data
    public static class FeedbackSession {
        private String sessionId;
        private String userId;
        private String modelId;
        private LocalDateTime startTime;
        private LocalDateTime lastActivity;
        private List<FeedbackEntry> sessionFeedback;
        private Map<String, Object> sessionContext;
        private SessionStatus status;
        private double satisfactionScore;
        private int totalInteractions;
        private int positiveInteractions;
        private int negativeInteractions;
        
        public FeedbackSession(String userId, String modelId) {
            this.sessionId = UUID.randomUUID().toString();
            this.userId = userId;
            this.modelId = modelId;
            this.startTime = LocalDateTime.now();
            this.lastActivity = LocalDateTime.now();
            this.sessionFeedback = new ArrayList<>();
            this.sessionContext = new HashMap<>();
            this.status = SessionStatus.ACTIVE;
            this.satisfactionScore = 0.0;
            this.totalInteractions = 0;
            this.positiveInteractions = 0;
            this.negativeInteractions = 0;
        }
    }
    
    @Data
    public static class FeedbackEntry {
        private String feedbackId;
        private String sessionId;
        private String userId;
        private String modelId;
        private LocalDateTime timestamp;
        private FeedbackType type;
        private String inputText;
        private String outputText;
        private FeedbackRating rating;
        private String comment;
        private Map<String, Object> context;
        private boolean processed;
        private double confidence;
        private List<String> tags;
        
        public FeedbackEntry(String sessionId, String userId, String modelId) {
            this.feedbackId = UUID.randomUUID().toString();
            this.sessionId = sessionId;
            this.userId = userId;
            this.modelId = modelId;
            this.timestamp = LocalDateTime.now();
            this.context = new HashMap<>();
            this.processed = false;
            this.confidence = 0.0;
            this.tags = new ArrayList<>();
        }
    }
    
    @Data
    public static class FeedbackModel {
        private String modelId;
        private String modelName;
        private LocalDateTime createdAt;
        private LocalDateTime lastUpdate;
        private Map<String, Double> featureWeights;
        private List<FeedbackPattern> learnedPatterns;
        private double overallAccuracy;
        private int totalFeedbackProcessed;
        private Map<FeedbackType, Integer> feedbackTypeCount;
        private ModelStatus status;
        
        public FeedbackModel(String modelId, String modelName) {
            this.modelId = modelId;
            this.modelName = modelName;
            this.createdAt = LocalDateTime.now();
            this.lastUpdate = LocalDateTime.now();
            this.featureWeights = new HashMap<>();
            this.learnedPatterns = new ArrayList<>();
            this.overallAccuracy = 0.0;
            this.totalFeedbackProcessed = 0;
            this.feedbackTypeCount = new HashMap<>();
            this.status = ModelStatus.TRAINING;
        }
    }
    
    @Data
    public static class FeedbackPattern {
        private String patternId;
        private String description;
        private Map<String, Object> inputPattern;
        private FeedbackRating expectedRating;
        private double confidence;
        private int occurrenceCount;
        private LocalDateTime firstSeen;
        private LocalDateTime lastSeen;
        
        public FeedbackPattern(String description) {
            this.patternId = UUID.randomUUID().toString();
            this.description = description;
            this.inputPattern = new HashMap<>();
            this.confidence = 0.0;
            this.occurrenceCount = 1;
            this.firstSeen = LocalDateTime.now();
            this.lastSeen = LocalDateTime.now();
        }
    }
    
    public enum FeedbackType {
        RATING,           // Numerical rating (1-5 stars)
        THUMBS_UP_DOWN,   // Simple positive/negative
        CORRECTION,       // User provides correct answer
        EXPLANATION,      // User explains why response was wrong
        PREFERENCE,       // User indicates preference between options
        CONTEXTUAL        // Feedback with additional context
    }
    
    public enum FeedbackRating {
        VERY_NEGATIVE(-2), NEGATIVE(-1), NEUTRAL(0), POSITIVE(1), VERY_POSITIVE(2);
        
        private final int value;
        
        FeedbackRating(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
        
        public static FeedbackRating fromValue(int value) {
            for (FeedbackRating rating : values()) {
                if (rating.value == value) {
                    return rating;
                }
            }
            return NEUTRAL;
        }
    }
    
    public enum SessionStatus {
        ACTIVE, PAUSED, COMPLETED, EXPIRED
    }
    
    public enum ModelStatus {
        TRAINING, READY, UPDATING, ERROR
    }
    
    /**
     * Start a new feedback session
     */
    public FeedbackSession startFeedbackSession(String userId, String modelId) {
        FeedbackSession session = new FeedbackSession(userId, modelId);
        feedbackSessions.put(session.getSessionId(), session);
        
        log.info("Started feedback session: {} for user: {} and model: {}", 
                session.getSessionId(), userId, modelId);
        
        return session;
    }
    
    /**
     * Submit feedback
     */
    public CompletableFuture<FeedbackEntry> submitFeedback(String sessionId, 
                                                          FeedbackType type,
                                                          String inputText,
                                                          String outputText,
                                                          FeedbackRating rating,
                                                          String comment,
                                                          Map<String, Object> context) {
        
        return CompletableFuture.supplyAsync(() -> {
            FeedbackSession session = feedbackSessions.get(sessionId);
            if (session == null) {
                throw new IllegalArgumentException("Feedback session not found: " + sessionId);
            }
            
            if (session.getStatus() != SessionStatus.ACTIVE) {
                throw new IllegalStateException("Feedback session is not active: " + sessionId);
            }
            
            // Create feedback entry
            FeedbackEntry feedback = new FeedbackEntry(sessionId, session.getUserId(), session.getModelId());
            feedback.setType(type);
            feedback.setInputText(inputText);
            feedback.setOutputText(outputText);
            feedback.setRating(rating);
            feedback.setComment(comment);
            if (context != null) {
                feedback.getContext().putAll(context);
            }
            
            // Add to session and global history
            session.getSessionFeedback().add(feedback);
            feedbackHistory.add(feedback);
            
            // Update session statistics
            updateSessionStatistics(session, feedback);
            
            // Process feedback asynchronously
            processFeedbackAsync(feedback);
            
            log.info("Submitted feedback: {} for session: {}", feedback.getFeedbackId(), sessionId);
            
            return feedback;
        });
    }
    
    /**
     * Process feedback asynchronously
     */
    private void processFeedbackAsync(FeedbackEntry feedback) {
        CompletableFuture.runAsync(() -> {
            try {
                // Extract features from feedback
                Map<String, Object> features = extractFeatures(feedback);
                
                // Update or create feedback model
                FeedbackModel model = getOrCreateFeedbackModel(feedback.getModelId());
                
                // Learn from feedback
                learnFromFeedback(model, feedback, features);
                
                // Mark as processed
                feedback.setProcessed(true);
                feedback.setConfidence(calculateFeedbackConfidence(feedback, features));
                
                log.debug("Processed feedback: {}", feedback.getFeedbackId());
                
            } catch (Exception e) {
                log.error("Error processing feedback {}: {}", feedback.getFeedbackId(), e.getMessage(), e);
            }
        });
    }
    
    /**
     * Extract features from feedback
     */
    private Map<String, Object> extractFeatures(FeedbackEntry feedback) {
        Map<String, Object> features = new HashMap<>();
        
        // Text-based features
        if (feedback.getInputText() != null) {
            features.put("input_length", feedback.getInputText().length());
            features.put("input_word_count", feedback.getInputText().split("\\s+").length);
            features.put("has_question", feedback.getInputText().contains("?"));
        }
        
        if (feedback.getOutputText() != null) {
            features.put("output_length", feedback.getOutputText().length());
            features.put("output_word_count", feedback.getOutputText().split("\\s+").length);
        }
        
        // Rating features
        features.put("rating_value", feedback.getRating().getValue());
        features.put("is_positive", feedback.getRating().getValue() > 0);
        features.put("is_negative", feedback.getRating().getValue() < 0);
        
        // Temporal features
        features.put("hour_of_day", feedback.getTimestamp().getHour());
        features.put("day_of_week", feedback.getTimestamp().getDayOfWeek().getValue());
        
        // Context features
        features.putAll(feedback.getContext());
        
        return features;
    }
    
    /**
     * Get or create feedback model
     */
    private FeedbackModel getOrCreateFeedbackModel(String modelId) {
        return feedbackModels.computeIfAbsent(modelId, id -> {
            FeedbackModel model = new FeedbackModel(id, "FeedbackModel_" + id);
            log.info("Created new feedback model: {}", id);
            return model;
        });
    }
    
    /**
     * Learn from feedback
     */
    private void learnFromFeedback(FeedbackModel model, FeedbackEntry feedback, Map<String, Object> features) {
        // Update feature weights based on feedback
        updateFeatureWeights(model, features, feedback.getRating());
        
        // Look for patterns
        identifyPatterns(model, feedback, features);
        
        // Update model statistics
        model.setTotalFeedbackProcessed(model.getTotalFeedbackProcessed() + 1);
        model.getFeedbackTypeCount().merge(feedback.getType(), 1, Integer::sum);
        model.setLastUpdate(LocalDateTime.now());
        
        // Calculate overall accuracy (simplified)
        calculateModelAccuracy(model);
        
        log.debug("Updated feedback model: {} with new feedback", model.getModelId());
    }
    
    /**
     * Update feature weights
     */
    private void updateFeatureWeights(FeedbackModel model, Map<String, Object> features, FeedbackRating rating) {
        double learningRate = 0.1;
        double ratingValue = rating.getValue();
        
        for (Map.Entry<String, Object> feature : features.entrySet()) {
            String featureName = feature.getKey();
            Object featureValue = feature.getValue();
            
            if (featureValue instanceof Number) {
                double numericValue = ((Number) featureValue).doubleValue();
                double currentWeight = model.getFeatureWeights().getOrDefault(featureName, 0.0);
                
                // Simple gradient update
                double newWeight = currentWeight + learningRate * ratingValue * numericValue;
                model.getFeatureWeights().put(featureName, newWeight);
            }
        }
    }
    
    /**
     * Identify patterns in feedback
     */
    private void identifyPatterns(FeedbackModel model, FeedbackEntry feedback, Map<String, Object> features) {
        // Simple pattern identification based on input/output similarity
        for (FeedbackPattern existingPattern : model.getLearnedPatterns()) {
            if (isPatternMatch(existingPattern, features, feedback)) {
                existingPattern.setOccurrenceCount(existingPattern.getOccurrenceCount() + 1);
                existingPattern.setLastSeen(LocalDateTime.now());
                existingPattern.setConfidence(Math.min(1.0, existingPattern.getConfidence() + 0.1));
                return;
            }
        }
        
        // Create new pattern if no match found
        if (Math.abs(feedback.getRating().getValue()) >= 1) { // Only for strong feedback
            FeedbackPattern newPattern = new FeedbackPattern(
                "Pattern for " + feedback.getType() + " with rating " + feedback.getRating());
            newPattern.setInputPattern(new HashMap<>(features));
            newPattern.setExpectedRating(feedback.getRating());
            newPattern.setConfidence(0.5);
            
            model.getLearnedPatterns().add(newPattern);
            log.debug("Created new feedback pattern: {}", newPattern.getPatternId());
        }
    }
    
    /**
     * Check if feedback matches existing pattern
     */
    private boolean isPatternMatch(FeedbackPattern pattern, Map<String, Object> features, FeedbackEntry feedback) {
        // Simple similarity check
        int matchingFeatures = 0;
        int totalFeatures = 0;
        
        for (Map.Entry<String, Object> patternFeature : pattern.getInputPattern().entrySet()) {
            String featureName = patternFeature.getKey();
            Object patternValue = patternFeature.getValue();
            Object feedbackValue = features.get(featureName);
            
            totalFeatures++;
            if (Objects.equals(patternValue, feedbackValue)) {
                matchingFeatures++;
            }
        }
        
        double similarity = totalFeatures > 0 ? (double) matchingFeatures / totalFeatures : 0.0;
        return similarity > 0.7 && pattern.getExpectedRating() == feedback.getRating();
    }
    
    /**
     * Calculate model accuracy
     */
    private void calculateModelAccuracy(FeedbackModel model) {
        // Simplified accuracy calculation based on positive vs negative feedback
        int positiveCount = model.getFeedbackTypeCount().getOrDefault(FeedbackType.THUMBS_UP_DOWN, 0);
        int totalCount = model.getTotalFeedbackProcessed();
        
        if (totalCount > 0) {
            model.setOverallAccuracy((double) positiveCount / totalCount);
        }
        
        // Update model status based on accuracy
        if (model.getOverallAccuracy() > 0.8 && model.getTotalFeedbackProcessed() > 50) {
            model.setStatus(ModelStatus.READY);
        }
    }
    
    /**
     * Calculate feedback confidence
     */
    private double calculateFeedbackConfidence(FeedbackEntry feedback, Map<String, Object> features) {
        // Base confidence on rating strength and context richness
        double ratingConfidence = Math.abs(feedback.getRating().getValue()) / 2.0;
        double contextConfidence = Math.min(1.0, features.size() / 10.0);
        
        return (ratingConfidence + contextConfidence) / 2.0;
    }
    
    /**
     * Update session statistics
     */
    private void updateSessionStatistics(FeedbackSession session, FeedbackEntry feedback) {
        session.setTotalInteractions(session.getTotalInteractions() + 1);
        session.setLastActivity(LocalDateTime.now());
        
        if (feedback.getRating().getValue() > 0) {
            session.setPositiveInteractions(session.getPositiveInteractions() + 1);
        } else if (feedback.getRating().getValue() < 0) {
            session.setNegativeInteractions(session.getNegativeInteractions() + 1);
        }
        
        // Calculate satisfaction score
        double totalScore = session.getSessionFeedback().stream()
                .mapToDouble(f -> f.getRating().getValue())
                .sum();
        session.setSatisfactionScore(totalScore / session.getTotalInteractions());
    }
    
    /**
     * Get feedback session
     */
    public Optional<FeedbackSession> getFeedbackSession(String sessionId) {
        return Optional.ofNullable(feedbackSessions.get(sessionId));
    }
    
    /**
     * Get feedback model
     */
    public Optional<FeedbackModel> getFeedbackModel(String modelId) {
        return Optional.ofNullable(feedbackModels.get(modelId));
    }
    
    /**
     * Get feedback predictions for input
     */
    public CompletableFuture<FeedbackPrediction> predictFeedback(String modelId, 
                                                                String inputText, 
                                                                String outputText,
                                                                Map<String, Object> context) {
        
        return CompletableFuture.supplyAsync(() -> {
            FeedbackModel model = feedbackModels.get(modelId);
            if (model == null) {
                throw new IllegalArgumentException("Feedback model not found: " + modelId);
            }
            
            // Create temporary feedback entry for feature extraction
            FeedbackEntry tempFeedback = new FeedbackEntry("temp", "temp", modelId);
            tempFeedback.setInputText(inputText);
            tempFeedback.setOutputText(outputText);
            if (context != null) {
                tempFeedback.getContext().putAll(context);
            }
            
            Map<String, Object> features = extractFeatures(tempFeedback);
            
            // Calculate prediction score
            double score = 0.0;
            for (Map.Entry<String, Object> feature : features.entrySet()) {
                String featureName = feature.getKey();
                Object featureValue = feature.getValue();
                
                if (featureValue instanceof Number) {
                    double numericValue = ((Number) featureValue).doubleValue();
                    double weight = model.getFeatureWeights().getOrDefault(featureName, 0.0);
                    score += weight * numericValue;
                }
            }
            
            // Normalize score to rating range
            FeedbackRating predictedRating = FeedbackRating.fromValue((int) Math.round(Math.max(-2, Math.min(2, score))));
            double confidence = Math.min(1.0, Math.abs(score) / 2.0);
            
            return new FeedbackPrediction(predictedRating, confidence, features);
        });
    }
    
    /**
     * Get system statistics
     */
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalFeedbackEntries", feedbackHistory.size());
        stats.put("activeSessions", feedbackSessions.values().stream()
                .mapToInt(session -> session.getStatus() == SessionStatus.ACTIVE ? 1 : 0)
                .sum());
        stats.put("totalModels", feedbackModels.size());
        
        // Calculate average satisfaction
        double avgSatisfaction = feedbackSessions.values().stream()
                .mapToDouble(FeedbackSession::getSatisfactionScore)
                .average()
                .orElse(0.0);
        stats.put("averageSatisfaction", avgSatisfaction);
        
        return stats;
    }
    
    @Data
    public static class FeedbackPrediction {
        private FeedbackRating predictedRating;
        private double confidence;
        private Map<String, Object> features;
        private LocalDateTime timestamp;
        
        public FeedbackPrediction(FeedbackRating predictedRating, double confidence, Map<String, Object> features) {
            this.predictedRating = predictedRating;
            this.confidence = confidence;
            this.features = features;
            this.timestamp = LocalDateTime.now();
        }
    }
}

