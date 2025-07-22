package com.agi.nlp.understanding;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages conversation context and history for dialogue systems
 */
@Slf4j
@Component
public class ConversationContextManager {
    
    private final Map<String, ConversationContext> contexts = new ConcurrentHashMap<>();
    private final int MAX_CONTEXT_HISTORY = 50;
    private final int CONTEXT_TIMEOUT_MINUTES = 30;
    
    @Data
    public static class ConversationContext {
        private String conversationId;
        private String userId;
        private List<ConversationTurn> history;
        private Map<String, Object> sessionData;
        private LocalDateTime lastActivity;
        private String currentTopic;
        private Map<String, Double> topicScores;
        private ConversationState state;
        
        public ConversationContext(String conversationId, String userId) {
            this.conversationId = conversationId;
            this.userId = userId;
            this.history = new ArrayList<>();
            this.sessionData = new HashMap<>();
            this.lastActivity = LocalDateTime.now();
            this.topicScores = new HashMap<>();
            this.state = ConversationState.ACTIVE;
        }
    }
    
    @Data
    public static class ConversationTurn {
        private String turnId;
        private String speaker; // "user" or "assistant"
        private String message;
        private LocalDateTime timestamp;
        private Map<String, Object> metadata;
        private double confidence;
        private String intent;
        private Map<String, String> entities;
        
        public ConversationTurn(String speaker, String message) {
            this.turnId = UUID.randomUUID().toString();
            this.speaker = speaker;
            this.message = message;
            this.timestamp = LocalDateTime.now();
            this.metadata = new HashMap<>();
            this.entities = new HashMap<>();
        }
    }
    
    public enum ConversationState {
        ACTIVE, PAUSED, ENDED, TIMEOUT
    }
    
    /**
     * Create or get conversation context
     */
    public ConversationContext getOrCreateContext(String conversationId, String userId) {
        ConversationContext context = contexts.get(conversationId);
        
        if (context == null) {
            context = new ConversationContext(conversationId, userId);
            contexts.put(conversationId, context);
            log.info("Created new conversation context: {} for user: {}", conversationId, userId);
        } else {
            // Update last activity
            context.setLastActivity(LocalDateTime.now());
        }
        
        return context;
    }
    
    /**
     * Add a turn to the conversation
     */
    public void addTurn(String conversationId, String speaker, String message) {
        ConversationContext context = contexts.get(conversationId);
        if (context == null) {
            log.warn("Attempted to add turn to non-existent conversation: {}", conversationId);
            return;
        }
        
        ConversationTurn turn = new ConversationTurn(speaker, message);
        context.getHistory().add(turn);
        context.setLastActivity(LocalDateTime.now());
        
        // Maintain history size limit
        if (context.getHistory().size() > MAX_CONTEXT_HISTORY) {
            context.getHistory().remove(0);
        }
        
        // Update topic tracking
        updateTopicTracking(context, message);
        
        log.debug("Added turn to conversation {}: {} said '{}'", conversationId, speaker, message);
    }
    
    /**
     * Get conversation history
     */
    public List<ConversationTurn> getHistory(String conversationId) {
        ConversationContext context = contexts.get(conversationId);
        return context != null ? new ArrayList<>(context.getHistory()) : new ArrayList<>();
    }
    
    /**
     * Get recent conversation history (last N turns)
     */
    public List<ConversationTurn> getRecentHistory(String conversationId, int maxTurns) {
        List<ConversationTurn> fullHistory = getHistory(conversationId);
        int startIndex = Math.max(0, fullHistory.size() - maxTurns);
        return fullHistory.subList(startIndex, fullHistory.size());
    }
    
    /**
     * Set session data
     */
    public void setSessionData(String conversationId, String key, Object value) {
        ConversationContext context = contexts.get(conversationId);
        if (context != null) {
            context.getSessionData().put(key, value);
        }
    }
    
    /**
     * Get session data
     */
    public Object getSessionData(String conversationId, String key) {
        ConversationContext context = contexts.get(conversationId);
        return context != null ? context.getSessionData().get(key) : null;
    }
    
