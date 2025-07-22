package com.agi.nlp.training.config;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

/**
 * NLP 모델 훈련 설정
 * 모델 아키텍처, 하이퍼파라미터, 훈련 옵션 등을 정의
 */
@Data
@Builder
public class TrainingConfig {
    
    // 기본 설정
    private String modelName;
    private String version;
    private String description;
    
    // 데이터 설정
    private String datasetPath;
    private String validationDataPath;
    private String testDataPath;
    
    // 모델 아키텍처
    private ModelArchitecture architecture;
    private int vocabSize;
    private int hiddenSize;
    private int numLayers;
    private int numAttentionHeads;
    private int maxSequenceLength;
    
    // 훈련 하이퍼파라미터
    private int epochs;
    private int batchSize;
    private double learningRate;
    private double weightDecay;
    private double dropoutRate;
    private String optimizerType;
    private String lrSchedulerType;
    
    // 정규화 설정
    private boolean useLayerNorm;
    private boolean useBatchNorm;
    private double gradientClipNorm;
    
    // 체크포인트 설정
    private String checkpointDir;
    private int saveEveryNEpochs;
    private boolean saveOnlyBestModel;
    private String metricForBest;
    
    // 하드웨어 설정
    private boolean useGpu;
    private int gpuCount;
    private boolean useDistributedTraining;
    private int numWorkers;
    
    // 로깅 및 모니터링
    private boolean enableTensorboard;
    private boolean enableWandb;
    private String logDir;
    private int logEveryNSteps;
    
    // 조기 종료 설정
    private boolean useEarlyStopping;
    private int patience;
    private double minDelta;
    
    // 데이터 증강 설정
    private boolean useDataAugmentation;
    private double augmentationRatio;
    private Map<String, Object> augmentationConfig;
    
    // 추가 설정
    private Map<String, Object> customConfig;
    
    /**
     * 기본 설정으로 TrainingConfig 생성
     */
    public static TrainingConfig getDefaultConfig() {
        return TrainingConfig.builder()
            .modelName("default_nlp_model")
            .version("1.0.0")
            .description("Default NLP training configuration")
            
            // 모델 아키텍처
            .architecture(ModelArchitecture.TRANSFORMER)
            .vocabSize(30000)
            .hiddenSize(768)
            .numLayers(12)
            .numAttentionHeads(12)
            .maxSequenceLength(512)
            
            // 훈련 설정
            .epochs(10)
            .batchSize(32)
            .learningRate(2e-5)
            .weightDecay(0.01)
            .dropoutRate(0.1)
            .optimizerType("AdamW")
            .lrSchedulerType("linear")
            
            // 정규화
            .useLayerNorm(true)
            .useBatchNorm(false)
            .gradientClipNorm(1.0)
            
            // 체크포인트
            .checkpointDir("./checkpoints")
            .saveEveryNEpochs(1)
            .saveOnlyBestModel(true)
            .metricForBest("f1_score")
            
            // 하드웨어
            .useGpu(true)
            .gpuCount(1)
            .useDistributedTraining(false)
            .numWorkers(4)
            
            // 로깅
            .enableTensorboard(true)
            .enableWandb(false)
            .logDir("./logs")
            .logEveryNSteps(100)
            
            // 조기 종료
            .useEarlyStopping(true)
            .patience(3)
            .minDelta(0.001)
            
            // 데이터 증강
            .useDataAugmentation(true)
            .augmentationRatio(0.2)
            
            .build();
    }
    
    /**
     * BERT 모델용 설정
     */
    public static TrainingConfig getBertConfig() {
        return getDefaultConfig().toBuilder()
            .modelName("bert_model")
            .architecture(ModelArchitecture.BERT)
            .hiddenSize(768)
            .numLayers(12)
            .numAttentionHeads(12)
            .learningRate(2e-5)
            .build();
    }
    
    /**
     * GPT 모델용 설정
     */
    public static TrainingConfig getGptConfig() {
        return getDefaultConfig().toBuilder()
            .modelName("gpt_model")
            .architecture(ModelArchitecture.GPT)
            .hiddenSize(1024)
            .numLayers(24)
            .numAttentionHeads(16)
            .learningRate(1e-4)
            .build();
    }
    
    /**
     * 경량 모델용 설정 (빠른 실험용)
     */
    public static TrainingConfig getLightweightConfig() {
        return getDefaultConfig().toBuilder()
            .modelName("lightweight_model")
            .architecture(ModelArchitecture.LSTM)
            .hiddenSize(256)
            .numLayers(2)
            .epochs(5)
            .batchSize(64)
            .learningRate(1e-3)
            .build();
    }
    
    /**
     * 설정 검증
     */
    public void validate() {
        if (epochs <= 0) {
            throw new IllegalArgumentException("Epochs must be positive");
        }
        if (batchSize <= 0) {
            throw new IllegalArgumentException("Batch size must be positive");
        }
        if (learningRate <= 0) {
            throw new IllegalArgumentException("Learning rate must be positive");
        }
        if (hiddenSize <= 0) {
            throw new IllegalArgumentException("Hidden size must be positive");
        }
        if (numLayers <= 0) {
            throw new IllegalArgumentException("Number of layers must be positive");
        }
        if (vocabSize <= 0) {
            throw new IllegalArgumentException("Vocabulary size must be positive");
        }
        if (maxSequenceLength <= 0) {
            throw new IllegalArgumentException("Max sequence length must be positive");
        }
    }
    
    /**
     * 설정을 Map으로 변환 (로깅용)
     */
    public Map<String, Object> toMap() {
        return Map.of(
            "model_name", modelName,
            "architecture", architecture.name(),
            "epochs", epochs,
            "batch_size", batchSize,
            "learning_rate", learningRate,
            "hidden_size", hiddenSize,
            "num_layers", numLayers,
            "vocab_size", vocabSize,
            "use_gpu", useGpu,
            "gpu_count", gpuCount
        );
    }
}

/**
 * 모델 아키텍처 타입
 */
enum ModelArchitecture {
    TRANSFORMER("Transformer"),
    BERT("BERT"),
    GPT("GPT"),
    LSTM("LSTM"),
    CNN("CNN"),
    CUSTOM("Custom");
    
    private final String displayName;
    
    ModelArchitecture(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}

