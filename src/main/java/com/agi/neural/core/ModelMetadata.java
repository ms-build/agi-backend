package com.agi.neural.core;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Metadata information for neural models
 */
@Data
@Builder
public class ModelMetadata {
    
    private String modelId;
    private String modelType;
    private String version;
    private String description;
    private String author;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;
    
    // Model performance metrics
    private Double accuracy;
    private Double precision;
    private Double recall;
    private Double f1Score;
    private Long totalInferences;
    private Long successfulInferences;
    private Long failedInferences;
    
    // Model configuration
    private Map<String, Object> hyperparameters;
    private Map<String, Object> architecture;
    
    // Resource usage
    private Long modelSizeBytes;
    private Long memoryUsageBytes;
    private Double averageInferenceTimeMs;
    private Double maxInferenceTimeMs;
    
    // Training information
    private String trainingDataset;
    private Long trainingExamples;
    private Integer epochs;
    private String optimizerType;
    private Double learningRate;
    
    // Capabilities
    private boolean supportsBatchInference;
    private boolean supportsExplainability;
    private boolean supportsOnlineLearning;
    private boolean supportsTransferLearning;
    
    // Status
    private ModelStatus status;
    private String statusMessage;
    
    public enum ModelStatus {
        INITIALIZING,
        READY,
        TRAINING,
        UPDATING,
        ERROR,
        DEPRECATED
    }
    
    /**
     * Calculate success rate
     */
    public double getSuccessRate() {
        if (totalInferences == null || totalInferences == 0) {
            return 0.0;
        }
        return (double) successfulInferences / totalInferences * 100.0;
    }
    
    /**
     * Update inference statistics
     */
    public void updateInferenceStats(boolean success, double inferenceTimeMs) {
        if (totalInferences == null) totalInferences = 0L;
        if (successfulInferences == null) successfulInferences = 0L;
        if (failedInferences == null) failedInferences = 0L;
        
        totalInferences++;
        if (success) {
            successfulInferences++;
        } else {
            failedInferences++;
        }
        
        // Update timing statistics
        if (averageInferenceTimeMs == null) {
            averageInferenceTimeMs = inferenceTimeMs;
        } else {
            averageInferenceTimeMs = (averageInferenceTimeMs * (totalInferences - 1) + inferenceTimeMs) / totalInferences;
        }
        
        if (maxInferenceTimeMs == null || inferenceTimeMs > maxInferenceTimeMs) {
            maxInferenceTimeMs = inferenceTimeMs;
        }
        
        lastUpdated = LocalDateTime.now();
    }
}

