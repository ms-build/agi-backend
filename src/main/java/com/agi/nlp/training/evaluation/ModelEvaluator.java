package com.agi.nlp.training.evaluation;

import com.agi.nlp.training.data.TokenizedData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * NLP 모델 평가 시스템
 * 다양한 메트릭을 사용하여 모델 성능을 평가
 */
@Slf4j
@Component
public class ModelEvaluator {
    
    /**
     * 모델 평가 실행
     */
    public EvaluationResult evaluate(Object model, List<TokenizedData> testSet) {
        log.info("Evaluating model on {} test samples", testSet.size());
        
        // 1. 예측 수행
        List<PredictionResult> predictions = performPredictions(model, testSet);
        
        // 2. 분류 메트릭 계산
        ClassificationMetrics classificationMetrics = calculateClassificationMetrics(predictions);
        
        // 3. 언어 모델 메트릭 계산 (해당하는 경우)
        LanguageModelMetrics languageMetrics = calculateLanguageModelMetrics(predictions);
        
        // 4. 추가 메트릭 계산
        AdditionalMetrics additionalMetrics = calculateAdditionalMetrics(predictions);
        
        return EvaluationResult.builder()
            .totalSamples(testSet.size())
            .accuracy(classificationMetrics.getAccuracy())
            .precision(classificationMetrics.getPrecision())
            .recall(classificationMetrics.getRecall())
            .f1Score(classificationMetrics.getF1Score())
            .classificationMetrics(classificationMetrics)
            .languageModelMetrics(languageMetrics)
            .additionalMetrics(additionalMetrics)
            .evaluationTime(System.currentTimeMillis())
            .build();
    }
    
    /**
     * 모델 예측 수행
     */
    private List<PredictionResult> performPredictions(Object model, List<TokenizedData> testSet) {
        List<PredictionResult> predictions = new ArrayList<>();
        
        for (TokenizedData sample : testSet) {
            // 실제 구현에서는 모델의 predict 메서드 호출
            // 현재는 Mock 예측 수행
            String predictedLabel = mockPredict(sample);
            double confidence = Math.random() * 0.3 + 0.7; // 0.7-1.0 사이의 신뢰도
            
            predictions.add(PredictionResult.builder()
                .input(sample.getCleanedText())
                .trueLabel(sample.getLabel())
                .predictedLabel(predictedLabel)
                .confidence(confidence)
                .tokens(sample.getTokens())
                .build());
        }
        
        return predictions;
    }
    
    /**
     * Mock 예측 (실제로는 모델 추론)
     */
    private String mockPredict(TokenizedData sample) {
        // 실제 라벨과 80% 확률로 일치하도록 Mock 예측
        if (Math.random() < 0.8) {
            return sample.getLabel();
        } else {
            // 다른 라벨 중 랜덤 선택
            String[] possibleLabels = {"greeting", "question", "help_request", "problem_report", "info_request", "order_cancel", "price_inquiry"};
            List<String> otherLabels = Arrays.stream(possibleLabels)
                .filter(label -> !label.equals(sample.getLabel()))
                .collect(Collectors.toList());
            return otherLabels.get((int)(Math.random() * otherLabels.size()));
        }
    }
    
    /**
     * 분류 메트릭 계산
     */
    private ClassificationMetrics calculateClassificationMetrics(List<PredictionResult> predictions) {
        // 전체 정확도
        long correctPredictions = predictions.stream()
            .mapToLong(p -> p.getTrueLabel().equals(p.getPredictedLabel()) ? 1 : 0)
            .sum();
        double accuracy = (double) correctPredictions / predictions.size();
        
        // 라벨별 메트릭 계산
        Set<String> allLabels = predictions.stream()
            .flatMap(p -> Arrays.stream(new String[]{p.getTrueLabel(), p.getPredictedLabel()}))
            .collect(Collectors.toSet());
        
        Map<String, LabelMetrics> labelMetrics = new HashMap<>();
        double totalPrecision = 0.0;
        double totalRecall = 0.0;
        double totalF1 = 0.0;
        
        for (String label : allLabels) {
            LabelMetrics metrics = calculateLabelMetrics(predictions, label);
            labelMetrics.put(label, metrics);
            totalPrecision += metrics.getPrecision();
            totalRecall += metrics.getRecall();
            totalF1 += metrics.getF1Score();
        }
        
        // 매크로 평균
        double macroPrecision = totalPrecision / allLabels.size();
        double macroRecall = totalRecall / allLabels.size();
        double macroF1 = totalF1 / allLabels.size();
        
        // 혼동 행렬
        Map<String, Map<String, Integer>> confusionMatrix = calculateConfusionMatrix(predictions, allLabels);
        
        return ClassificationMetrics.builder()
            .accuracy(accuracy)
            .precision(macroPrecision)
            .recall(macroRecall)
            .f1Score(macroF1)
            .labelMetrics(labelMetrics)
            .confusionMatrix(confusionMatrix)
            .supportByLabel(calculateSupport(predictions))
            .build();
    }
    
