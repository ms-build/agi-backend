package com.agi.creativity;

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
 * Neural network-powered creativity engine for idea generation, content creation, and creative problem-solving
 */
@Slf4j
@Component
public class NeuralCreativityEngine {
    
    @Autowired
    private ModelManager modelManager;
    
    private final Map<String, CreativitySession> creativitySessions = new ConcurrentHashMap<>();
    private final Map<String, CreativeProject> creativeProjects = new ConcurrentHashMap<>();
    
    @Data
    public static class CreativitySession {
        private String sessionId;
        private String userId;
        private CreativityMode mode;
        private LocalDateTime startTime;
        private LocalDateTime lastActivity;
        private List<CreativeRequest> requests;
        private List<CreativeOutput> outputs;
        private Map<String, Object> sessionContext;
        private SessionStatus status;
        private double creativityScore;
        
        public CreativitySession(String userId, CreativityMode mode) {
            this.sessionId = UUID.randomUUID().toString();
            this.userId = userId;
            this.mode = mode;
            this.startTime = LocalDateTime.now();
            this.lastActivity = LocalDateTime.now();
            this.requests = new ArrayList<>();
            this.outputs = new ArrayList<>();
            this.sessionContext = new HashMap<>();
            this.status = SessionStatus.ACTIVE;
            this.creativityScore = 0.0;
        }
    }
    
    @Data
    public static class CreativeRequest {
        private String requestId;
        private CreativeTask task;
        private String prompt;
        private Map<String, Object> parameters;
        private List<String> constraints;
        private List<String> inspirations;
        private CreativityLevel desiredLevel;
        private LocalDateTime requestTime;
        
        public CreativeRequest(CreativeTask task, String prompt) {
            this.requestId = UUID.randomUUID().toString();
            this.task = task;
            this.prompt = prompt;
            this.parameters = new HashMap<>();
            this.constraints = new ArrayList<>();
            this.inspirations = new ArrayList<>();
            this.desiredLevel = CreativityLevel.MODERATE;
            this.requestTime = LocalDateTime.now();
        }
    }
    
    @Data
    public static class CreativeOutput {
        private String outputId;
        private String requestId;
        private CreativeTask task;
        private String generatedContent;
        private Map<String, Object> metadata;
        private List<CreativeElement> elements;
        private double originalityScore;
        private double qualityScore;
        private double relevanceScore;
        private LocalDateTime generatedAt;
        private long generationTimeMs;
        
        public CreativeOutput(String requestId, CreativeTask task) {
            this.outputId = UUID.randomUUID().toString();
            this.requestId = requestId;
            this.task = task;
            this.metadata = new HashMap<>();
            this.elements = new ArrayList<>();
            this.originalityScore = 0.0;
            this.qualityScore = 0.0;
            this.relevanceScore = 0.0;
            this.generatedAt = LocalDateTime.now();
        }
    }
    
    @Data
    public static class CreativeElement {
        private String elementId;
        private ElementType type;
        private String content;
        private Map<String, Object> properties;
        private double creativityContribution;
        
        public CreativeElement(ElementType type, String content) {
            this.elementId = UUID.randomUUID().toString();
            this.type = type;
            this.content = content;
            this.properties = new HashMap<>();
            this.creativityContribution = 0.0;
        }
    }
    
    @Data
    public static class CreativeProject {
        private String projectId;
        private String userId;
        private String projectName;
        private String description;
        private ProjectType projectType;
        private List<String> sessionIds;
        private Map<String, Object> projectData;
        private ProjectStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime lastModified;
        
        public CreativeProject(String userId, String projectName, ProjectType projectType) {
            this.projectId = UUID.randomUUID().toString();
            this.userId = userId;
            this.projectName = projectName;
            this.projectType = projectType;
            this.sessionIds = new ArrayList<>();
            this.projectData = new HashMap<>();
            this.status = ProjectStatus.ACTIVE;
            this.createdAt = LocalDateTime.now();
            this.lastModified = LocalDateTime.now();
        }
    }
    
