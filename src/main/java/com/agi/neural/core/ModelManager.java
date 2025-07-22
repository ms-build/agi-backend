package com.agi.neural.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Central manager for all neural network models in the AGI system
 */
@Slf4j
@Component
public class ModelManager {
    
    private final Map<String, NeuralModel<?, ?>> models = new ConcurrentHashMap<>();
    private final Map<String, ModelMetadata> modelMetadata = new ConcurrentHashMap<>();
    
    /**
     * Register a new model with the manager
     */
    public <I, O> void registerModel(NeuralModel<I, O> model) {
        String modelId = model.getModelId();
        log.info("Registering model: {} (type: {})", modelId, model.getModelType());
        
        models.put(modelId, model);
        modelMetadata.put(modelId, model.getMetadata());
        
        log.info("Model {} registered successfully", modelId);
    }
    
    /**
     * Unregister a model from the manager
     */
    public void unregisterModel(String modelId) {
        log.info("Unregistering model: {}", modelId);
        
        NeuralModel<?, ?> model = models.remove(modelId);
        if (model != null) {
            try {
                model.cleanup();
                log.info("Model {} unregistered and cleaned up successfully", modelId);
            } catch (Exception e) {
                log.error("Error cleaning up model {}: {}", modelId, e.getMessage(), e);
            }
        }
        
        modelMetadata.remove(modelId);
    }
    
    /**
     * Get a model by ID
     */
    @SuppressWarnings("unchecked")
    public <I, O> Optional<NeuralModel<I, O>> getModel(String modelId) {
        return Optional.ofNullable((NeuralModel<I, O>) models.get(modelId));
    }
    
    /**
     * Get models by type
     */
    public List<NeuralModel<?, ?>> getModelsByType(String modelType) {
        return models.values().stream()
                .filter(model -> modelType.equals(model.getModelType()))
                .collect(Collectors.toList());
    }
    
    /**
     * Get all registered model IDs
     */
    public List<String> getAllModelIds() {
        return List.copyOf(models.keySet());
    }
    
    /**
     * Get metadata for a specific model
     */
    public Optional<ModelMetadata> getModelMetadata(String modelId) {
        return Optional.ofNullable(modelMetadata.get(modelId));
    }
    
    /**
     * Get metadata for all models
     */
    public Map<String, ModelMetadata> getAllModelMetadata() {
        return Map.copyOf(modelMetadata);
    }
    
    /**
     * Check if a model is ready for inference
     */
    public boolean isModelReady(String modelId) {
        return getModel(modelId)
                .map(NeuralModel::isReady)
                .orElse(false);
    }
    
    /**
     * Perform inference with automatic model selection
     */
    public <I, O> CompletableFuture<O> performInference(String modelId, I input) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<NeuralModel<I, O>> modelOpt = getModel(modelId);
            if (modelOpt.isEmpty()) {
                throw new RuntimeException("Model not found: " + modelId);
            }
            
            NeuralModel<I, O> model = modelOpt.get();
            if (!model.isReady()) {
                throw new RuntimeException("Model not ready: " + modelId);
            }
            
            long startTime = System.currentTimeMillis();
            try {
                O result = model.predict(input);
                long endTime = System.currentTimeMillis();
                double inferenceTime = endTime - startTime;
                
                // Update statistics
                ModelMetadata metadata = modelMetadata.get(modelId);
                if (metadata != null) {
                    metadata.updateInferenceStats(true, inferenceTime);
                }
                
                log.debug("Inference completed for model {} in {}ms", modelId, inferenceTime);
                return result;
                
            } catch (Exception e) {
                long endTime = System.currentTimeMillis();
                double inferenceTime = endTime - startTime;
                
                // Update statistics
                ModelMetadata metadata = modelMetadata.get(modelId);
                if (metadata != null) {
                    metadata.updateInferenceStats(false, inferenceTime);
                }
                
                log.error("Inference failed for model {}: {}", modelId, e.getMessage(), e);
                throw new RuntimeException("Inference failed for model " + modelId, e);
            }
        });
    }
    
    /**
     * Perform batch inference
     */
    public <I, O> CompletableFuture<List<O>> performBatchInference(String modelId, List<I> inputs) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<NeuralModel<I, O>> modelOpt = getModel(modelId);
            if (modelOpt.isEmpty()) {
                throw new RuntimeException("Model not found: " + modelId);
            }
            
            NeuralModel<I, O> model = modelOpt.get();
            if (!model.isReady()) {
                throw new RuntimeException("Model not ready: " + modelId);
            }
            
            long startTime = System.currentTimeMillis();
            try {
                List<O> results = model.predictBatch(inputs);
                long endTime = System.currentTimeMillis();
                double inferenceTime = endTime - startTime;
                
                // Update statistics for batch
                ModelMetadata metadata = modelMetadata.get(modelId);
                if (metadata != null) {
                    metadata.updateInferenceStats(true, inferenceTime / inputs.size());
                }
                
                log.debug("Batch inference completed for model {} with {} inputs in {}ms", 
                         modelId, inputs.size(), inferenceTime);
                return results;
                
            } catch (Exception e) {
                long endTime = System.currentTimeMillis();
                double inferenceTime = endTime - startTime;
                
                // Update statistics
                ModelMetadata metadata = modelMetadata.get(modelId);
                if (metadata != null) {
                    metadata.updateInferenceStats(false, inferenceTime);
                }
                
                log.error("Batch inference failed for model {}: {}", modelId, e.getMessage(), e);
                throw new RuntimeException("Batch inference failed for model " + modelId, e);
            }
        });
    }
    
    /**
     * Get system health status
     */
    public ModelSystemHealth getSystemHealth() {
        int totalModels = models.size();
        int readyModels = (int) models.values().stream()
                .mapToInt(model -> model.isReady() ? 1 : 0)
                .sum();
        
        long totalInferences = modelMetadata.values().stream()
                .mapToLong(metadata -> metadata.getTotalInferences() != null ? metadata.getTotalInferences() : 0)
                .sum();
        
        double averageSuccessRate = modelMetadata.values().stream()
                .mapToDouble(ModelMetadata::getSuccessRate)
                .average()
                .orElse(0.0);
        
        return ModelSystemHealth.builder()
                .totalModels(totalModels)
                .readyModels(readyModels)
                .totalInferences(totalInferences)
                .averageSuccessRate(averageSuccessRate)
                .build();
    }
    
    /**
     * Cleanup all models
     */
    public void cleanup() {
        log.info("Cleaning up all models...");
        models.keySet().forEach(this::unregisterModel);
        log.info("All models cleaned up");
    }
}