    /**
     * 라벨별 메트릭 계산
     */
    private LabelMetrics calculateLabelMetrics(List<PredictionResult> predictions, String label) {
        int truePositive = 0;
        int falsePositive = 0;
        int falseNegative = 0;
        
        for (PredictionResult prediction : predictions) {
            boolean actualIsLabel = prediction.getTrueLabel().equals(label);
            boolean predictedIsLabel = prediction.getPredictedLabel().equals(label);
            
            if (actualIsLabel && predictedIsLabel) {
                truePositive++;
            } else if (!actualIsLabel && predictedIsLabel) {
                falsePositive++;
            } else if (actualIsLabel && !predictedIsLabel) {
                falseNegative++;
            }
        }
        
        double precision = truePositive + falsePositive > 0 ? 
            (double) truePositive / (truePositive + falsePositive) : 0.0;
        double recall = truePositive + falseNegative > 0 ? 
            (double) truePositive / (truePositive + falseNegative) : 0.0;
        double f1Score = precision + recall > 0 ? 
            2 * (precision * recall) / (precision + recall) : 0.0;
        
        return LabelMetrics.builder()
            .label(label)
            .precision(precision)
            .recall(recall)
            .f1Score(f1Score)
            .truePositive(truePositive)
            .falsePositive(falsePositive)
            .falseNegative(falseNegative)
            .support(truePositive + falseNegative)
            .build();
    }
    
    /**
     * 혼동 행렬 계산
     */
    private Map<String, Map<String, Integer>> calculateConfusionMatrix(
            List<PredictionResult> predictions, Set<String> labels) {
        
        Map<String, Map<String, Integer>> matrix = new HashMap<>();
        
        // 행렬 초기화
        for (String trueLabel : labels) {
            matrix.put(trueLabel, new HashMap<>());
            for (String predLabel : labels) {
                matrix.get(trueLabel).put(predLabel, 0);
            }
        }
        
        // 예측 결과로 행렬 채우기
        for (PredictionResult prediction : predictions) {
            String trueLabel = prediction.getTrueLabel();
            String predLabel = prediction.getPredictedLabel();
            
            if (matrix.containsKey(trueLabel) && matrix.get(trueLabel).containsKey(predLabel)) {
                matrix.get(trueLabel).merge(predLabel, 1, Integer::sum);
            }
        }
        
        return matrix;
    }
    
    /**
     * 라벨별 지원 수 계산
     */
    private Map<String, Integer> calculateSupport(List<PredictionResult> predictions) {
        return predictions.stream()
            .collect(Collectors.groupingBy(
                PredictionResult::getTrueLabel,
                Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
            ));
    }
    
    /**
     * 언어 모델 메트릭 계산
     */
    private LanguageModelMetrics calculateLanguageModelMetrics(List<PredictionResult> predictions) {
        // 실제로는 BLEU, ROUGE, Perplexity 등 계산
        // 현재는 Mock 값 반환
        
        return LanguageModelMetrics.builder()
            .perplexity(15.2) // Mock 값
            .bleuScore(0.75)  // Mock 값
            .rougeL(0.68)     // Mock 값
            .build();
    }
    
    /**
     * 추가 메트릭 계산
     */
    private AdditionalMetrics calculateAdditionalMetrics(List<PredictionResult> predictions) {
        // 평균 신뢰도
        double avgConfidence = predictions.stream()
            .mapToDouble(PredictionResult::getConfidence)
            .average()
            .orElse(0.0);
        
        // 고신뢰도 예측 비율 (0.9 이상)
        long highConfidencePredictions = predictions.stream()
            .mapToLong(p -> p.getConfidence() >= 0.9 ? 1 : 0)
            .sum();
        double highConfidenceRatio = (double) highConfidencePredictions / predictions.size();
        
        // 평균 토큰 길이
        double avgTokenLength = predictions.stream()
            .mapToInt(p -> p.getTokens().size())
            .average()
            .orElse(0.0);
        
        return AdditionalMetrics.builder()
            .averageConfidence(avgConfidence)
            .highConfidenceRatio(highConfidenceRatio)
            .averageTokenLength(avgTokenLength)
            .totalPredictions(predictions.size())
            .build();
    }
}

// 평가 결과 데이터 클래스들
@lombok.Data
@lombok.Builder
class EvaluationResult {
    private int totalSamples;
    private double accuracy;
    private double precision;
    private double recall;
    private double f1Score;
    private ClassificationMetrics classificationMetrics;
    private LanguageModelMetrics languageModelMetrics;
    private AdditionalMetrics additionalMetrics;
    private long evaluationTime;
}

@lombok.Data
@lombok.Builder
class PredictionResult {
    private String input;
    private String trueLabel;
    private String predictedLabel;
    private double confidence;
    private List<String> tokens;
}

@lombok.Data
@lombok.Builder
class ClassificationMetrics {
    private double accuracy;
    private double precision;
    private double recall;
    private double f1Score;
    private Map<String, LabelMetrics> labelMetrics;
    private Map<String, Map<String, Integer>> confusionMatrix;
    private Map<String, Integer> supportByLabel;
}

@lombok.Data
@lombok.Builder
class LabelMetrics {
    private String label;
    private double precision;
    private double recall;
    private double f1Score;
    private int truePositive;
    private int falsePositive;
    private int falseNegative;
    private int support;
}

@lombok.Data
@lombok.Builder
class LanguageModelMetrics {
    private double perplexity;
    private double bleuScore;
    private double rougeL;
}

@lombok.Data
@lombok.Builder
class AdditionalMetrics {
    private double averageConfidence;
    private double highConfidenceRatio;
    private double averageTokenLength;
    private int totalPredictions;
}

