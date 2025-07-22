package com.agi.planning.engine;

import com.agi.neural.core.NeuralModel;
import com.agi.neural.core.ModelManager;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Neural network-powered planning engine for intelligent task planning and optimization
 */
@Slf4j
@Component
public class NeuralPlanningEngine {
    
    @Autowired
    private ModelManager modelManager;
    
    private final Map<String, PlanningSession> planningSessions = new ConcurrentHashMap<>();
    private final Map<String, PlanTemplate> planTemplates = new ConcurrentHashMap<>();
    
    @Data
    public static class PlanningSession {
        private String sessionId;
        private String userId;
        private String goalDescription;
        private LocalDateTime startTime;
        private LocalDateTime lastUpdate;
        private List<PlanStep> generatedPlan;
        private Map<String, Object> constraints;
        private Map<String, Object> resources;
        private SessionStatus status;
        private double confidenceScore;
        private int optimizationIterations;
        private PlanningStrategy strategy;
        
        public PlanningSession(String userId, String goalDescription) {
            this.sessionId = UUID.randomUUID().toString();
            this.userId = userId;
            this.goalDescription = goalDescription;
            this.startTime = LocalDateTime.now();
            this.lastUpdate = LocalDateTime.now();
            this.generatedPlan = new ArrayList<>();
            this.constraints = new HashMap<>();
            this.resources = new HashMap<>();
            this.status = SessionStatus.PLANNING;
            this.confidenceScore = 0.0;
            this.optimizationIterations = 0;
        }
    }
    
    @Data
    public static class PlanStep {
        private String stepId;
        private String stepName;
        private String description;
        private List<String> prerequisites;
        private List<String> outputs;
        private Map<String, Object> parameters;
        private StepType type;
        private Priority priority;
        private LocalDateTime estimatedStart;
        private LocalDateTime estimatedEnd;
        private long estimatedDurationMinutes;
        private double complexity;
        private double confidence;
        private StepStatus status;
        private List<String> dependencies;
        
        public PlanStep(String stepName, String description) {
            this.stepId = UUID.randomUUID().toString();
            this.stepName = stepName;
            this.description = description;
            this.prerequisites = new ArrayList<>();
            this.outputs = new ArrayList<>();
            this.parameters = new HashMap<>();
            this.priority = Priority.MEDIUM;
            this.complexity = 0.5;
            this.confidence = 0.0;
            this.status = StepStatus.PLANNED;
            this.dependencies = new ArrayList<>();
        }
    }
    
    @Data
    public static class PlanTemplate {
        private String templateId;
        private String templateName;
        private String description;
        private List<PlanStep> templateSteps;
        private Map<String, Object> defaultConstraints;
        private PlanCategory category;
        private double successRate;
        private int usageCount;
        private LocalDateTime createdAt;
        private LocalDateTime lastUsed;
        
        public PlanTemplate(String templateName, String description, PlanCategory category) {
            this.templateId = UUID.randomUUID().toString();
            this.templateName = templateName;
            this.description = description;
            this.category = category;
            this.templateSteps = new ArrayList<>();
            this.defaultConstraints = new HashMap<>();
            this.successRate = 0.0;
            this.usageCount = 0;
            this.createdAt = LocalDateTime.now();
        }
    }
    
    public enum SessionStatus {
        PLANNING, OPTIMIZING, READY, EXECUTING, COMPLETED, FAILED, CANCELLED
    }
    
    public enum StepType {
        PREPARATION, EXECUTION, VALIDATION, COMMUNICATION, ANALYSIS, DECISION
    }
    
    public enum Priority {
        LOW(1), MEDIUM(2), HIGH(3), CRITICAL(4);
        
        private final int value;
        
        Priority(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
    }
    
    public enum StepStatus {
        PLANNED, READY, IN_PROGRESS, COMPLETED, FAILED, SKIPPED, BLOCKED
    }
    
    public enum PlanCategory {
        PROJECT_MANAGEMENT, PROBLEM_SOLVING, LEARNING, RESEARCH, 
        DEVELOPMENT, ANALYSIS, COMMUNICATION, CREATIVE
    }
    
