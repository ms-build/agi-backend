package com.agi.neural.core;

/**
 * Exception thrown when neural model initialization fails
 */
public class ModelInitializationException extends Exception {
    
    private final String modelId;
    
    public ModelInitializationException(String modelId, String message) {
        super(String.format("Failed to initialize model '%s': %s", modelId, message));
        this.modelId = modelId;
    }
    
    public ModelInitializationException(String modelId, String message, Throwable cause) {
        super(String.format("Failed to initialize model '%s': %s", modelId, message), cause);
        this.modelId = modelId;
    }
    
    public String getModelId() {
        return modelId;
    }
}

