package com.agi.learning.adaptive;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Gradient descent based adaptive learning strategy
 */
@Slf4j
@Component
public class GradientDescentStrategy implements LearningStrategy {
    
    private static final String STRATEGY_NAME = "gradient_descent";
    private static final double DEFAULT_LEARNING_RATE = 0.01;
    
    @Override
    public void initialize(AdaptiveLearningEngine.AdaptiveLearningSession session) {
        log.info("Initializing Gradient Descent strategy for session: {}", session.getSessionId());
        
        // Set default learning rate if not specified
        if (session.getLearningRate() <= 0) {
            session.setLearningRate(DEFAULT_LEARNING_RATE);
        }
        
        // Initialize strategy-specific data
        session.getLearningData().put("momentum", 0.9);
        session.getLearningData().put("decay", 0.0001);
        session.getLearningData().put("gradientHistory", new java.util.ArrayList<>());
        
        log.debug("Gradient Descent strategy initialized with learning rate: {}", session.getLearningRate());
    }
    
    @Override
    public boolean adapt(AdaptiveLearningEngine.AdaptiveLearningSession session, 
                        AdaptiveLearningEngine.AdaptationEvent event) {
        
        try {
            log.debug("Applying gradient descent adaptation for session: {}", session.getSessionId());
            
            // Simulate gradient calculation and parameter update
            double[] gradients = calculateGradients(event.getInputData(), event.getOutputData());
            
            // Apply momentum if available
            double momentum = (Double) session.getLearningData().getOrDefault("momentum", 0.9);
            gradients = applyMomentum(gradients, session, momentum);
            
            // Update model parameters (simulated)
            boolean success = updateModelParameters(session, gradients);
            
            if (success) {
                event.setConfidence(calculateConfidence(gradients));
                event.setDescription("Gradient descent parameter update applied successfully");
                
                // Store gradient history for momentum calculation
                @SuppressWarnings("unchecked")
                List<double[]> gradientHistory = (List<double[]>) session.getLearningData()
                        .computeIfAbsent("gradientHistory", k -> new java.util.ArrayList<>());
                gradientHistory.add(gradients);
                
                // Keep only recent gradients
                if (gradientHistory.size() > 10) {
                    gradientHistory.remove(0);
                }
                
                log.debug("Gradient descent adaptation successful for session: {}", session.getSessionId());
                return true;
            } else {
                event.setDescription("Gradient descent parameter update failed");
                log.warn("Gradient descent adaptation failed for session: {}", session.getSessionId());
                return false;
            }
            
        } catch (Exception e) {
            log.error("Error in gradient descent adaptation for session {}: {}", 
                     session.getSessionId(), e.getMessage(), e);
            event.setDescription("Gradient descent adaptation error: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public String getStrategyName() {
        return STRATEGY_NAME;
    }
    
    @Override
    public String getDescription() {
        return "Gradient descent based adaptive learning with momentum support";
    }
    
    @Override
    public boolean supportsMode(AdaptiveLearningEngine.AdaptationMode mode) {
        // Gradient descent works well with most adaptation modes
        return mode == AdaptiveLearningEngine.AdaptationMode.ONLINE_LEARNING ||
               mode == AdaptiveLearningEngine.AdaptationMode.BATCH_ADAPTATION ||
               mode == AdaptiveLearningEngine.AdaptationMode.INCREMENTAL_LEARNING ||
               mode == AdaptiveLearningEngine.AdaptationMode.PERFORMANCE_BASED;
    }
    
    @Override
    public double getRecommendedLearningRate() {
        return DEFAULT_LEARNING_RATE;
    }
    
    @Override
    public void cleanup(AdaptiveLearningEngine.AdaptiveLearningSession session) {
        log.info("Cleaning up Gradient Descent strategy for session: {}", session.getSessionId());
        
        // Clear gradient history to free memory
        session.getLearningData().remove("gradientHistory");
        
        log.debug("Gradient Descent strategy cleanup completed");
    }
    
    /**
     * Calculate gradients based on input and expected output
     */
    private double[] calculateGradients(java.util.Map<String, Object> inputData, 
                                       java.util.Map<String, Object> outputData) {
        
        // Simplified gradient calculation
        // In a real implementation, this would compute actual gradients
        int parameterCount = 10; // Assume 10 parameters for simplicity
        double[] gradients = new double[parameterCount];
        
        // Simulate gradient calculation with some randomness
        for (int i = 0; i < parameterCount; i++) {
            gradients[i] = (Math.random() - 0.5) * 0.1; // Small random gradients
        }
        
        // Add some structure based on input/output data
        if (inputData.containsKey("error")) {
            double error = ((Number) inputData.get("error")).doubleValue();
            for (int i = 0; i < gradients.length; i++) {
                gradients[i] *= (1.0 + error); // Scale gradients by error
            }
        }
        
        return gradients;
    }
    
    /**
     * Apply momentum to gradients
     */
    private double[] applyMomentum(double[] gradients, 
                                  AdaptiveLearningEngine.AdaptiveLearningSession session,
                                  double momentum) {
        
        @SuppressWarnings("unchecked")
        List<double[]> gradientHistory = (List<double[]>) session.getLearningData()
                .get("gradientHistory");
        
        if (gradientHistory == null || gradientHistory.isEmpty()) {
            return gradients; // No history, return original gradients
        }
        
        // Get last gradient
        double[] lastGradient = gradientHistory.get(gradientHistory.size() - 1);
        
        // Apply momentum
        double[] momentumGradients = new double[gradients.length];
        for (int i = 0; i < gradients.length; i++) {
            momentumGradients[i] = momentum * lastGradient[i] + (1 - momentum) * gradients[i];
        }
        
        return momentumGradients;
    }
    
    /**
     * Update model parameters (simulated)
     */
    private boolean updateModelParameters(AdaptiveLearningEngine.AdaptiveLearningSession session,
                                        double[] gradients) {
        
        // Simulate parameter update
        double learningRate = session.getLearningRate();
        
        // Check for numerical stability
        double gradientNorm = Arrays.stream(gradients)
                .map(Math::abs)
                .sum();
        
        if (gradientNorm > 10.0) {
            log.warn("Large gradient norm detected: {}. Skipping update to maintain stability.", gradientNorm);
            return false;
        }
        
        // Simulate successful parameter update
        log.debug("Updated {} parameters with learning rate: {}", gradients.length, learningRate);
        return true;
    }
    
    /**
     * Calculate confidence based on gradient properties
     */
    private double calculateConfidence(double[] gradients) {
        // Calculate confidence based on gradient magnitude and consistency
        double gradientNorm = Arrays.stream(gradients)
                .map(g -> g * g)
                .sum();
        gradientNorm = Math.sqrt(gradientNorm);
        
        // Higher confidence for moderate gradient norms
        if (gradientNorm < 0.01) {
            return 0.3; // Very small gradients - low confidence
        } else if (gradientNorm < 0.1) {
            return 0.8; // Good gradient magnitude - high confidence
        } else if (gradientNorm < 1.0) {
            return 0.6; // Moderate gradients - medium confidence
        } else {
            return 0.2; // Very large gradients - low confidence (potential instability)
        }
    }
}