    public enum PlanningStrategy {
        FORWARD_CHAINING, BACKWARD_CHAINING, HIERARCHICAL, 
        CONSTRAINT_BASED, NEURAL_OPTIMIZATION, HYBRID
    }
    
    /**
     * Start planning session
     */
    public PlanningSession startPlanning(String userId, String goalDescription, 
                                       Map<String, Object> constraints,
                                       Map<String, Object> resources) {
        
        PlanningSession session = new PlanningSession(userId, goalDescription);
        
        if (constraints != null) {
            session.getConstraints().putAll(constraints);
        }
        if (resources != null) {
            session.getResources().putAll(resources);
        }
        
        // Determine planning strategy using neural network
        session.setStrategy(selectPlanningStrategy(goalDescription, constraints));
        
        planningSessions.put(session.getSessionId(), session);
        
        log.info("Started planning session: {} for goal: {}", session.getSessionId(), goalDescription);
        
        return session;
    }
    
    /**
     * Generate plan using neural network
     */
    public CompletableFuture<List<PlanStep>> generatePlan(String sessionId) {
        return CompletableFuture.supplyAsync(() -> {
            PlanningSession session = planningSessions.get(sessionId);
            if (session == null) {
                throw new IllegalArgumentException("Planning session not found: " + sessionId);
            }
            
            try {
                session.setStatus(SessionStatus.PLANNING);
                
                // Use neural network to generate initial plan
                List<PlanStep> initialPlan = generatePlanWithNeural(session);
                
                // Optimize plan using neural optimization
                List<PlanStep> optimizedPlan = optimizePlanWithNeural(session, initialPlan);
                
                // Validate plan feasibility
                validatePlanFeasibility(session, optimizedPlan);
                
                session.setGeneratedPlan(optimizedPlan);
                session.setStatus(SessionStatus.READY);
                session.setLastUpdate(LocalDateTime.now());
                
                // Calculate overall confidence
                calculatePlanConfidence(session);
                
                log.info("Generated plan with {} steps for session: {}", 
                        optimizedPlan.size(), sessionId);
                
                return optimizedPlan;
                
            } catch (Exception e) {
                session.setStatus(SessionStatus.FAILED);
                log.error("Plan generation failed for session {}: {}", sessionId, e.getMessage(), e);
                throw new RuntimeException("Plan generation failed", e);
            }
        });
    }
    
    /**
     * Generate plan using neural network
     */
    private List<PlanStep> generatePlanWithNeural(PlanningSession session) {
        try {
            // Prepare neural input
            Map<String, Object> neuralInput = prepareNeuralInput(session);
            
            String modelId = "plan_generator_model";
            if (modelManager.isModelReady(modelId)) {
                Map<String, Object> prediction = modelManager.predict(modelId, neuralInput);
                return extractPlanFromPrediction(prediction, session);
            } else {
                // Fallback to template-based planning
                return generatePlanFromTemplate(session);
            }
            
        } catch (Exception e) {
            log.warn("Neural plan generation failed, using template fallback: {}", e.getMessage());
            return generatePlanFromTemplate(session);
        }
    }
    
    /**
     * Prepare neural input for planning
     */
    private Map<String, Object> prepareNeuralInput(PlanningSession session) {
        Map<String, Object> input = new HashMap<>();
        
        // Goal analysis
        String goal = session.getGoalDescription();
        input.put("goal_description", goal);
        input.put("goal_length", goal.length());
        input.put("goal_complexity", analyzeGoalComplexity(goal));
        
        // Constraint analysis
        input.put("constraint_count", session.getConstraints().size());
        input.put("has_time_constraint", session.getConstraints().containsKey("deadline"));
        input.put("has_budget_constraint", session.getConstraints().containsKey("budget"));
        input.put("has_resource_constraint", session.getConstraints().containsKey("resources"));
        
        // Resource analysis
        input.put("resource_count", session.getResources().size());
        input.put("resource_availability", calculateResourceAvailability(session.getResources()));
        
        // Context features
        input.put("user_id", session.getUserId());
        input.put("session_time", session.getStartTime().getHour());
        
        return input;
    }
    
