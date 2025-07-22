package com.agi.nlp.training.data;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * NLP 훈련 데이터 전처리 및 관리
 * 텍스트 정제, 토큰화, 데이터 증강, 데이터셋 분할 등을 담당
 */
@Slf4j
@Component
public class DataProcessor {
    
    private final TextCleaner textCleaner = new TextCleaner();
    private final Tokenizer tokenizer = new Tokenizer();
    private final DataAugmenter dataAugmenter = new DataAugmenter();
    
    /**
     * 데이터셋 전처리
     */
    public ProcessedDataset processDataset(String datasetPath) {
        log.info("Processing dataset from: {}", datasetPath);
        
        // 1. 원본 데이터 로드
        List<RawTextData> rawData = loadRawData(datasetPath);
        log.info("Loaded {} raw samples", rawData.size());
        
        // 2. 텍스트 정제
        List<CleanedTextData> cleanedData = rawData.parallelStream()
            .map(this::cleanTextData)
            .collect(Collectors.toList());
        
        // 3. 토큰화
        List<TokenizedData> tokenizedData = cleanedData.parallelStream()
            .map(this::tokenizeData)
            .collect(Collectors.toList());
        
        // 4. 데이터 증강 (선택적)
        List<TokenizedData> augmentedData = augmentData(tokenizedData);
        log.info("Augmented data size: {}", augmentedData.size());
        
        // 5. 데이터셋 분할 (Train/Validation/Test)
        DatasetSplit split = splitDataset(augmentedData);
        
        return ProcessedDataset.builder()
            .trainSet(split.getTrainSet())
            .validationSet(split.getValidationSet())
            .testSet(split.getTestSet())
            .vocabulary(buildVocabulary(augmentedData))
            .statistics(calculateStatistics(augmentedData))
            .build();
    }
    
    /**
     * 원본 데이터 로드
     */
    private List<RawTextData> loadRawData(String datasetPath) {
        // 실제 구현에서는 파일 시스템, 데이터베이스, API 등에서 데이터 로드
        // 현재는 Mock 데이터 생성
        
        List<RawTextData> mockData = new ArrayList<>();
        
        // 대화 데이터 샘플
        mockData.add(RawTextData.builder()
            .text("안녕하세요, 오늘 날씨가 어떤가요?")
            .label("greeting")
            .metadata(Map.of("source", "conversation", "user_id", "user1"))
            .build());
        
        mockData.add(RawTextData.builder()
            .text("주문을 취소하고 싶습니다.")
            .label("order_cancel")
            .metadata(Map.of("source", "customer_service", "priority", "high"))
            .build());
        
        mockData.add(RawTextData.builder()
            .text("이 제품의 가격이 얼마인가요?")
            .label("price_inquiry")
            .metadata(Map.of("source", "product_inquiry"))
            .build());
        
        // 실제로는 수천~수만 개의 데이터
        for (int i = 0; i < 1000; i++) {
            mockData.add(generateMockData(i));
        }
        
        return mockData;
    }
    
    /**
     * Mock 데이터 생성
     */
    private RawTextData generateMockData(int index) {
        String[] templates = {
            "사용자 질문 %d번입니다.",
            "도움이 필요합니다 %d.",
            "문제가 발생했습니다 %d.",
            "정보를 알고 싶습니다 %d."
        };
        
        String[] labels = {"question", "help_request", "problem_report", "info_request"};
        
        return RawTextData.builder()
            .text(String.format(templates[index % templates.length], index))
            .label(labels[index % labels.length])
            .metadata(Map.of("generated", true, "index", index))
            .build();
    }
    
    /**
     * 텍스트 정제
     */
    private CleanedTextData cleanTextData(RawTextData rawData) {
        String cleanedText = textCleaner.clean(rawData.getText());
        
        return CleanedTextData.builder()
            .originalText(rawData.getText())
            .cleanedText(cleanedText)
            .label(rawData.getLabel())
            .metadata(rawData.getMetadata())
            .build();
    }
    
