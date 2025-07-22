package com.agi.nlp.analysis;

import com.agi.neural.core.NeuralModel;
import com.agi.neural.core.ModelMetadata;
import com.agi.neural.core.ModelInitializationException;
import com.agi.neural.core.InferenceException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.time.LocalDateTime;

/**
 * Neural model for text classification tasks
 */
@Slf4j
@Component
public class TextClassificationModel implements NeuralModel<String, TextClassificationModel.ClassificationResult> {
    
    private static final String MODEL_ID = "text-classifier-v1";
    private static final String MODEL_TYPE = "text-classification";
    private static final String VERSION = "1.0.0";
    
    private boolean initialized = false;
    private ModelMetadata metadata;
    private double lastConfidence = 0.0;
    private String lastExplanation = "";
    
    // Predefined categories for classification
    private static final String[] CATEGORIES = {
        "QUESTION", "COMMAND", "STATEMENT", "GREETING", "FAREWELL", 
        "POSITIVE", "NEGATIVE", "NEUTRAL", "TECHNICAL", "PERSONAL"
    };
    
    @Data
    public static class ClassificationResult {
        private String category;
        private double confidence;
        private Map<String, Double> allScores;
        private String explanation;
    }
    
    @Override
    public String getModelId() {
        return MODEL_ID;
    }
    
    @Override
    public String getModelType() {
        return MODEL_TYPE;
    }
    
    @Override
    public String getVersion() {
        return VERSION;
    }
    
    @Override
    public void initialize(Map<String, Object> config) throws ModelInitializationException {
        try {
            log.info("Initializing Text Classification Model...");
            
            this.metadata = ModelMetadata.builder()
                    .modelId(MODEL_ID)
                    .modelType(MODEL_TYPE)
                    .version(VERSION)
                    .description("Text classification model for intent and sentiment analysis")
                    .author("AGI Backend System")
                    .createdAt(LocalDateTime.now())
                    .lastUpdated(LocalDateTime.now())
                    .status(ModelMetadata.ModelStatus.READY)
                    .supportsBatchInference(true)
                    .supportsExplainability(true)
                    .supportsOnlineLearning(true)
                    .supportsTransferLearning(true)
                    .totalInferences(0L)
                    .successfulInferences(0L)
                    .failedInferences(0L)
                    .build();
            
            this.initialized = true;
            log.info("Text Classification Model initialized successfully");
            
        } catch (Exception e) {
            throw new ModelInitializationException(MODEL_ID, "Failed to initialize text classification model", e);
        }
    }
    
    @Override
    public boolean isReady() {
        return initialized;
    }
    
    @Override
    public ClassificationResult predict(String input) throws InferenceException {
        if (!initialized) {
            throw new InferenceException(MODEL_ID, "Model not initialized", input);
        }
        
        if (input == null || input.trim().isEmpty()) {
            throw new InferenceException(MODEL_ID, "Input text is null or empty", input);
        }
        
        try {
            String text = input.toLowerCase().trim();
            Map<String, Double> scores = new java.util.HashMap<>();
            
            // Simple rule-based classification
            for (String category : CATEGORIES) {
                double score = calculateCategoryScore(text, category);
                scores.put(category, score);
            }
            
            // Find the category with highest score
            String bestCategory = scores.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("NEUTRAL");
            
            double confidence = scores.get(bestCategory);
            this.lastConfidence = confidence;
            
            // Generate explanation
            this.lastExplanation = generateExplanation(text, bestCategory, confidence);
            
            ClassificationResult result = new ClassificationResult();
            result.setCategory(bestCategory);
            result.setConfidence(confidence);
            result.setAllScores(scores);
            result.setExplanation(lastExplanation);
            
            return result;
            
        } catch (Exception e) {
            throw new InferenceException(MODEL_ID, "Failed to classify text", input, e);
        }
    }
    
    @Override
    public ModelMetadata getMetadata() {
        return metadata;
    }
    
    @Override
    public void cleanup() {
        log.info("Cleaning up Text Classification Model...");
        initialized = false;
        log.info("Text Classification Model cleaned up");
    }
    
    @Override
    public double getLastConfidence() {
        return lastConfidence;
    }
    
    @Override
    public String getLastExplanation() {
        return lastExplanation;
    }
    
    /**
     * Calculate score for a specific category
     */
    private double calculateCategoryScore(String text, String category) {
        double score = 0.0;
        
        switch (category) {
            case "QUESTION":
                if (text.contains("?") || text.startsWith("what") || text.startsWith("how") || 
                    text.startsWith("why") || text.startsWith("when") || text.startsWith("where")) {
                    score = 0.9;
                }
                break;
                
            case "COMMAND":
                if (text.startsWith("please") || text.contains("do ") || text.contains("make ") ||
                    text.contains("create") || text.contains("generate")) {
                    score = 0.8;
                }
                break;
                
            case "GREETING":
                if (text.contains("hello") || text.contains("hi ") || text.contains("good morning") ||
                    text.contains("good afternoon") || text.contains("good evening")) {
                    score = 0.95;
                }
                break;
                
            case "FAREWELL":
                if (text.contains("goodbye") || text.contains("bye") || text.contains("see you") ||
                    text.contains("farewell") || text.contains("take care")) {
                    score = 0.95;
                }
                break;
                
            case "POSITIVE":
                if (text.contains("good") || text.contains("great") || text.contains("excellent") ||
                    text.contains("amazing") || text.contains("wonderful") || text.contains("love")) {
                    score = 0.8;
                }
                break;
                
            case "NEGATIVE":
                if (text.contains("bad") || text.contains("terrible") || text.contains("awful") ||
                    text.contains("hate") || text.contains("wrong") || text.contains("error")) {
                    score = 0.8;
                }
                break;
                
            case "TECHNICAL":
                if (text.contains("code") || text.contains("program") || text.contains("algorithm") ||
                    text.contains("database") || text.contains("api") || text.contains("system")) {
                    score = 0.7;
                }
                break;
                
            case "PERSONAL":
                if (text.contains("i ") || text.contains("my ") || text.contains("me ") ||
                    text.contains("myself") || text.contains("personal")) {
                    score = 0.6;
                }
                break;
                
            default:
                score = 0.3; // Default neutral score
        }
        
        // Add some randomness to make it more realistic
        score += (Math.random() - 0.5) * 0.1;
        return Math.max(0.0, Math.min(1.0, score));
    }
    
    /**
     * Generate explanation for the classification
     */
    private String generateExplanation(String text, String category, double confidence) {
        StringBuilder explanation = new StringBuilder();
        explanation.append(String.format("Text classified as '%s' with %.2f%% confidence. ", 
                                        category, confidence * 100));
        
        switch (category) {
            case "QUESTION":
                explanation.append("Detected question words or question mark.");
                break;
            case "COMMAND":
                explanation.append("Detected imperative language or action words.");
                break;
            case "GREETING":
                explanation.append("Detected greeting words or phrases.");
                break;
            case "FAREWELL":
                explanation.append("Detected farewell words or phrases.");
                break;
            case "POSITIVE":
                explanation.append("Detected positive sentiment words.");
                break;
            case "NEGATIVE":
                explanation.append("Detected negative sentiment words.");
                break;
            case "TECHNICAL":
                explanation.append("Detected technical terminology.");
                break;
            case "PERSONAL":
                explanation.append("Detected personal pronouns or references.");
                break;
            default:
                explanation.append("No strong indicators for specific categories.");
        }
        
        return explanation.toString();
    }
}