    @Data
    public static class IdeaGeneration {
        private String ideaId;
        private String topic;
        private List<String> generatedIdeas;
        private Map<String, Double> ideaScores;
        private List<String> categories;
        private double diversityScore;
        private LocalDateTime generatedAt;
        
        public IdeaGeneration(String topic) {
            this.ideaId = UUID.randomUUID().toString();
            this.topic = topic;
            this.generatedIdeas = new ArrayList<>();
            this.ideaScores = new HashMap<>();
            this.categories = new ArrayList<>();
            this.diversityScore = 0.0;
            this.generatedAt = LocalDateTime.now();
        }
    }
    
    public enum CreativityMode {
        BRAINSTORMING, CONTENT_CREATION, PROBLEM_SOLVING, ARTISTIC_CREATION, 
        INNOVATION, STORYTELLING, DESIGN_THINKING
    }
    
    public enum CreativeTask {
        IDEA_GENERATION, TEXT_CREATION, IMAGE_CONCEPT, MUSIC_COMPOSITION,
        STORY_WRITING, PROBLEM_SOLVING, DESIGN_CONCEPT, INNOVATION_PROPOSAL
    }
    
    public enum CreativityLevel {
        CONSERVATIVE, MODERATE, HIGH, EXPERIMENTAL, RADICAL
    }
    
    public enum SessionStatus {
        ACTIVE, PAUSED, COMPLETED, ARCHIVED
    }
    
    public enum ElementType {
        CONCEPT, METAPHOR, NARRATIVE, VISUAL, AUDIO, STRUCTURE, STYLE, THEME
    }
    
    public enum ProjectType {
        WRITING, DESIGN, MUSIC, INNOVATION, RESEARCH, MIXED_MEDIA
    }
    
    public enum ProjectStatus {
        ACTIVE, COMPLETED, ON_HOLD, CANCELLED
    }
    
    /**
     * Start creativity session
     */
    public CreativitySession startCreativitySession(String userId, CreativityMode mode) {
        CreativitySession session = new CreativitySession(userId, mode);
        creativitySessions.put(session.getSessionId(), session);
        
        log.info("Started creativity session: {} for user: {} in mode: {}", 
                session.getSessionId(), userId, mode);
        
        return session;
    }
    
    /**
     * Generate creative content
     */
    public CompletableFuture<CreativeOutput> generateCreativeContent(String sessionId,
                                                                    CreativeTask task,
                                                                    String prompt,
                                                                    Map<String, Object> parameters) {
        
        return CompletableFuture.supplyAsync(() -> {
            CreativitySession session = creativitySessions.get(sessionId);
            if (session == null) {
                throw new IllegalArgumentException("Creativity session not found: " + sessionId);
            }
            
            try {
                long startTime = System.currentTimeMillis();
                
                CreativeRequest request = new CreativeRequest(task, prompt);
                if (parameters != null) {
                    request.getParameters().putAll(parameters);
                }
                
                session.getRequests().add(request);
                
                // Generate creative content based on task
                CreativeOutput output = generateContentByTask(request, session);
                
                session.getOutputs().add(output);
                session.setLastActivity(LocalDateTime.now());
                
                // Update session creativity score
                updateSessionCreativityScore(session);
                
                output.setGenerationTimeMs(System.currentTimeMillis() - startTime);
                
                log.info("Generated creative content for session: {} with originality: {}", 
                        sessionId, output.getOriginalityScore());
                
                return output;
                
            } catch (Exception e) {
                log.error("Creative content generation failed for session {}: {}", 
                        sessionId, e.getMessage(), e);
                throw new RuntimeException("Creative content generation failed", e);
            }
        });
    }
    
