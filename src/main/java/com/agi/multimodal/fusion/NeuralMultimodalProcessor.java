package com.agi.multimodal.fusion;

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
 * Neural network-powered multimodal processor for handling text, image, audio, and video data
 */
@Slf4j
@Component
public class NeuralMultimodalProcessor {
    
    @Autowired
    private ModelManager modelManager;
    
    private final Map<String, ProcessingSession> processingSessions = new ConcurrentHashMap<>();
    private final Map<String, ModalityProcessor> modalityProcessors = new ConcurrentHashMap<>();
    
    @Data
    public static class ProcessingSession {
        private String sessionId;
        private String userId;
        private LocalDateTime startTime;
        private LocalDateTime lastActivity;
        private List<MultimodalInput> inputs;
        private List<ProcessingResult> results;
        private Map<String, Object> sessionContext;
        private FusionStrategy fusionStrategy;
        private SessionStatus status;
        private double overallConfidence;
        
        public ProcessingSession(String userId) {
            this.sessionId = UUID.randomUUID().toString();
            this.userId = userId;
            this.startTime = LocalDateTime.now();
            this.lastActivity = LocalDateTime.now();
            this.inputs = new ArrayList<>();
            this.results = new ArrayList<>();
            this.sessionContext = new HashMap<>();
            this.fusionStrategy = FusionStrategy.NEURAL_FUSION;
            this.status = SessionStatus.ACTIVE;
            this.overallConfidence = 0.0;
        }
    }
    
    @Data
    public static class MultimodalInput {
        private String inputId;
        private ModalityType modalityType;
        private String contentPath;
        private byte[] contentData;
        private Map<String, Object> metadata;
        private double[] embedding;
        private LocalDateTime timestamp;
        private InputStatus status;
        private double processingConfidence;
        
        public MultimodalInput(ModalityType modalityType, String contentPath) {
            this.inputId = UUID.randomUUID().toString();
            this.modalityType = modalityType;
            this.contentPath = contentPath;
            this.metadata = new HashMap<>();
            this.timestamp = LocalDateTime.now();
            this.status = InputStatus.PENDING;
            this.processingConfidence = 0.0;
        }
        
        public MultimodalInput(ModalityType modalityType, byte[] contentData) {
            this.inputId = UUID.randomUUID().toString();
            this.modalityType = modalityType;
            this.contentData = contentData;
            this.metadata = new HashMap<>();
            this.timestamp = LocalDateTime.now();
            this.status = InputStatus.PENDING;
            this.processingConfidence = 0.0;
        }
    }
    
    @Data
    public static class ProcessingResult {
        private String resultId;
        private String inputId;
        private ModalityType sourceModality;
        private ResultType resultType;
        private Map<String, Object> extractedFeatures;
        private String textDescription;
        private List<DetectedObject> detectedObjects;
        private List<String> extractedText;
        private Map<String, Double> emotions;
        private Map<String, Double> concepts;
        private double confidence;
        private LocalDateTime processedAt;
        private long processingTimeMs;
        
        public ProcessingResult(String inputId, ModalityType sourceModality, ResultType resultType) {
            this.resultId = UUID.randomUUID().toString();
            this.inputId = inputId;
            this.sourceModality = sourceModality;
            this.resultType = resultType;
            this.extractedFeatures = new HashMap<>();
            this.detectedObjects = new ArrayList<>();
            this.extractedText = new ArrayList<>();
            this.emotions = new HashMap<>();
            this.concepts = new HashMap<>();
            this.confidence = 0.0;
            this.processedAt = LocalDateTime.now();
        }
    }
    
    @Data
    public static class DetectedObject {
        private String objectId;
        private String objectClass;
        private double confidence;
        private BoundingBox boundingBox;
        private Map<String, Object> attributes;
        
