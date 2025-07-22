package com.agi.learning.adaptive;

import com.agi.neural.core.NeuralModel;
import com.agi.neural.core.ModelManager;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;

/**
 * Engine for adaptive learning that allows models to learn and adapt in real-time
 */
@Slf4j
@Component
public class AdaptiveLearningEngine {
    
    @Autowired
    private ModelManager modelManager;
    
    private final Map<String, AdaptiveLearningSession> activeSessions = new ConcurrentHashMap<>();
    private final Map<String, LearningStrategy> strategies = new ConcurrentHashMap<>();
    
    @Data
    public static class AdaptiveLearningSession {
        private String sessionId;
        private String modelId;
        private LearningStrategy strategy;
        private AdaptationMode mode;
        private LocalDateTime startTime;
        private LocalDateTime lastUpdate;
        private Map<String, Object> learningData;
        private List<AdaptationEvent> adaptationHistory;
        private double learningRate;
        private double adaptationScore;
        private SessionStatus status;
        private String errorMessage;
        
        public AdaptiveLearningSession(String modelId, LearningStrategy strategy, AdaptationMode mode) {
            this.sessionId = UUID.randomUUID().toString();
            this.modelId = modelId;
            this.strategy = strategy;
            this.mode = mode;
            this.startTime = LocalDateTime.now();
            this.lastUpdate = LocalDateTime.now();
            this.learningData = new HashMap<>();
            this.adaptationHistory = new ArrayList<>();
            this.learningRate = 0.01; // Default learning rate
            this.adaptationScore = 0.0;
            this.status = SessionStatus.ACTIVE;
        }
    }
    
    @Data
    public static class AdaptationEvent {
        private String eventId;
        private LocalDateTime timestamp;
        private AdaptationType type;
        private Map<String, Object> inputData;
        private Map<String, Object> outputData;
        private double confidence;
        private boolean successful;
        private String description;
        
        public AdaptationEvent(AdaptationType type, Map<String, Object> inputData) {
            this.eventId = UUID.randomUUID().toString();
            this.timestamp = LocalDateTime.now();
            this.type = type;
            this.inputData = inputData != null ? new HashMap<>(inputData) : new HashMap<>();
            this.outputData = new HashMap<>();
            this.confidence = 0.0;
            this.successful = false;
        }
    }
    
    public enum AdaptationMode {
        ONLINE_LEARNING,      // Learn from each new example immediately
        BATCH_ADAPTATION,     // Accumulate examples and adapt in batches
        INCREMENTAL_LEARNING, // Gradually incorporate new knowledge
        CONTEXTUAL_ADAPTATION,// Adapt based on context changes
        FEEDBACK_DRIVEN,      // Adapt based on user feedback
        PERFORMANCE_BASED     // Adapt based on performance metrics
    }
    
    public enum AdaptationType {
        PARAMETER_UPDATE,     // Update model parameters
        ARCHITECTURE_CHANGE,  // Modify model architecture
        FEATURE_ADAPTATION,   // Adapt feature representations
        STRATEGY_ADJUSTMENT,  // Adjust learning strategy
        CONTEXT_UPDATE,       // Update contextual understanding
        KNOWLEDGE_INTEGRATION // Integrate new knowledge
    }
    
    public enum SessionStatus {
        ACTIVE, PAUSED, COMPLETED, FAILED, CANCELLED
    }
    
    /**
     * Start adaptive learning session for a model
     */
    public AdaptiveLearningSession startAdaptiveLearning(String modelId, 
                                                        LearningStrategy strategy, 
                                                        AdaptationMode mode) {
        
        log.info("Starting adaptive learning session for model: {} with strategy: {} and mode: {}", 
                modelId, strategy.getClass().getSimpleName(), mode);
        
        // Validate model exists and is ready
        if (!modelManager.isModelReady(modelId)) {
            throw new IllegalArgumentException("Model not ready for adaptive learning: " + modelId);
        }
        
        AdaptiveLearningSession session = new AdaptiveLearningSession(modelId, strategy, mode);
        activeSessions.put(session.getSessionId(), session);
        
        // Initialize learning strategy
        strategy.initialize(session);
        
        log.info("Adaptive learning session started: {}", session.getSessionId());
        return session;
    }
    
    /**
     * Process new learning data
     */
    public CompletableFuture<AdaptationEvent> processLearningData(String sessionId, 
                                                                 Map<String, Object> inputData,
                                                                 Map<String, Object> expectedOutput) {
        
        return CompletableFuture.supplyAsync(() -> {
            AdaptiveLearningSession session = activeSessions.get(sessionId);
            if (session == null) {
                throw new IllegalArgumentException("Learning session not found: " + sessionId);
            }
            
            if (session.getStatus() != SessionStatus.ACTIVE) {
                throw new IllegalStateException("Learning session is not active: " + sessionId);
            }
            
            try {
                // Create adaptation event
                AdaptationEvent event = new AdaptationEvent(AdaptationType.PARAMETER_UPDATE, inputData);
                event.getOutputData().putAll(expectedOutput != null ? expectedOutput : new HashMap<>());
                
                // Apply learning strategy
                boolean success = session.getStrategy().adapt(session, event);
                event.setSuccessful(success);
                
                if (success) {
                    // Update session statistics
                    updateSessionStatistics(session, event);
                    session.getAdaptationHistory().add(event);
                    session.setLastUpdate(LocalDateTime.now());
                    
                    log.debug("Successfully processed learning data for session: {}", sessionId);
                } else {
                    log.warn("Failed to process learning data for session: {}", sessionId);
                }
                
                return event;
                
            } catch (Exception e) {
                log.error("Error processing learning data for session {}: {}", sessionId, e.getMessage(), e);
                session.setStatus(SessionStatus.FAILED);
                session.setErrorMessage(e.getMessage());
                throw new RuntimeException("Failed to process learning data", e);
            }
        });
    }
    