    /**
     * Extract plan from neural prediction
     */
    private List<PlanStep> extractPlanFromPrediction(Map<String, Object> prediction, 
                                                    PlanningSession session) {
        List<PlanStep> steps = new ArrayList<>();
        
        // Extract number of steps
        int stepCount = ((Number) prediction.getOrDefault("step_count", 5)).intValue();
        
        // Generate steps based on prediction
        for (int i = 0; i < stepCount; i++) {
            String stepKey = "step_" + i;
            
            if (prediction.containsKey(stepKey + "_name")) {
                String stepName = (String) prediction.get(stepKey + "_name");
                String stepDesc = (String) prediction.getOrDefault(stepKey + "_description", 
                        "Generated step " + (i + 1));
                
                PlanStep step = new PlanStep(stepName, stepDesc);
                
                // Extract step properties from prediction
                if (prediction.containsKey(stepKey + "_priority")) {
                    int priorityValue = ((Number) prediction.get(stepKey + "_priority")).intValue();
                    step.setPriority(Priority.values()[Math.min(priorityValue, Priority.values().length - 1)]);
                }
                
                if (prediction.containsKey(stepKey + "_duration")) {
                    long duration = ((Number) prediction.get(stepKey + "_duration")).longValue();
                    step.setEstimatedDurationMinutes(duration);
                }
                
                if (prediction.containsKey(stepKey + "_complexity")) {
                    double complexity = ((Number) prediction.get(stepKey + "_complexity")).doubleValue();
                    step.setComplexity(Math.max(0.0, Math.min(1.0, complexity)));
                }
                
                steps.add(step);
            }
        }
        
        // If no steps extracted, generate default steps
        if (steps.isEmpty()) {
            steps = generateDefaultSteps(session);
        }
        
        return steps;
    }
    
    /**
     * Generate plan from template
     */
    private List<PlanStep> generatePlanFromTemplate(PlanningSession session) {
        // Find best matching template
        PlanTemplate template = findBestTemplate(session.getGoalDescription());
        
        if (template != null) {
            List<PlanStep> steps = new ArrayList<>();
            for (PlanStep templateStep : template.getTemplateSteps()) {
                PlanStep step = new PlanStep(templateStep.getStepName(), templateStep.getDescription());
                step.setType(templateStep.getType());
                step.setPriority(templateStep.getPriority());
                step.setComplexity(templateStep.getComplexity());
                step.setEstimatedDurationMinutes(templateStep.getEstimatedDurationMinutes());
                steps.add(step);
            }
            return steps;
        } else {
            return generateDefaultSteps(session);
        }
    }
    
    /**
     * Generate default steps
     */
    private List<PlanStep> generateDefaultSteps(PlanningSession session) {
        List<PlanStep> steps = new ArrayList<>();
        
        steps.add(new PlanStep("Analysis", "Analyze the goal and requirements"));
        steps.add(new PlanStep("Planning", "Create detailed action plan"));
        steps.add(new PlanStep("Preparation", "Gather necessary resources"));
        steps.add(new PlanStep("Execution", "Execute the main tasks"));
        steps.add(new PlanStep("Validation", "Validate results and outcomes"));
        
        // Set default properties
        for (int i = 0; i < steps.size(); i++) {
            PlanStep step = steps.get(i);
            step.setPriority(i < 2 ? Priority.HIGH : Priority.MEDIUM);
            step.setComplexity(0.5 + (i * 0.1));
            step.setEstimatedDurationMinutes(30 + (i * 15));
            step.setConfidence(0.7);
        }
        
        return steps;
    }
    
    /**
     * Optimize plan using neural network
     */
    private List<PlanStep> optimizePlanWithNeural(PlanningSession session, List<PlanStep> initialPlan) {
        try {
            // Prepare optimization input
            Map<String, Object> optimizationInput = new HashMap<>();
            optimizationInput.put("session_id", session.getSessionId());
            optimizationInput.put("step_count", initialPlan.size());
            optimizationInput.put("total_complexity", calculateTotalComplexity(initialPlan));
            optimizationInput.put("total_duration", calculateTotalDuration(initialPlan));
            optimizationInput.putAll(session.getConstraints());
            
            String modelId = "plan_optimizer_model";
            if (modelManager.isModelReady(modelId)) {
                Map<String, Object> optimization = modelManager.predict(modelId, optimizationInput);
                return applyOptimization(initialPlan, optimization);
            }
            
        } catch (Exception e) {
            log.warn("Neural plan optimization failed: {}", e.getMessage());
        }
        
        // Apply rule-based optimization
        return optimizePlanRuleBased(initialPlan, session);
    }
    
