package com.agi.neural.core;

/**
 * Exception thrown when neural model inference fails
 */
public class InferenceException extends Exception {
    
    private final String modelId;
    private final Object input;
    
    public InferenceException(String modelId, String message) {
        super(String.format("Inference failed for model '%s': %s", modelId, message));
        this.modelId = modelId;
        this.input = null;
    }
    
    public InferenceException(String modelId, String message, Object input) {
        super(String.format("Inference failed for model '%s': %s", modelId, message));
        this.modelId = modelId;
        this.input = input;
    }
    
    public InferenceException(String modelId, String message, Throwable cause) {
        super(String.format("Inference failed for model '%s': %s", modelId, message), cause);
        this.modelId = modelId;
        this.input = null;
    }
    
    public InferenceException(String modelId, String message, Object input, Throwable cause) {
        super(String.format("Inference failed for model '%s': %s", modelId, message), cause);
        this.modelId = modelId;
        this.input = input;
    }
    
    public String getModelId() {
        return modelId;
    }
    
    public Object getInput() {
        return input;
    }
}