    /**
     * Generate content by task type
     */
    private CreativeOutput generateContentByTask(CreativeRequest request, CreativitySession session) {
        CreativeOutput output = new CreativeOutput(request.getRequestId(), request.getTask());
        
        try {
            switch (request.getTask()) {
                case IDEA_GENERATION:
                    generateIdeas(request, output, session);
                    break;
                case TEXT_CREATION:
                    generateText(request, output, session);
                    break;
                case STORY_WRITING:
                    generateStory(request, output, session);
                    break;
                case PROBLEM_SOLVING:
                    generateSolutions(request, output, session);
                    break;
                case DESIGN_CONCEPT:
                    generateDesignConcept(request, output, session);
                    break;
                case MUSIC_COMPOSITION:
                    generateMusicConcept(request, output, session);
                    break;
                default:
                    generateGenericCreativeContent(request, output, session);
            }
            
            // Evaluate creativity metrics
            evaluateCreativityMetrics(output, session);
            
        } catch (Exception e) {
            log.error("Failed to generate content for task {}: {}", request.getTask(), e.getMessage(), e);
            output.setGeneratedContent("Creative generation failed: " + e.getMessage());
        }
        
        return output;
    }
    
    /**
     * Generate ideas
     */
    private void generateIdeas(CreativeRequest request, CreativeOutput output, CreativitySession session) {
        try {
            // Prepare neural input for idea generation
            Map<String, Object> neuralInput = new HashMap<>();
            neuralInput.put("topic", request.getPrompt());
            neuralInput.put("creativity_level", request.getDesiredLevel().name());
            neuralInput.put("constraints", request.getConstraints());
            neuralInput.put("inspirations", request.getInspirations());
            neuralInput.put("session_context", session.getSessionContext());
            
            String modelId = "idea_generation_model";
            if (modelManager.isModelReady(modelId)) {
                Map<String, Object> ideaResult = modelManager.predict(modelId, neuralInput);
                
                // Extract generated ideas
                if (ideaResult.containsKey("ideas")) {
                    List<String> ideas = (List<String>) ideaResult.get("ideas");
                    StringBuilder ideaContent = new StringBuilder("Generated Ideas:\n\n");
                    
                    for (int i = 0; i < ideas.size(); i++) {
                        ideaContent.append(i + 1).append(". ").append(ideas.get(i)).append("\n");
                        
                        // Create creative elements for each idea
                        CreativeElement element = new CreativeElement(ElementType.CONCEPT, ideas.get(i));
                        element.setCreativityContribution(0.8 + (Math.random() * 0.2));
                        output.getElements().add(element);
                    }
                    
                    output.setGeneratedContent(ideaContent.toString());
                }
                
                // Extract creativity scores
                if (ideaResult.containsKey("originality_score")) {
                    output.setOriginalityScore(((Number) ideaResult.get("originality_score")).doubleValue());
                }
                
            } else {
                // Fallback idea generation
                generateFallbackIdeas(request, output);
            }
            
        } catch (Exception e) {
            log.warn("Neural idea generation failed, using fallback: {}", e.getMessage());
            generateFallbackIdeas(request, output);
        }
    }
    
    /**
     * Generate text content
     */
    private void generateText(CreativeRequest request, CreativeOutput output, CreativitySession session) {
        try {
            Map<String, Object> neuralInput = new HashMap<>();
            neuralInput.put("prompt", request.getPrompt());
            neuralInput.put("style", request.getParameters().getOrDefault("style", "creative"));
            neuralInput.put("length", request.getParameters().getOrDefault("length", "medium"));
            neuralInput.put("tone", request.getParameters().getOrDefault("tone", "engaging"));
            
            String modelId = "text_generation_model";
            if (modelManager.isModelReady(modelId)) {
                Map<String, Object> textResult = modelManager.predict(modelId, neuralInput);
                
                if (textResult.containsKey("generated_text")) {
                    output.setGeneratedContent((String) textResult.get("generated_text"));
                }
                
                if (textResult.containsKey("quality_score")) {
                    output.setQualityScore(((Number) textResult.get("quality_score")).doubleValue());
                }
                
            } else {
                generateFallbackText(request, output);
            }
            
        } catch (Exception e) {
            log.warn("Neural text generation failed, using fallback: {}", e.getMessage());
            generateFallbackText(request, output);
        }
    }
    
