package com.agi.neural.core;

import java.util.Map;

/**
 * Base interface for all neural network models in the AGI system
 */
public interface NeuralModel<I, O> {
    
    /**
     * Get the unique identifier for this model
     */
    String getModelId();
    
    /**
     * Get the model type (e.g., "text-classifier", "image-generator", etc.)
     */
    String getModelType();
    
    /**
     * Get the model version
     */
    String getVersion();
    
    /**
     * Initialize the model with configuration parameters
     */
    void initialize(Map<String, Object> config) throws ModelInitializationException;
    
    /**
     * Check if the model is ready for inference
     */
    boolean isReady();
    
    /**
     * Perform inference on the input data
     */
    O predict(I input) throws InferenceException;
    
    /**
     * Perform batch inference on multiple inputs
     */
    default java.util.List<O> predictBatch(java.util.List<I> inputs) throws InferenceException {
        return inputs.stream()
                .map(input -> {
                    try {
                        return predict(input);
                    } catch (InferenceException e) {
                        throw new RuntimeException("Batch inference failed", e);
                    }
                })
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Get model metadata and statistics
     */
    ModelMetadata getMetadata();
    
    /**
     * Cleanup resources when model is no longer needed
     */
    void cleanup();
    
    /**
     * Get the confidence score for the last prediction (if applicable)
     */
    default double getLastConfidence() {
        return 0.0;
    }
    
    /**
     * Get explanation for the last prediction (if model supports explainability)
     */
    default String getLastExplanation() {
        return "No explanation available";
    }
}

