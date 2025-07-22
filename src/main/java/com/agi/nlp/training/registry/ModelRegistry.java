package com.agi.nlp.training.registry;

import com.agi.nlp.training.evaluation.EvaluationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 훈련된 모델들의 중앙 레지스트리
 * 모델 버전 관리, 메타데이터 저장, 모델 검색 등을 담당
 */
@Slf4j
@Component
public class ModelRegistry {
    
    // 등록된 모델들
    private final Map<String, RegisteredModel> models = new ConcurrentHashMap<>();
    
    // 모델 버전 관리
    private final Map<String, List<ModelVersion>> modelVersions = new ConcurrentHashMap<>();
    
    // 활성 모델 (배포된 모델)
    private final Map<String, String> activeModels = new ConcurrentHashMap<>();
    
    /**
     * 모델 등록
     */
    public String registerModel(ModelRegistrationRequest request) {
        String modelId = generateModelId(request.getModelName());
        
        RegisteredModel model = RegisteredModel.builder()
            .modelId(modelId)
            .modelName(request.getModelName())
            .description(request.getDescription())
            .architecture(request.getArchitecture())
            .version(request.getVersion())
            .modelPath(request.getModelPath())
            .configPath(request.getConfigPath())
            .evaluation(request.getEvaluation())
            .tags(request.getTags())
            .metadata(request.getMetadata())
            .createdBy(request.getCreatedBy())
            .createdAt(System.currentTimeMillis())
            .status(ModelStatus.REGISTERED)
            .build();
        
        models.put(modelId, model);
        
        // 버전 관리
        addModelVersion(request.getModelName(), model);
        
        log.info("Model registered: {} (ID: {})", request.getModelName(), modelId);
        return modelId;
    }
    
    /**
     * 모델 버전 추가
     */
    private void addModelVersion(String modelName, RegisteredModel model) {
        modelVersions.computeIfAbsent(modelName, k -> new ArrayList<>())
            .add(ModelVersion.builder()
                .modelId(model.getModelId())
                .version(model.getVersion())
                .createdAt(model.getCreatedAt())
                .evaluation(model.getEvaluation())
                .build());
        
        // 버전 정렬 (최신순)
        modelVersions.get(modelName).sort((v1, v2) -> 
            Long.compare(v2.getCreatedAt(), v1.getCreatedAt()));
    }
    
    /**
     * 모델 조회
     */
    public Optional<RegisteredModel> getModel(String modelId) {
        return Optional.ofNullable(models.get(modelId));
    }
    
    /**
     * 모델 이름으로 최신 버전 조회
     */
    public Optional<RegisteredModel> getLatestModel(String modelName) {
        List<ModelVersion> versions = modelVersions.get(modelName);
        if (versions == null || versions.isEmpty()) {
            return Optional.empty();
        }
        
        String latestModelId = versions.get(0).getModelId();
        return getModel(latestModelId);
    }
    
    /**
     * 모델 검색
     */
    public List<RegisteredModel> searchModels(ModelSearchCriteria criteria) {
        return models.values().stream()
            .filter(model -> matchesCriteria(model, criteria))
            .sorted((m1, m2) -> Long.compare(m2.getCreatedAt(), m1.getCreatedAt()))
            .collect(Collectors.toList());
    }
    