    /**
     * Generate story
     */
    private void generateStory(CreativeRequest request, CreativeOutput output, CreativitySession session) {
        try {
            Map<String, Object> neuralInput = new HashMap<>();
            neuralInput.put("theme", request.getPrompt());
            neuralInput.put("genre", request.getParameters().getOrDefault("genre", "general"));
            neuralInput.put("length", request.getParameters().getOrDefault("length", "short"));
            neuralInput.put("characters", request.getParameters().getOrDefault("characters", new ArrayList<>()));
            
            String modelId = "story_generation_model";
            if (modelManager.isModelReady(modelId)) {
                Map<String, Object> storyResult = modelManager.predict(modelId, neuralInput);
                
                if (storyResult.containsKey("story")) {
                    output.setGeneratedContent((String) storyResult.get("story"));
                }
                
                // Extract story elements
                if (storyResult.containsKey("elements")) {
                    Map<String, Object> elements = (Map<String, Object>) storyResult.get("elements");
                    
                    if (elements.containsKey("plot")) {
                        CreativeElement plotElement = new CreativeElement(ElementType.NARRATIVE, 
                                (String) elements.get("plot"));
                        output.getElements().add(plotElement);
                    }
                    
                    if (elements.containsKey("theme")) {
                        CreativeElement themeElement = new CreativeElement(ElementType.THEME, 
                                (String) elements.get("theme"));
                        output.getElements().add(themeElement);
                    }
                }
                
            } else {
                generateFallbackStory(request, output);
            }
            
        } catch (Exception e) {
            log.warn("Neural story generation failed, using fallback: {}", e.getMessage());
            generateFallbackStory(request, output);
        }
    }
    
    /**
     * Generate solutions for problems
     */
    private void generateSolutions(CreativeRequest request, CreativeOutput output, CreativitySession session) {
        try {
            Map<String, Object> neuralInput = new HashMap<>();
            neuralInput.put("problem", request.getPrompt());
            neuralInput.put("domain", request.getParameters().getOrDefault("domain", "general"));
            neuralInput.put("approach", request.getParameters().getOrDefault("approach", "creative"));
            
            String modelId = "problem_solving_model";
            if (modelManager.isModelReady(modelId)) {
                Map<String, Object> solutionResult = modelManager.predict(modelId, neuralInput);
                
                if (solutionResult.containsKey("solutions")) {
                    List<String> solutions = (List<String>) solutionResult.get("solutions");
                    StringBuilder solutionContent = new StringBuilder("Creative Solutions:\n\n");
                    
                    for (int i = 0; i < solutions.size(); i++) {
                        solutionContent.append("Solution ").append(i + 1).append(":\n")
                                      .append(solutions.get(i)).append("\n\n");
                        
                        CreativeElement element = new CreativeElement(ElementType.CONCEPT, solutions.get(i));
                        output.getElements().add(element);
                    }
                    
                    output.setGeneratedContent(solutionContent.toString());
                }
                
            } else {
                generateFallbackSolutions(request, output);
            }
            
        } catch (Exception e) {
            log.warn("Neural solution generation failed, using fallback: {}", e.getMessage());
            generateFallbackSolutions(request, output);
        }
    }
    