    /**
     * Apply rule-based optimization
     */
    private List<PlanStep> optimizePlanRuleBased(List<PlanStep> plan, PlanningSession session) {
        List<PlanStep> optimized = new ArrayList<>(plan);
        
        // Sort by priority and dependencies
        optimized.sort((a, b) -> {
            int priorityCompare = Integer.compare(b.getPriority().getValue(), a.getPriority().getValue());
            if (priorityCompare != 0) return priorityCompare;
            return Integer.compare(a.getDependencies().size(), b.getDependencies().size());
        });
        
        // Adjust timing based on constraints
        if (session.getConstraints().containsKey("deadline")) {
            adjustTimingForDeadline(optimized, session);
        }
        
        session.setOptimizationIterations(session.getOptimizationIterations() + 1);
        
        return optimized;
    }
    
    /**
     * Apply neural optimization
     */
    private List<PlanStep> applyOptimization(List<PlanStep> plan, Map<String, Object> optimization) {
        List<PlanStep> optimized = new ArrayList<>(plan);
        
        // Apply optimization suggestions
        if (optimization.containsKey("reorder_steps")) {
            boolean shouldReorder = (Boolean) optimization.get("reorder_steps");
            if (shouldReorder) {
                // Reorder based on optimization
                optimized.sort((a, b) -> Double.compare(b.getComplexity(), a.getComplexity()));
            }
        }
        
        if (optimization.containsKey("duration_adjustment")) {
            double adjustment = ((Number) optimization.get("duration_adjustment")).doubleValue();
            for (PlanStep step : optimized) {
                long newDuration = Math.round(step.getEstimatedDurationMinutes() * adjustment);
                step.setEstimatedDurationMinutes(Math.max(5, newDuration));
            }
        }
        
        return optimized;
    }
    
    /**
     * Validate plan feasibility
     */
    private void validatePlanFeasibility(PlanningSession session, List<PlanStep> plan) {
        // Check resource constraints
        double totalComplexity = calculateTotalComplexity(plan);
        long totalDuration = calculateTotalDuration(plan);
        
        // Validate against constraints
        if (session.getConstraints().containsKey("max_duration")) {
            long maxDuration = ((Number) session.getConstraints().get("max_duration")).longValue();
            if (totalDuration > maxDuration) {
                log.warn("Plan duration ({} min) exceeds constraint ({} min)", totalDuration, maxDuration);
            }
        }
        
        if (session.getConstraints().containsKey("max_complexity")) {
            double maxComplexity = ((Number) session.getConstraints().get("max_complexity")).doubleValue();
            if (totalComplexity > maxComplexity) {
                log.warn("Plan complexity ({}) exceeds constraint ({})", totalComplexity, maxComplexity);
            }
        }
        
        // Set step timing
        setStepTiming(plan);
    }
    
    /**
     * Set step timing
     */
    private void setStepTiming(List<PlanStep> plan) {
        LocalDateTime currentTime = LocalDateTime.now();
        
        for (PlanStep step : plan) {
            step.setEstimatedStart(currentTime);
            step.setEstimatedEnd(currentTime.plusMinutes(step.getEstimatedDurationMinutes()));
            currentTime = step.getEstimatedEnd();
        }
    }
    
    /**
     * Calculate plan confidence
     */
    private void calculatePlanConfidence(PlanningSession session) {
        List<PlanStep> plan = session.getGeneratedPlan();
        if (plan.isEmpty()) {
            session.setConfidenceScore(0.0);
            return;
        }
        
        double totalConfidence = plan.stream()
                .mapToDouble(PlanStep::getConfidence)
                .sum();
        
        double avgConfidence = totalConfidence / plan.size();
        
        // Adjust based on constraints satisfaction
        double constraintSatisfaction = calculateConstraintSatisfaction(session, plan);
        
        session.setConfidenceScore(avgConfidence * constraintSatisfaction);
    }
    