    /**
     * Get learning session status
     */
    public Optional<AdaptiveLearningSession> getLearningSession(String sessionId) {
        return Optional.ofNullable(activeSessions.get(sessionId));
    }
    
    /**
     * Pause learning session
     */
    public void pauseLearningSession(String sessionId) {
        AdaptiveLearningSession session = activeSessions.get(sessionId);
        if (session != null && session.getStatus() == SessionStatus.ACTIVE) {
            session.setStatus(SessionStatus.PAUSED);
            log.info("Paused learning session: {}", sessionId);
        }
    }
    
    /**
     * Resume learning session
     */
    public void resumeLearningSession(String sessionId) {
        AdaptiveLearningSession session = activeSessions.get(sessionId);
        if (session != null && session.getStatus() == SessionStatus.PAUSED) {
            session.setStatus(SessionStatus.ACTIVE);
            log.info("Resumed learning session: {}", sessionId);
        }
    }
    
    /**
     * Stop learning session
     */
    public void stopLearningSession(String sessionId) {
        AdaptiveLearningSession session = activeSessions.remove(sessionId);
        if (session != null) {
            session.setStatus(SessionStatus.COMPLETED);
            session.getStrategy().cleanup(session);
            log.info("Stopped learning session: {}", sessionId);
        }
    }
    
    /**
     * Register learning strategy
     */
    public void registerStrategy(String name, LearningStrategy strategy) {
        strategies.put(name, strategy);
        log.info("Registered learning strategy: {}", name);
    }
    
    /**
     * Get available strategies
     */
    public Set<String> getAvailableStrategies() {
        return new HashSet<>(strategies.keySet());
    }
    
    /**
     * Get strategy by name
     */
    public Optional<LearningStrategy> getStrategy(String name) {
        return Optional.ofNullable(strategies.get(name));
    }
    
    /**
     * Update session statistics
     */
    private void updateSessionStatistics(AdaptiveLearningSession session, AdaptationEvent event) {
        // Update adaptation score based on event success and confidence
        double eventScore = event.isSuccessful() ? event.getConfidence() : -0.1;
        session.setAdaptationScore(session.getAdaptationScore() * 0.9 + eventScore * 0.1);
        
        // Adjust learning rate based on recent performance
        adjustLearningRate(session);
    }
    
    /**
     * Adjust learning rate based on performance
     */
    private void adjustLearningRate(AdaptiveLearningSession session) {
        List<AdaptationEvent> recentEvents = session.getAdaptationHistory();
        if (recentEvents.size() < 10) return;
        
        // Get last 10 events
        List<AdaptationEvent> lastEvents = recentEvents.subList(
            Math.max(0, recentEvents.size() - 10), recentEvents.size());
        
        double successRate = lastEvents.stream()
                .mapToDouble(event -> event.isSuccessful() ? 1.0 : 0.0)
                .average()
                .orElse(0.5);
        
        // Adjust learning rate based on success rate
        if (successRate > 0.8) {
            // High success rate - can increase learning rate slightly
            session.setLearningRate(Math.min(0.1, session.getLearningRate() * 1.05));
        } else if (successRate < 0.4) {
            // Low success rate - decrease learning rate
            session.setLearningRate(Math.max(0.001, session.getLearningRate() * 0.95));
        }
    }
    
    /**
     * Get all active learning sessions
     */
    public List<AdaptiveLearningSession> getActiveSessions() {
        return activeSessions.values().stream()
                .filter(session -> session.getStatus() == SessionStatus.ACTIVE)
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Get system statistics
     */
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("activeSessions", activeSessions.size());
        stats.put("availableStrategies", strategies.size());
        stats.put("totalAdaptationEvents", activeSessions.values().stream()
                .mapToInt(session -> session.getAdaptationHistory().size())
                .sum());
        
        // Calculate average adaptation score
        double avgScore = activeSessions.values().stream()
                .mapToDouble(AdaptiveLearningSession::getAdaptationScore)
                .average()
                .orElse(0.0);
        stats.put("averageAdaptationScore", avgScore);
        
        return stats;
    }
    
    /**
     * Cleanup expired sessions
     */
    public void cleanupExpiredSessions() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        
        List<String> expiredSessions = activeSessions.entrySet().stream()
                .filter(entry -> entry.getValue().getLastUpdate().isBefore(cutoff))
                .map(Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toList());
        
        for (String sessionId : expiredSessions) {
            stopLearningSession(sessionId);
            log.info("Cleaned up expired learning session: {}", sessionId);
        }
    }
}