    /**
     * Update conversation state
     */
    public void updateState(String conversationId, ConversationState state) {
        ConversationContext context = contexts.get(conversationId);
        if (context != null) {
            context.setState(state);
            context.setLastActivity(LocalDateTime.now());
            log.info("Updated conversation {} state to {}", conversationId, state);
        }
    }
    
    /**
     * Get current topic
     */
    public String getCurrentTopic(String conversationId) {
        ConversationContext context = contexts.get(conversationId);
        return context != null ? context.getCurrentTopic() : null;
    }
    
    /**
     * Generate context summary for AI models
     */
    public String generateContextSummary(String conversationId, int maxTurns) {
        List<ConversationTurn> recentHistory = getRecentHistory(conversationId, maxTurns);
        
        if (recentHistory.isEmpty()) {
            return "No conversation history available.";
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append("Recent conversation context:\n");
        
        for (ConversationTurn turn : recentHistory) {
            summary.append(String.format("%s: %s\n", 
                          turn.getSpeaker().toUpperCase(), turn.getMessage()));
        }
        
        ConversationContext context = contexts.get(conversationId);
        if (context != null && context.getCurrentTopic() != null) {
            summary.append(String.format("\nCurrent topic: %s\n", context.getCurrentTopic()));
        }
        
        return summary.toString();
    }
    
    /**
     * Clean up expired contexts
     */
    public void cleanupExpiredContexts() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(CONTEXT_TIMEOUT_MINUTES);
        
        List<String> expiredContexts = new ArrayList<>();
        for (Map.Entry<String, ConversationContext> entry : contexts.entrySet()) {
            if (entry.getValue().getLastActivity().isBefore(cutoff)) {
                expiredContexts.add(entry.getKey());
            }
        }
        
        for (String contextId : expiredContexts) {
            ConversationContext context = contexts.remove(contextId);
            if (context != null) {
                context.setState(ConversationState.TIMEOUT);
                log.info("Cleaned up expired conversation context: {}", contextId);
            }
        }
    }
    
    /**
     * Get all active conversations for a user
     */
    public List<String> getUserConversations(String userId) {
        return contexts.entrySet().stream()
                .filter(entry -> userId.equals(entry.getValue().getUserId()))
                .filter(entry -> entry.getValue().getState() == ConversationState.ACTIVE)
                .map(Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Update topic tracking based on message content
     */
    private void updateTopicTracking(ConversationContext context, String message) {
        // Simple topic detection based on keywords
        Map<String, List<String>> topicKeywords = Map.of(
            "technology", Arrays.asList("computer", "software", "programming", "code", "algorithm"),
            "personal", Arrays.asList("i", "my", "me", "myself", "personal", "feel"),
            "business", Arrays.asList("company", "business", "work", "job", "career", "money"),
            "science", Arrays.asList("research", "study", "experiment", "theory", "data"),
            "general", Arrays.asList("what", "how", "why", "when", "where")
        );
        
        String lowerMessage = message.toLowerCase();
        Map<String, Double> currentScores = context.getTopicScores();
        
        for (Map.Entry<String, List<String>> entry : topicKeywords.entrySet()) {
            String topic = entry.getKey();
            List<String> keywords = entry.getValue();
            
            double score = keywords.stream()
                    .mapToDouble(keyword -> lowerMessage.contains(keyword) ? 1.0 : 0.0)
                    .sum() / keywords.size();
            
            // Update running average
            double currentScore = currentScores.getOrDefault(topic, 0.0);
            double newScore = (currentScore * 0.7) + (score * 0.3);
            currentScores.put(topic, newScore);
        }
        
        // Update current topic to the one with highest score
        String bestTopic = currentScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("general");
        
        if (currentScores.get(bestTopic) > 0.3) {
            context.setCurrentTopic(bestTopic);
        }
    }
    
    /**
     * Get system statistics
     */
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalContexts", contexts.size());
        stats.put("activeContexts", contexts.values().stream()
                .mapToInt(ctx -> ctx.getState() == ConversationState.ACTIVE ? 1 : 0)
                .sum());
        stats.put("totalTurns", contexts.values().stream()
                .mapToInt(ctx -> ctx.getHistory().size())
                .sum());
        
        return stats;
    }
}