    /**
     * Helper methods
     */
    private PlanningStrategy selectPlanningStrategy(String goalDescription, Map<String, Object> constraints) {
        // Simple rule-based strategy selection
        if (constraints != null && constraints.containsKey("deadline")) {
            return PlanningStrategy.CONSTRAINT_BASED;
        }
        if (goalDescription.toLowerCase().contains("complex") || goalDescription.toLowerCase().contains("project")) {
            return PlanningStrategy.HIERARCHICAL;
        }
        return PlanningStrategy.NEURAL_OPTIMIZATION;
    }
    
    private double analyzeGoalComplexity(String goal) {
        // Simple complexity analysis
        double complexity = 0.3; // Base complexity
        complexity += Math.min(0.3, goal.length() / 1000.0); // Length factor
        complexity += goal.split(" ").length * 0.01; // Word count factor
        return Math.min(1.0, complexity);
    }
    
    private double calculateResourceAvailability(Map<String, Object> resources) {
        if (resources.isEmpty()) return 0.5; // Default availability
        
        return resources.values().stream()
                .mapToDouble(resource -> resource instanceof Number ? 
                        Math.min(1.0, ((Number) resource).doubleValue()) : 0.5)
                .average()
                .orElse(0.5);
    }
    
    private double calculateTotalComplexity(List<PlanStep> plan) {
        return plan.stream().mapToDouble(PlanStep::getComplexity).sum();
    }
    
    private long calculateTotalDuration(List<PlanStep> plan) {
        return plan.stream().mapToLong(PlanStep::getEstimatedDurationMinutes).sum();
    }
    
    private void adjustTimingForDeadline(List<PlanStep> plan, PlanningSession session) {
        // Adjust step durations to meet deadline
        if (session.getConstraints().containsKey("deadline")) {
            // Implementation for deadline adjustment
            log.debug("Adjusting plan timing for deadline constraint");
        }
    }
    
    private double calculateConstraintSatisfaction(PlanningSession session, List<PlanStep> plan) {
        // Calculate how well the plan satisfies constraints
        double satisfaction = 1.0;
        
        // Check duration constraint
        if (session.getConstraints().containsKey("max_duration")) {
            long maxDuration = ((Number) session.getConstraints().get("max_duration")).longValue();
            long actualDuration = calculateTotalDuration(plan);
            if (actualDuration > maxDuration) {
                satisfaction *= 0.8; // Penalty for exceeding duration
            }
        }
        
        return satisfaction;
    }
    
    private PlanTemplate findBestTemplate(String goalDescription) {
        // Find template with highest similarity to goal
        return planTemplates.values().stream()
                .max((t1, t2) -> Double.compare(
                        calculateTemplateSimilarity(t1, goalDescription),
                        calculateTemplateSimilarity(t2, goalDescription)))
                .orElse(null);
    }
    
    private double calculateTemplateSimilarity(PlanTemplate template, String goalDescription) {
        // Simple keyword-based similarity
        String[] goalWords = goalDescription.toLowerCase().split("\\s+");
        String[] templateWords = template.getDescription().toLowerCase().split("\\s+");
        
        Set<String> goalSet = new HashSet<>(Arrays.asList(goalWords));
        Set<String> templateSet = new HashSet<>(Arrays.asList(templateWords));
        
        Set<String> intersection = new HashSet<>(goalSet);
        intersection.retainAll(templateSet);
        
        Set<String> union = new HashSet<>(goalSet);
        union.addAll(templateSet);
        
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }
    
    /**
     * Get planning session
     */
    public Optional<PlanningSession> getPlanningSession(String sessionId) {
        return Optional.ofNullable(planningSessions.get(sessionId));
    }
    
    /**
     * Get system statistics
     */
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("active_sessions", planningSessions.size());
        stats.put("available_templates", planTemplates.size());
        
        // Calculate average confidence
        double avgConfidence = planningSessions.values().stream()
                .mapToDouble(PlanningSession::getConfidenceScore)
                .average()
                .orElse(0.0);
        stats.put("average_confidence", avgConfidence);
        
        return stats;
    }
}