    /**
     * 검색 조건 매칭
     */
    private boolean matchesCriteria(RegisteredModel model, ModelSearchCriteria criteria) {
        // 모델명 필터
        if (criteria.getModelName() != null && 
            !model.getModelName().toLowerCase().contains(criteria.getModelName().toLowerCase())) {
            return false;
        }
        
        // 아키텍처 필터
        if (criteria.getArchitecture() != null && 
            !model.getArchitecture().equals(criteria.getArchitecture())) {
            return false;
        }
        
        // 태그 필터
        if (criteria.getTags() != null && !criteria.getTags().isEmpty()) {
            if (model.getTags() == null || 
                !model.getTags().containsAll(criteria.getTags())) {
                return false;
            }
        }
        
        // 성능 필터
        if (criteria.getMinAccuracy() != null && model.getEvaluation() != null) {
            if (model.getEvaluation().getAccuracy() < criteria.getMinAccuracy()) {
                return false;
            }
        }
        
        // 상태 필터
        if (criteria.getStatus() != null && !model.getStatus().equals(criteria.getStatus())) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 모델 활성화 (배포)
     */
    public void activateModel(String modelId, String deploymentName) {
        RegisteredModel model = models.get(modelId);
        if (model == null) {
            throw new IllegalArgumentException("Model not found: " + modelId);
        }
        
        model.setStatus(ModelStatus.ACTIVE);
        activeModels.put(deploymentName, modelId);
        
        log.info("Model activated: {} -> {}", modelId, deploymentName);
    }
    
    /**
     * 모델 비활성화
     */
    public void deactivateModel(String deploymentName) {
        String modelId = activeModels.remove(deploymentName);
        if (modelId != null) {
            RegisteredModel model = models.get(modelId);
            if (model != null) {
                model.setStatus(ModelStatus.INACTIVE);
            }
            log.info("Model deactivated: {} ({})", deploymentName, modelId);
        }
    }
    
    /**
     * 활성 모델 조회
     */
    public Optional<RegisteredModel> getActiveModel(String deploymentName) {
        String modelId = activeModels.get(deploymentName);
        return modelId != null ? getModel(modelId) : Optional.empty();
    }
    
    /**
     * 모델 삭제
     */
    public void deleteModel(String modelId) {
        RegisteredModel model = models.remove(modelId);
        if (model != null) {
            // 버전 목록에서도 제거
            List<ModelVersion> versions = modelVersions.get(model.getModelName());
            if (versions != null) {
                versions.removeIf(v -> v.getModelId().equals(modelId));
                if (versions.isEmpty()) {
                    modelVersions.remove(model.getModelName());
                }
            }
            
            // 활성 모델에서도 제거
            activeModels.entrySet().removeIf(entry -> entry.getValue().equals(modelId));
            
            log.info("Model deleted: {}", modelId);
        }
    }
    
    /**
     * 모델 통계
     */
    public ModelRegistryStatistics getStatistics() {
        int totalModels = models.size();
        int activeModels = (int) models.values().stream()
            .mapToLong(m -> m.getStatus() == ModelStatus.ACTIVE ? 1 : 0)
            .sum();
        
        Map<String, Long> modelsByArchitecture = models.values().stream()
            .collect(Collectors.groupingBy(
                RegisteredModel::getArchitecture,
                Collectors.counting()
            ));
        
        Map<String, Long> modelsByStatus = models.values().stream()
            .collect(Collectors.groupingBy(
                m -> m.getStatus().name(),
                Collectors.counting()
            ));
        
        return ModelRegistryStatistics.builder()
            .totalModels(totalModels)
            .activeModels(activeModels)
            .modelsByArchitecture(modelsByArchitecture)
            .modelsByStatus(modelsByStatus)
            .build();
    }
    
    /**
     * 모델 ID 생성
     */
    private String generateModelId(String modelName) {
        return modelName.toLowerCase().replaceAll("[^a-z0-9]", "_") + 
               "_" + System.currentTimeMillis() + 
               "_" + Integer.toHexString((int)(Math.random() * 0x10000));
    }
    
    /**
     * 모든 모델 목록 조회
     */
    public List<RegisteredModel> getAllModels() {
        return new ArrayList<>(models.values());
    }
    
    /**
     * 모델 버전 목록 조회
     */
    public List<ModelVersion> getModelVersions(String modelName) {
        return modelVersions.getOrDefault(modelName, new ArrayList<>());
    }
}

// 데이터 클래스들
@lombok.Data
@lombok.Builder
class RegisteredModel {
    private String modelId;
    private String modelName;
    private String description;
    private String architecture;
    private String version;
    private String modelPath;
    private String configPath;
    private EvaluationResult evaluation;
    private Set<String> tags;
    private Map<String, Object> metadata;
    private String createdBy;
    private long createdAt;
    private ModelStatus status;
}

@lombok.Data
@lombok.Builder
class ModelVersion {
    private String modelId;
    private String version;
    private long createdAt;
    private EvaluationResult evaluation;
}

@lombok.Data
@lombok.Builder
class ModelRegistrationRequest {
    private String modelName;
    private String description;
    private String architecture;
    private String version;
    private String modelPath;
    private String configPath;
    private EvaluationResult evaluation;
    private Set<String> tags;
    private Map<String, Object> metadata;
    private String createdBy;
}

@lombok.Data
@lombok.Builder
class ModelSearchCriteria {
    private String modelName;
    private String architecture;
    private Set<String> tags;
    private Double minAccuracy;
    private ModelStatus status;
}

@lombok.Data
@lombok.Builder
class ModelRegistryStatistics {
    private int totalModels;
    private int activeModels;
    private Map<String, Long> modelsByArchitecture;
    private Map<String, Long> modelsByStatus;
}

enum ModelStatus {
    REGISTERED,
    ACTIVE,
    INACTIVE,
    DEPRECATED,
    FAILED
}