    /**
     * 토큰화
     */
    private TokenizedData tokenizeData(CleanedTextData cleanedData) {
        List<String> tokens = tokenizer.tokenize(cleanedData.getCleanedText());
        List<Integer> tokenIds = tokenizer.convertToIds(tokens);
        
        return TokenizedData.builder()
            .originalText(cleanedData.getOriginalText())
            .cleanedText(cleanedData.getCleanedText())
            .tokens(tokens)
            .tokenIds(tokenIds)
            .label(cleanedData.getLabel())
            .metadata(cleanedData.getMetadata())
            .build();
    }
    
    /**
     * 데이터 증강
     */
    private List<TokenizedData> augmentData(List<TokenizedData> originalData) {
        List<TokenizedData> augmentedData = new ArrayList<>(originalData);
        
        // 데이터 증강 비율 (원본의 20% 추가)
        int augmentCount = (int) (originalData.size() * 0.2);
        
        for (int i = 0; i < augmentCount; i++) {
            TokenizedData original = originalData.get(i % originalData.size());
            TokenizedData augmented = dataAugmenter.augment(original);
            augmentedData.add(augmented);
        }
        
        return augmentedData;
    }
    
    /**
     * 데이터셋 분할
     */
    private DatasetSplit splitDataset(List<TokenizedData> data) {
        Collections.shuffle(data); // 데이터 섞기
        
        int totalSize = data.size();
        int trainSize = (int) (totalSize * 0.8);  // 80% 훈련
        int validSize = (int) (totalSize * 0.1);  // 10% 검증
        // 나머지 10% 테스트
        
        List<TokenizedData> trainSet = data.subList(0, trainSize);
        List<TokenizedData> validSet = data.subList(trainSize, trainSize + validSize);
        List<TokenizedData> testSet = data.subList(trainSize + validSize, totalSize);
        
        return DatasetSplit.builder()
            .trainSet(trainSet)
            .validationSet(validSet)
            .testSet(testSet)
            .build();
    }
    
    /**
     * 어휘 사전 구축
     */
    private Vocabulary buildVocabulary(List<TokenizedData> data) {
        Map<String, Integer> tokenCounts = new HashMap<>();
        
        // 토큰 빈도 계산
        for (TokenizedData sample : data) {
            for (String token : sample.getTokens()) {
                tokenCounts.merge(token, 1, Integer::sum);
            }
        }
        
        // 빈도 기준으로 어휘 사전 구축
        List<String> vocabulary = tokenCounts.entrySet().stream()
            .filter(entry -> entry.getValue() >= 2) // 최소 2번 이상 등장
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        
        // 특수 토큰 추가
        vocabulary.add(0, "[PAD]");  // 패딩
        vocabulary.add(1, "[UNK]");  // 미지 토큰
        vocabulary.add(2, "[CLS]");  // 분류 토큰
        vocabulary.add(3, "[SEP]");  // 구분 토큰
        
        return Vocabulary.builder()
            .tokens(vocabulary)
            .tokenToId(createTokenToIdMap(vocabulary))
            .size(vocabulary.size())
            .build();
    }
    
    /**
     * 토큰-ID 매핑 생성
     */
    private Map<String, Integer> createTokenToIdMap(List<String> vocabulary) {
        Map<String, Integer> tokenToId = new HashMap<>();
        for (int i = 0; i < vocabulary.size(); i++) {
            tokenToId.put(vocabulary.get(i), i);
        }
        return tokenToId;
    }
    
    /**
     * 데이터셋 통계 계산
     */
    private DatasetStatistics calculateStatistics(List<TokenizedData> data) {
        int totalSamples = data.size();
        
        // 텍스트 길이 통계
        List<Integer> lengths = data.stream()
            .mapToInt(sample -> sample.getTokens().size())
            .boxed()
            .collect(Collectors.toList());
        
        double avgLength = lengths.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        int maxLength = lengths.stream().mapToInt(Integer::intValue).max().orElse(0);
        int minLength = lengths.stream().mapToInt(Integer::intValue).min().orElse(0);
        
        // 라벨 분포
        Map<String, Long> labelDistribution = data.stream()
            .collect(Collectors.groupingBy(
                TokenizedData::getLabel,
                Collectors.counting()
            ));
        
        return DatasetStatistics.builder()
            .totalSamples(totalSamples)
            .averageLength(avgLength)
            .maxLength(maxLength)
            .minLength(minLength)
            .labelDistribution(labelDistribution)
            .build();
    }
}