        public DetectedObject(String objectClass, double confidence) {
            this.objectId = UUID.randomUUID().toString();
            this.objectClass = objectClass;
            this.confidence = confidence;
            this.attributes = new HashMap<>();
        }
    }
    
    @Data
    public static class BoundingBox {
        private double x;
        private double y;
        private double width;
        private double height;
        
        public BoundingBox(double x, double y, double width, double height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
    
    @Data
    public static class ModalityProcessor {
        private String processorId;
        private ModalityType modalityType;
        private String modelId;
        private Map<String, Object> processorConfig;
        private ProcessorStatus status;
        private double averageProcessingTime;
        private int processedCount;
        private double successRate;
        
        public ModalityProcessor(ModalityType modalityType, String modelId) {
            this.processorId = UUID.randomUUID().toString();
            this.modalityType = modalityType;
            this.modelId = modelId;
            this.processorConfig = new HashMap<>();
            this.status = ProcessorStatus.READY;
            this.averageProcessingTime = 0.0;
            this.processedCount = 0;
            this.successRate = 0.0;
        }
    }
    
    public enum ModalityType {
        TEXT, IMAGE, AUDIO, VIDEO, SENSOR_DATA, STRUCTURED_DATA
    }
    
    public enum SessionStatus {
        ACTIVE, PROCESSING, COMPLETED, FAILED, CANCELLED
    }
    
    public enum InputStatus {
        PENDING, PROCESSING, COMPLETED, FAILED
    }
    
    public enum ResultType {
        FEATURE_EXTRACTION, OBJECT_DETECTION, TEXT_EXTRACTION, 
        EMOTION_ANALYSIS, CONCEPT_RECOGNITION, DESCRIPTION_GENERATION,
        SIMILARITY_ANALYSIS, CLASSIFICATION
    }
    
    public enum FusionStrategy {
        EARLY_FUSION, LATE_FUSION, NEURAL_FUSION, ATTENTION_FUSION, 
        HIERARCHICAL_FUSION, ADAPTIVE_FUSION
    }
    
    public enum ProcessorStatus {
        READY, BUSY, ERROR, MAINTENANCE
    }
    
    /**
     * Initialize modality processors
     */
    public void initializeProcessors() {
        // Text processor
        ModalityProcessor textProcessor = new ModalityProcessor(ModalityType.TEXT, "text_analysis_model");
        textProcessor.getProcessorConfig().put("max_length", 10000);
        textProcessor.getProcessorConfig().put("language_detection", true);
        modalityProcessors.put("text_processor", textProcessor);
        
        // Image processor
        ModalityProcessor imageProcessor = new ModalityProcessor(ModalityType.IMAGE, "image_analysis_model");
        imageProcessor.getProcessorConfig().put("max_resolution", "4096x4096");
        imageProcessor.getProcessorConfig().put("object_detection", true);
        modalityProcessors.put("image_processor", imageProcessor);
        
        // Audio processor
        ModalityProcessor audioProcessor = new ModalityProcessor(ModalityType.AUDIO, "audio_analysis_model");
        audioProcessor.getProcessorConfig().put("sample_rate", 44100);
        audioProcessor.getProcessorConfig().put("speech_recognition", true);
        modalityProcessors.put("audio_processor", audioProcessor);
        
        // Video processor
        ModalityProcessor videoProcessor = new ModalityProcessor(ModalityType.VIDEO, "video_analysis_model");
        videoProcessor.getProcessorConfig().put("frame_rate", 30);
        videoProcessor.getProcessorConfig().put("scene_detection", true);
        modalityProcessors.put("video_processor", videoProcessor);
        
        log.info("Initialized {} modality processors", modalityProcessors.size());
    }
    
    /**
     * Start processing session
     */
    public ProcessingSession startSession(String userId, FusionStrategy strategy) {
        ProcessingSession session = new ProcessingSession(userId);
        if (strategy != null) {
            session.setFusionStrategy(strategy);
        }
        
        processingSessions.put(session.getSessionId(), session);
        
        log.info("Started multimodal processing session: {} with strategy: {}", 
                session.getSessionId(), session.getFusionStrategy());
        
        return session;
    }
    
    /**
     * Process multimodal input
     */
    public CompletableFuture<List<ProcessingResult>> processMultimodalInput(String sessionId,
                                                                           List<MultimodalInput> inputs) {
        
        return CompletableFuture.supplyAsync(() -> {
            ProcessingSession session = processingSessions.get(sessionId);
            if (session == null) {
                throw new IllegalArgumentException("Processing session not found: " + sessionId);
            }
            
            try {
                session.setStatus(SessionStatus.PROCESSING);
                session.getInputs().addAll(inputs);
                
                List<ProcessingResult> allResults = new ArrayList<>();
                
                // Process each modality
                for (MultimodalInput input : inputs) {
                    List<ProcessingResult> modalityResults = processModalityInput(input);
                    allResults.addAll(modalityResults);
                }
                
                // Apply multimodal fusion
                List<ProcessingResult> fusedResults = applyMultimodalFusion(allResults, session);
                
                session.getResults().addAll(fusedResults);
                session.setStatus(SessionStatus.COMPLETED);
                session.setLastActivity(LocalDateTime.now());
                
                // Calculate overall confidence
                calculateOverallConfidence(session);
                
                log.info("Processed {} inputs with {} results for session: {}", 
                        inputs.size(), fusedResults.size(), sessionId);
                
                return fusedResults;
                
            } catch (Exception e) {
                session.setStatus(SessionStatus.FAILED);
                log.error("Multimodal processing failed for session {}: {}", sessionId, e.getMessage(), e);
                throw new RuntimeException("Multimodal processing failed", e);
            }
        });
    }
    
    /**
     * Process single modality input
     */
    private List<ProcessingResult> processModalityInput(MultimodalInput input) {
        List<ProcessingResult> results = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        
        try {
            input.setStatus(InputStatus.PROCESSING);
            
            switch (input.getModalityType()) {
                case TEXT:
                    results.addAll(processTextInput(input));
                    break;
                case IMAGE:
                    results.addAll(processImageInput(input));
                    break;
                case AUDIO:
                    results.addAll(processAudioInput(input));
                    break;
                case VIDEO:
                    results.addAll(processVideoInput(input));
                    break;
                default:
                    log.warn("Unsupported modality type: {}", input.getModalityType());
            }
            
            input.setStatus(InputStatus.COMPLETED);
            
        } catch (Exception e) {
            input.setStatus(InputStatus.FAILED);
            log.error("Failed to process {} input: {}", input.getModalityType(), e.getMessage(), e);
        }
        
        long processingTime = System.currentTimeMillis() - startTime;
        results.forEach(result -> result.setProcessingTimeMs(processingTime));
        
        return results;
    }
    
    /**
     * Process text input
     */
    private List<ProcessingResult> processTextInput(MultimodalInput input) {
        List<ProcessingResult> results = new ArrayList<>();
        
        try {
            String text = getTextContent(input);
            
            // Feature extraction
            ProcessingResult featureResult = new ProcessingResult(
                    input.getInputId(), ModalityType.TEXT, ResultType.FEATURE_EXTRACTION);
            
            Map<String, Object> neuralInput = new HashMap<>();
            neuralInput.put("text", text);
            neuralInput.put("text_length", text.length());
            
            String modelId = "text_analysis_model";
            if (modelManager.isModelReady(modelId)) {
                Map<String, Object> analysis = modelManager.predict(modelId, neuralInput);
                
                // Extract features
                featureResult.getExtractedFeatures().putAll(analysis);
                
                // Extract emotions
                if (analysis.containsKey("emotions")) {
                    Map<String, Double> emotions = (Map<String, Double>) analysis.get("emotions");
                    featureResult.setEmotions(emotions);
                }
                
                // Extract concepts
                if (analysis.containsKey("concepts")) {
                    Map<String, Double> concepts = (Map<String, Double>) analysis.get("concepts");
                    featureResult.setConcepts(concepts);
                }
                
                featureResult.setConfidence(0.8);
                
            } else {
                // Fallback processing
                featureResult = processTextFallback(input, text);
            }
            
            results.add(featureResult);
            
        } catch (Exception e) {
            log.error("Text processing failed: {}", e.getMessage(), e);
        }
        
        return results;
    }
    
    /**
     * Process image input
     */
    private List<ProcessingResult> processImageInput(MultimodalInput input) {
        List<ProcessingResult> results = new ArrayList<>();
        
        try {
            // Object detection
            ProcessingResult objectResult = new ProcessingResult(
                    input.getInputId(), ModalityType.IMAGE, ResultType.OBJECT_DETECTION);
            
            Map<String, Object> neuralInput = new HashMap<>();
            neuralInput.put("image_data", input.getContentData());
            neuralInput.put("image_path", input.getContentPath());
            
            String modelId = "image_analysis_model";
            if (modelManager.isModelReady(modelId)) {
                Map<String, Object> analysis = modelManager.predict(modelId, neuralInput);
                
                // Extract detected objects
                if (analysis.containsKey("objects")) {
                    List<Map<String, Object>> objects = (List<Map<String, Object>>) analysis.get("objects");
                    for (Map<String, Object> obj : objects) {
                        String objectClass = (String) obj.get("class");
                        double confidence = ((Number) obj.get("confidence")).doubleValue();
                        
                        DetectedObject detectedObject = new DetectedObject(objectClass, confidence);
                        
                        if (obj.containsKey("bbox")) {
                            Map<String, Number> bbox = (Map<String, Number>) obj.get("bbox");
                            BoundingBox boundingBox = new BoundingBox(
                                    bbox.get("x").doubleValue(),
                                    bbox.get("y").doubleValue(),
                                    bbox.get("width").doubleValue(),
                                    bbox.get("height").doubleValue());
                            detectedObject.setBoundingBox(boundingBox);
                        }
                        
                        objectResult.getDetectedObjects().add(detectedObject);
                    }
                }
                
                // Generate description
                if (analysis.containsKey("description")) {
                    objectResult.setTextDescription((String) analysis.get("description"));
                }
                
                objectResult.setConfidence(0.85);
                
            } else {
                // Fallback processing
                objectResult = processImageFallback(input);
            }
            
            results.add(objectResult);
            
        } catch (Exception e) {
            log.error("Image processing failed: {}", e.getMessage(), e);
        }
        
        return results;
    }
    
    /**
     * Process audio input
     */
    private List<ProcessingResult> processAudioInput(MultimodalInput input) {
        List<ProcessingResult> results = new ArrayList<>();
        
        try {
            // Speech recognition and analysis
            ProcessingResult audioResult = new ProcessingResult(
                    input.getInputId(), ModalityType.AUDIO, ResultType.TEXT_EXTRACTION);
            
            Map<String, Object> neuralInput = new HashMap<>();
            neuralInput.put("audio_data", input.getContentData());
            neuralInput.put("audio_path", input.getContentPath());
            
            String modelId = "audio_analysis_model";
            if (modelManager.isModelReady(modelId)) {
                Map<String, Object> analysis = modelManager.predict(modelId, neuralInput);
                
                // Extract transcribed text
                if (analysis.containsKey("transcription")) {
                    String transcription = (String) analysis.get("transcription");
                    audioResult.getExtractedText().add(transcription);
                    audioResult.setTextDescription(transcription);
                }
                
                // Extract emotions from speech
                if (analysis.containsKey("speech_emotions")) {
                    Map<String, Double> emotions = (Map<String, Double>) analysis.get("speech_emotions");
                    audioResult.setEmotions(emotions);
                }
                
                audioResult.setConfidence(0.75);
                
            } else {
                // Fallback processing
                audioResult = processAudioFallback(input);
            }
            
            results.add(audioResult);
            
        } catch (Exception e) {
            log.error("Audio processing failed: {}", e.getMessage(), e);
        }
        
        return results;
    }
    
    /**
     * Process video input
     */
    private List<ProcessingResult> processVideoInput(MultimodalInput input) {
        List<ProcessingResult> results = new ArrayList<>();
        
        try {
            // Video analysis
            ProcessingResult videoResult = new ProcessingResult(
                    input.getInputId(), ModalityType.VIDEO, ResultType.DESCRIPTION_GENERATION);
            
            Map<String, Object> neuralInput = new HashMap<>();
            neuralInput.put("video_data", input.getContentData());
            neuralInput.put("video_path", input.getContentPath());
            
            String modelId = "video_analysis_model";
            if (modelManager.isModelReady(modelId)) {
                Map<String, Object> analysis = modelManager.predict(modelId, neuralInput);
                
                // Extract video description
                if (analysis.containsKey("description")) {
                    videoResult.setTextDescription((String) analysis.get("description"));
                }
                
                // Extract key frames objects
                if (analysis.containsKey("key_frame_objects")) {
                    List<Map<String, Object>> objects = (List<Map<String, Object>>) analysis.get("key_frame_objects");
                    for (Map<String, Object> obj : objects) {
                        String objectClass = (String) obj.get("class");
                        double confidence = ((Number) obj.get("confidence")).doubleValue();
                        videoResult.getDetectedObjects().add(new DetectedObject(objectClass, confidence));
                    }
                }
                
                videoResult.setConfidence(0.7);
                
            } else {
                // Fallback processing
                videoResult = processVideoFallback(input);
            }
            
            results.add(videoResult);
            
        } catch (Exception e) {
            log.error("Video processing failed: {}", e.getMessage(), e);
        }
        
        return results;
    }
    
    /**
     * Apply multimodal fusion
     */
    private List<ProcessingResult> applyMultimodalFusion(List<ProcessingResult> results, 
                                                        ProcessingSession session) {
        
        if (results.size() <= 1) {
            return results; // No fusion needed
        }
        
        try {
            switch (session.getFusionStrategy()) {
                case NEURAL_FUSION:
                    return applyNeuralFusion(results, session);
                case ATTENTION_FUSION:
                    return applyAttentionFusion(results, session);
                case LATE_FUSION:
                    return applyLateFusion(results, session);
                default:
                    return applyEarlyFusion(results, session);
            }
            
        } catch (Exception e) {
            log.warn("Multimodal fusion failed, returning original results: {}", e.getMessage());
            return results;
        }
    }
    
    /**
     * Apply neural fusion
     */
    private List<ProcessingResult> applyNeuralFusion(List<ProcessingResult> results, 
                                                    ProcessingSession session) {
        try {
            // Prepare fusion input
            Map<String, Object> fusionInput = new HashMap<>();
            fusionInput.put("result_count", results.size());
            
            // Add modality features
            for (int i = 0; i < results.size(); i++) {
                ProcessingResult result = results.get(i);
                fusionInput.put("modality_" + i, result.getSourceModality().name());
                fusionInput.put("confidence_" + i, result.getConfidence());
                fusionInput.put("features_" + i, result.getExtractedFeatures());
            }
            
            String modelId = "multimodal_fusion_model";
            if (modelManager.isModelReady(modelId)) {
                Map<String, Object> fusion = modelManager.predict(modelId, fusionInput);
                
                // Create fused result
                ProcessingResult fusedResult = new ProcessingResult(
                        "fused_" + System.currentTimeMillis(),
                        ModalityType.STRUCTURED_DATA,
                        ResultType.SIMILARITY_ANALYSIS);
                
                fusedResult.getExtractedFeatures().putAll(fusion);
                fusedResult.setConfidence(((Number) fusion.getOrDefault("fused_confidence", 0.8)).doubleValue());
                
                // Combine descriptions
                String combinedDescription = results.stream()
                        .map(ProcessingResult::getTextDescription)
                        .filter(Objects::nonNull)
                        .reduce((a, b) -> a + "; " + b)
                        .orElse("Multimodal analysis result");
                fusedResult.setTextDescription(combinedDescription);
                
                List<ProcessingResult> fusedResults = new ArrayList<>(results);
                fusedResults.add(fusedResult);
                
                return fusedResults;
            }
            
        } catch (Exception e) {
            log.warn("Neural fusion failed: {}", e.getMessage());
        }
        
        return results;
    }
    
    /**
     * Apply attention fusion
     */
    private List<ProcessingResult> applyAttentionFusion(List<ProcessingResult> results, 
                                                       ProcessingSession session) {
        // Calculate attention weights based on confidence and modality importance
        Map<ModalityType, Double> modalityWeights = new HashMap<>();
        modalityWeights.put(ModalityType.TEXT, 0.3);
        modalityWeights.put(ModalityType.IMAGE, 0.4);
        modalityWeights.put(ModalityType.AUDIO, 0.2);
        modalityWeights.put(ModalityType.VIDEO, 0.1);
        
        // Apply attention weights
        for (ProcessingResult result : results) {
            double modalityWeight = modalityWeights.getOrDefault(result.getSourceModality(), 0.25);
            double attentionScore = result.getConfidence() * modalityWeight;
            result.getExtractedFeatures().put("attention_score", attentionScore);
        }
        
        return results;
    }
    
    /**
     * Apply late fusion
     */
    private List<ProcessingResult> applyLateFusion(List<ProcessingResult> results, 
                                                  ProcessingSession session) {
        // Simple late fusion: combine results at decision level
        ProcessingResult fusedResult = new ProcessingResult(
                "late_fused_" + System.currentTimeMillis(),
                ModalityType.STRUCTURED_DATA,
                ResultType.CLASSIFICATION);
        
        // Average confidence
        double avgConfidence = results.stream()
                .mapToDouble(ProcessingResult::getConfidence)
                .average()
                .orElse(0.0);
        fusedResult.setConfidence(avgConfidence);
        
        // Combine features
        for (ProcessingResult result : results) {
            String prefix = result.getSourceModality().name().toLowerCase() + "_";
            result.getExtractedFeatures().forEach((key, value) -> 
                    fusedResult.getExtractedFeatures().put(prefix + key, value));
        }
        
        List<ProcessingResult> fusedResults = new ArrayList<>(results);
        fusedResults.add(fusedResult);
        
        return fusedResults;
    }
    
    /**
     * Apply early fusion
     */
    private List<ProcessingResult> applyEarlyFusion(List<ProcessingResult> results, 
                                                   ProcessingSession session) {
        // Early fusion: combine at feature level
        ProcessingResult fusedResult = new ProcessingResult(
                "early_fused_" + System.currentTimeMillis(),
                ModalityType.STRUCTURED_DATA,
                ResultType.FEATURE_EXTRACTION);
        
        // Combine all features
        for (ProcessingResult result : results) {
            fusedResult.getExtractedFeatures().putAll(result.getExtractedFeatures());
        }
        
        // Calculate weighted confidence
        double totalWeight = results.stream().mapToDouble(ProcessingResult::getConfidence).sum();
        double weightedConfidence = results.stream()
                .mapToDouble(r -> r.getConfidence() * r.getConfidence())
                .sum() / totalWeight;
        fusedResult.setConfidence(weightedConfidence);
        
        List<ProcessingResult> fusedResults = new ArrayList<>(results);
        fusedResults.add(fusedResult);
        
        return fusedResults;
    }
    
    /**
     * Fallback processing methods
     */
    private ProcessingResult processTextFallback(MultimodalInput input, String text) {
        ProcessingResult result = new ProcessingResult(
                input.getInputId(), ModalityType.TEXT, ResultType.FEATURE_EXTRACTION);
        
        // Simple text analysis
        result.getExtractedFeatures().put("word_count", text.split("\\s+").length);
        result.getExtractedFeatures().put("char_count", text.length());
        result.getExtractedFeatures().put("sentence_count", text.split("[.!?]+").length);
        
        // Simple emotion detection
        Map<String, Double> emotions = new HashMap<>();
        emotions.put("positive", text.toLowerCase().contains("good") || text.toLowerCase().contains("happy") ? 0.7 : 0.3);
        emotions.put("negative", text.toLowerCase().contains("bad") || text.toLowerCase().contains("sad") ? 0.7 : 0.3);
        result.setEmotions(emotions);
        
        result.setConfidence(0.6);
        return result;
    }
    
    private ProcessingResult processImageFallback(MultimodalInput input) {
        ProcessingResult result = new ProcessingResult(
                input.getInputId(), ModalityType.IMAGE, ResultType.OBJECT_DETECTION);
        
        // Mock object detection
        result.getDetectedObjects().add(new DetectedObject("unknown_object", 0.5));
        result.setTextDescription("Image analysis (fallback mode)");
        result.setConfidence(0.4);
        
        return result;
    }
    
    private ProcessingResult processAudioFallback(MultimodalInput input) {
        ProcessingResult result = new ProcessingResult(
                input.getInputId(), ModalityType.AUDIO, ResultType.TEXT_EXTRACTION);
        
        result.getExtractedText().add("Audio transcription not available");
        result.setTextDescription("Audio analysis (fallback mode)");
        result.setConfidence(0.3);
        
        return result;
    }
    
    private ProcessingResult processVideoFallback(MultimodalInput input) {
        ProcessingResult result = new ProcessingResult(
                input.getInputId(), ModalityType.VIDEO, ResultType.DESCRIPTION_GENERATION);
        
        result.setTextDescription("Video analysis (fallback mode)");
        result.setConfidence(0.3);
        
        return result;
    }
    
    /**
     * Helper methods
     */
    private String getTextContent(MultimodalInput input) {
        if (input.getContentData() != null) {
            return new String(input.getContentData());
        } else if (input.getContentPath() != null) {
            // In real implementation, read from file
            return "Text content from: " + input.getContentPath();
        }
        return "";
    }
    
    private void calculateOverallConfidence(ProcessingSession session) {
        if (session.getResults().isEmpty()) {
            session.setOverallConfidence(0.0);
            return;
        }
        
        double totalConfidence = session.getResults().stream()
                .mapToDouble(ProcessingResult::getConfidence)
                .sum();
        
        session.setOverallConfidence(totalConfidence / session.getResults().size());
    }
    
    /**
     * Get processing session
     */
    public Optional<ProcessingSession> getProcessingSession(String sessionId) {
        return Optional.ofNullable(processingSessions.get(sessionId));
    }
    
    /**
     * Get system statistics
     */
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("active_sessions", processingSessions.size());
        stats.put("available_processors", modalityProcessors.size());
        
        // Calculate average confidence
        double avgConfidence = processingSessions.values().stream()
                .mapToDouble(ProcessingSession::getOverallConfidence)
                .average()
                .orElse(0.0);
        stats.put("average_confidence", avgConfidence);
        
        // Processor statistics
        Map<String, Object> processorStats = new HashMap<>();
        modalityProcessors.forEach((key, processor) -> {
            Map<String, Object> procStat = new HashMap<>();
            procStat.put("status", processor.getStatus());
            procStat.put("processed_count", processor.getProcessedCount());
            procStat.put("success_rate", processor.getSuccessRate());
            processorStats.put(key, procStat);
        });
        stats.put("processors", processorStats);
        
        return stats;
    }
}