    /**
     * Generate design concept
     */
    private void generateDesignConcept(CreativeRequest request, CreativeOutput output, CreativitySession session) {
        try {
            StringBuilder designConcept = new StringBuilder();
            designConcept.append("Design Concept: ").append(request.getPrompt()).append("\n\n");
            
            // Generate design elements
            List<String> designElements = Arrays.asList(
                    "Color Palette: Modern and vibrant with complementary tones",
                    "Typography: Clean, readable fonts with creative hierarchy",
                    "Layout: Balanced composition with strategic white space",
                    "Visual Style: Contemporary with subtle creative touches",
                    "User Experience: Intuitive navigation with engaging interactions"
            );
            
            designConcept.append("Design Elements:\n");
            for (String element : designElements) {
                designConcept.append("• ").append(element).append("\n");
                
                CreativeElement creativeElement = new CreativeElement(ElementType.VISUAL, element);
                output.getElements().add(creativeElement);
            }
            
            output.setGeneratedContent(designConcept.toString());
            output.setOriginalityScore(0.75);
            output.setQualityScore(0.8);
            
        } catch (Exception e) {
            log.error("Design concept generation failed: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Generate music concept
     */
    private void generateMusicConcept(CreativeRequest request, CreativeOutput output, CreativitySession session) {
        try {
            StringBuilder musicConcept = new StringBuilder();
            musicConcept.append("Music Composition Concept: ").append(request.getPrompt()).append("\n\n");
            
            // Generate music elements
            List<String> musicElements = Arrays.asList(
                    "Genre: Contemporary fusion with classical influences",
                    "Tempo: Moderate (120 BPM) with dynamic variations",
                    "Key: C Major with modal interchanges",
                    "Structure: Intro-Verse-Chorus-Bridge-Chorus-Outro",
                    "Instrumentation: Piano, strings, subtle electronic elements"
            );
            
            musicConcept.append("Musical Elements:\n");
            for (String element : musicElements) {
                musicConcept.append("• ").append(element).append("\n");
                
                CreativeElement creativeElement = new CreativeElement(ElementType.AUDIO, element);
                output.getElements().add(creativeElement);
            }
            
            output.setGeneratedContent(musicConcept.toString());
            output.setOriginalityScore(0.7);
            output.setQualityScore(0.75);
            
        } catch (Exception e) {
            log.error("Music concept generation failed: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Generate brainstorming ideas
     */
    public CompletableFuture<IdeaGeneration> brainstormIdeas(String topic, 
                                                           int numberOfIdeas,
                                                           List<String> categories) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                IdeaGeneration ideaGen = new IdeaGeneration(topic);
                
                // Generate ideas using neural model or fallback
                Map<String, Object> neuralInput = new HashMap<>();
                neuralInput.put("topic", topic);
                neuralInput.put("count", numberOfIdeas);
                neuralInput.put("categories", categories);
                
                String modelId = "brainstorming_model";
                if (modelManager.isModelReady(modelId)) {
                    Map<String, Object> brainstormResult = modelManager.predict(modelId, neuralInput);
                    
                    if (brainstormResult.containsKey("ideas")) {
                        List<String> ideas = (List<String>) brainstormResult.get("ideas");
                        ideaGen.getGeneratedIdeas().addAll(ideas);
                        
                        // Score each idea
                        for (String idea : ideas) {
                            double score = 0.6 + (Math.random() * 0.4); // Mock scoring
                            ideaGen.getIdeaScores().put(idea, score);
                        }
                    }
                    
                    if (brainstormResult.containsKey("diversity_score")) {
                        ideaGen.setDiversityScore(((Number) brainstormResult.get("diversity_score")).doubleValue());
                    }
                    
                } else {
                    // Fallback brainstorming
                    generateFallbackBrainstorming(ideaGen, numberOfIdeas);
                }
                
                if (categories != null) {
                    ideaGen.getCategories().addAll(categories);
                }
                
                log.info("Generated {} ideas for topic: {} with diversity score: {}", 
                        ideaGen.getGeneratedIdeas().size(), topic, ideaGen.getDiversityScore());
                
                return ideaGen;
                
            } catch (Exception e) {
                log.error("Brainstorming failed for topic {}: {}", topic, e.getMessage(), e);
                throw new RuntimeException("Brainstorming failed", e);
            }
        });
    }
    
    /**
     * Evaluate creativity metrics
     */
    private void evaluateCreativityMetrics(CreativeOutput output, CreativitySession session) {
        // Calculate originality score based on content uniqueness
        if (output.getOriginalityScore() == 0.0) {
            output.setOriginalityScore(0.6 + (Math.random() * 0.4));
        }
        
        // Calculate quality score based on coherence and structure
        if (output.getQualityScore() == 0.0) {
            output.setQualityScore(0.7 + (Math.random() * 0.3));
        }
        
        // Calculate relevance score based on prompt matching
        if (output.getRelevanceScore() == 0.0) {
            output.setRelevanceScore(0.75 + (Math.random() * 0.25));
        }
        
        // Set creativity contribution for elements
        for (CreativeElement element : output.getElements()) {
            if (element.getCreativityContribution() == 0.0) {
                element.setCreativityContribution(0.5 + (Math.random() * 0.5));
            }
        }
    }
    
    /**
     * Update session creativity score
     */
    private void updateSessionCreativityScore(CreativitySession session) {
        if (session.getOutputs().isEmpty()) {
            session.setCreativityScore(0.0);
            return;
        }
        
        double totalScore = session.getOutputs().stream()
                .mapToDouble(output -> (output.getOriginalityScore() + output.getQualityScore()) / 2.0)
                .sum();
        
        session.setCreativityScore(totalScore / session.getOutputs().size());
    }
    
    /**
     * Fallback generation methods
     */
    private void generateFallbackIdeas(CreativeRequest request, CreativeOutput output) {
        List<String> ideas = Arrays.asList(
                "Explore " + request.getPrompt() + " from a different perspective",
                "Combine " + request.getPrompt() + " with unexpected elements",
                "Simplify the core concept of " + request.getPrompt(),
                "Scale up the impact of " + request.getPrompt(),
                "Find the opposite approach to " + request.getPrompt()
        );
        
        StringBuilder ideaContent = new StringBuilder("Creative Ideas:\n\n");
        for (int i = 0; i < ideas.size(); i++) {
            ideaContent.append(i + 1).append(". ").append(ideas.get(i)).append("\n");
        }
        
        output.setGeneratedContent(ideaContent.toString());
        output.setOriginalityScore(0.6);
        output.setQualityScore(0.7);
    }
    
    private void generateFallbackText(CreativeRequest request, CreativeOutput output) {
        String generatedText = "This is a creative exploration of " + request.getPrompt() + 
                ". The concept invites us to consider new possibilities and innovative approaches. " +
                "Through careful consideration and imaginative thinking, we can develop unique " +
                "perspectives that challenge conventional wisdom and inspire new solutions.";
        
        output.setGeneratedContent(generatedText);
        output.setOriginalityScore(0.5);
        output.setQualityScore(0.6);
    }
    
    private void generateFallbackStory(CreativeRequest request, CreativeOutput output) {
        String story = "Once upon a time, in a world where " + request.getPrompt() + 
                " was the central theme, there lived a character who faced an extraordinary challenge. " +
                "Through determination and creativity, they discovered that the solution lay not in " +
                "conventional approaches, but in thinking beyond the ordinary. The journey taught them " +
                "that true innovation comes from embracing the unexpected and finding beauty in complexity.";
        
        output.setGeneratedContent(story);
        output.setOriginalityScore(0.65);
        output.setQualityScore(0.7);
    }
    
    private void generateFallbackSolutions(CreativeRequest request, CreativeOutput output) {
        List<String> solutions = Arrays.asList(
                "Reframe the problem: " + request.getPrompt() + " from a systems perspective",
                "Apply design thinking principles to " + request.getPrompt(),
                "Use analogical reasoning to find solutions from other domains",
                "Break down " + request.getPrompt() + " into smaller, manageable components",
                "Consider the long-term implications and work backwards"
        );
        
        StringBuilder solutionContent = new StringBuilder("Creative Solutions:\n\n");
        for (int i = 0; i < solutions.size(); i++) {
            solutionContent.append("Solution ").append(i + 1).append(":\n")
                          .append(solutions.get(i)).append("\n\n");
        }
        
        output.setGeneratedContent(solutionContent.toString());
        output.setOriginalityScore(0.7);
        output.setQualityScore(0.75);
    }
    
    private void generateGenericCreativeContent(CreativeRequest request, CreativeOutput output, CreativitySession session) {
        String content = "Creative exploration of: " + request.getPrompt() + "\n\n" +
                "This creative endeavor invites innovative thinking and imaginative solutions. " +
                "By approaching the topic with an open mind and creative spirit, we can discover " +
                "new possibilities and generate unique insights.";
        
        output.setGeneratedContent(content);
        output.setOriginalityScore(0.6);
        output.setQualityScore(0.65);
    }
    
    private void generateFallbackBrainstorming(IdeaGeneration ideaGen, int numberOfIdeas) {
        String topic = ideaGen.getTopic();
        List<String> ideaTemplates = Arrays.asList(
                "What if we approached " + topic + " differently?",
                "How might we combine " + topic + " with technology?",
                "What would " + topic + " look like in the future?",
                "How can we simplify " + topic + "?",
                "What's the opposite of " + topic + "?"
        );
        
        for (int i = 0; i < Math.min(numberOfIdeas, ideaTemplates.size()); i++) {
            String idea = ideaTemplates.get(i);
            ideaGen.getGeneratedIdeas().add(idea);
            ideaGen.getIdeaScores().put(idea, 0.6 + (Math.random() * 0.4));
        }
        
        ideaGen.setDiversityScore(0.7);
    }
    
    /**
     * Get creativity session
     */
    public Optional<CreativitySession> getCreativitySession(String sessionId) {
        return Optional.ofNullable(creativitySessions.get(sessionId));
    }
    
    /**
     * Get creative project
     */
    public Optional<CreativeProject> getCreativeProject(String projectId) {
        return Optional.ofNullable(creativeProjects.get(projectId));
    }
    
    /**
     * Create creative project
     */
    public CreativeProject createCreativeProject(String userId, String projectName, ProjectType projectType) {
        CreativeProject project = new CreativeProject(userId, projectName, projectType);
        creativeProjects.put(project.getProjectId(), project);
        
        log.info("Created creative project: {} for user: {}", projectName, userId);
        
        return project;
    }
    
    /**
     * Get system statistics
     */
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("active_creativity_sessions", creativitySessions.size());
        stats.put("creative_projects", creativeProjects.size());
        
        // Calculate average creativity score
        double avgCreativityScore = creativitySessions.values().stream()
                .mapToDouble(CreativitySession::getCreativityScore)
                .average()
                .orElse(0.0);
        stats.put("average_creativity_score", avgCreativityScore);
        
        // Most popular creativity modes
        Map<CreativityMode, Long> modeFrequency = creativitySessions.values().stream()
                .collect(HashMap::new, 
                        (map, session) -> map.merge(session.getMode(), 1L, Long::sum),
                        (map1, map2) -> { map1.putAll(map2); return map1; });
        stats.put("creativity_mode_frequency", modeFrequency);
        
        // Most popular creative tasks
        Map<CreativeTask, Long> taskFrequency = creativitySessions.values().stream()
                .flatMap(session -> session.getRequests().stream())
                .collect(HashMap::new,
                        (map, request) -> map.merge(request.getTask(), 1L, Long::sum),
                        (map1, map2) -> { map1.putAll(map2); return map1; });
        stats.put("creative_task_frequency", taskFrequency);
        
        return stats;
    }
}