/**
 * 텍스트 정제 클래스
 */
class TextCleaner {
    public String clean(String text) {
        if (text == null) return "";
        
        return text
            .trim()                           // 앞뒤 공백 제거
            .replaceAll("\\s+", " ")         // 연속 공백을 하나로
            .replaceAll("[^\\w\\s가-힣]", "") // 특수문자 제거 (한글 유지)
            .toLowerCase();                   // 소문자 변환
    }
}

/**
 * 토크나이저 클래스
 */
class Tokenizer {
    public List<String> tokenize(String text) {
        // 간단한 공백 기반 토큰화 (실제로는 BPE, WordPiece 등 사용)
        return Arrays.asList(text.split("\\s+"));
    }
    
    public List<Integer> convertToIds(List<String> tokens) {
        // 실제로는 어휘 사전을 사용하여 변환
        return tokens.stream()
            .mapToInt(String::hashCode)
            .map(Math::abs)
            .boxed()
            .collect(Collectors.toList());
    }
}

/**
 * 데이터 증강 클래스
 */
class DataAugmenter {
    public TokenizedData augment(TokenizedData original) {
        // 간단한 동의어 치환 (실제로는 더 정교한 증강 기법 사용)
        List<String> augmentedTokens = new ArrayList<>(original.getTokens());
        
        // 랜덤하게 일부 토큰을 유사한 토큰으로 교체
        Random random = new Random();
        for (int i = 0; i < augmentedTokens.size(); i++) {
            if (random.nextDouble() < 0.1) { // 10% 확률로 교체
                augmentedTokens.set(i, augmentedTokens.get(i) + "_aug");
            }
        }
        
        return TokenizedData.builder()
            .originalText(original.getOriginalText())
            .cleanedText(original.getCleanedText() + " [augmented]")
            .tokens(augmentedTokens)
            .tokenIds(convertToIds(augmentedTokens))
            .label(original.getLabel())
            .metadata(Map.of("augmented", true))
            .build();
    }
    
    private List<Integer> convertToIds(List<String> tokens) {
        return tokens.stream()
            .mapToInt(String::hashCode)
            .map(Math::abs)
            .boxed()
            .collect(Collectors.toList());
    }
}

// 데이터 클래스들
@lombok.Data
@lombok.Builder
class RawTextData {
    private String text;
    private String label;
    private Map<String, Object> metadata;
}

@lombok.Data
@lombok.Builder
class CleanedTextData {
    private String originalText;
    private String cleanedText;
    private String label;
    private Map<String, Object> metadata;
}

@lombok.Data
@lombok.Builder
class TokenizedData {
    private String originalText;
    private String cleanedText;
    private List<String> tokens;
    private List<Integer> tokenIds;
    private String label;
    private Map<String, Object> metadata;
}

@lombok.Data
@lombok.Builder
class ProcessedDataset {
    private List<TokenizedData> trainSet;
    private List<TokenizedData> validationSet;
    private List<TokenizedData> testSet;
    private Vocabulary vocabulary;
    private DatasetStatistics statistics;
}

@lombok.Data
@lombok.Builder
class DatasetSplit {
    private List<TokenizedData> trainSet;
    private List<TokenizedData> validationSet;
    private List<TokenizedData> testSet;
}

@lombok.Data
@lombok.Builder
class Vocabulary {
    private List<String> tokens;
    private Map<String, Integer> tokenToId;
    private int size;
}

@lombok.Data
@lombok.Builder
class DatasetStatistics {
    private int totalSamples;
    private double averageLength;
    private int maxLength;
    private int minLength;
    private Map<String, Long> labelDistribution;
}

