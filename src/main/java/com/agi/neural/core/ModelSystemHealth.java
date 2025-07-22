package com.agi.neural.core;

import lombok.Builder;
import lombok.Data;

/**
 * System health information for the neural network infrastructure
 */
@Data
@Builder
public class ModelSystemHealth {
    
    private int totalModels;
    private int readyModels;
    private int errorModels;
    private long totalInferences;
    private double averageSuccessRate;
    private double systemLoad;
    private long memoryUsage;
    private long availableMemory;
    
    /**
     * Get the percentage of models that are ready
     */
    public double getReadyModelPercentage() {
        if (totalModels == 0) return 0.0;
        return (double) readyModels / totalModels * 100.0;
    }
    
    /**
     * Check if the system is healthy
     */
    public boolean isHealthy() {
        return getReadyModelPercentage() >= 80.0 && averageSuccessRate >= 90.0;
    }
    
    /**
     * Get system status
     */
    public SystemStatus getStatus() {
        if (totalModels == 0) {
            return SystemStatus.NO_MODELS;
        }
        
        double readyPercentage = getReadyModelPercentage();
        
        if (readyPercentage >= 90.0 && averageSuccessRate >= 95.0) {
            return SystemStatus.EXCELLENT;
        } else if (readyPercentage >= 80.0 && averageSuccessRate >= 90.0) {
            return SystemStatus.GOOD;
        } else if (readyPercentage >= 60.0 && averageSuccessRate >= 80.0) {
            return SystemStatus.FAIR;
        } else if (readyPercentage >= 40.0) {
            return SystemStatus.POOR;
        } else {
            return SystemStatus.CRITICAL;
        }
    }
    
    public enum SystemStatus {
        EXCELLENT,
        GOOD,
        FAIR,
        POOR,
        CRITICAL,
        NO_MODELS
    }
}

