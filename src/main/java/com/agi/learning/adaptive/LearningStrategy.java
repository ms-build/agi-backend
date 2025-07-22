package com.agi.learning.adaptive;

/**
 * Interface for different adaptive learning strategies
 */
public interface LearningStrategy {
    
    /**
     * Initialize the learning strategy with session parameters
     */
    void initialize(AdaptiveLearningEngine.AdaptiveLearningSession session);
    
    /**
     * Adapt the model based on new data
     */
    boolean adapt(AdaptiveLearningEngine.AdaptiveLearningSession session, 
                  AdaptiveLearningEngine.AdaptationEvent event);
    
    /**
     * Get strategy name
     */
    String getStrategyName();
    
    /**
     * Get strategy description
     */
    String getDescription();
    
    /**
     * Check if strategy supports the given adaptation mode
     */
    boolean supportsMode(AdaptiveLearningEngine.AdaptationMode mode);
    
    /**
     * Get recommended learning rate for this strategy
     */
    double getRecommendedLearningRate();
    
    /**
     * Cleanup resources when session ends
     */
    void cleanup(AdaptiveLearningEngine.AdaptiveLearningSession session);
}

