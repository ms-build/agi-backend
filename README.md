# AGI Backend

🧠 **Artificial General Intelligence Backend System**

Spring Boot 기반의 완전한 AGI 시스템으로, 신경망 모델 관리, 자연어 처리, 대화형 AI, 지식 전이 등의 기능을 제공합니다.

## 🚀 주요 기능

### 🧠 Neural Network Infrastructure
- **모델 관리**: 중앙화된 신경망 모델 관리 시스템
- **추론 엔진**: 동기/비동기 추론 지원
- **모델 메타데이터**: 성능 지표 및 통계 추적

### 🗣️ Natural Language Processing
- **텍스트 임베딩**: 텍스트를 벡터로 변환
- **텍스트 분류**: 의도 및 감정 분석
- **대화 관리**: 컨텍스트 유지 및 히스토리 관리

### 🔄 Knowledge Transfer
- **도메인 간 지식 전이**: 다양한 전이 학습 방법 지원
- **특징 추출**: 학습된 특징의 재사용
- **지식 증류**: 교사-학생 모델 간 지식 전달

### 🛠️ Infrastructure
- **Docker 지원**: 완전한 컨테이너화
- **모니터링**: Prometheus + Grafana
- **검색**: Elasticsearch 통합
- **캐싱**: Redis 지원
- **스트리밍**: Kafka 이벤트 처리

## 🏗️ 기술 스택

### Core Framework
- **Spring Boot 3.4.5** - 메인 프레임워크
- **MyBatis 3.0.3** - 데이터베이스 ORM
- **H2 Database** - 개발용 인메모리 DB
- **Java 17** - 프로그래밍 언어

### Machine Learning & AI
- **TensorFlow** - 딥러닝 프레임워크
- **DeepLearning4J** - Java 기반 딥러닝
- **Apache Spark** - 대용량 데이터 처리
- **Stanford CoreNLP** - 자연어 처리
- **LangChain4j** - LLM 통합

### Infrastructure & DevOps
- **Docker & Docker Compose** - 컨테이너화
- **Redis** - 캐싱 및 세션 관리
- **Elasticsearch** - 검색 및 벡터 저장
- **Apache Kafka** - 이벤트 스트리밍
- **Prometheus & Grafana** - 모니터링
- **MinIO** - 객체 저장소

## 🚀 빠른 시작

### 1. 간단한 실행 (H2 DB만 사용)
```bash
./start.sh
```

### 2. 완전한 인프라와 함께 실행
```bash
# 인프라 서비스 시작
docker-compose up -d

# 애플리케이션 시작
./start.sh
```

### 3. 정지
```bash
./stop.sh
```

## 🔗 접속 정보

### 애플리케이션
- **메인 API**: http://localhost:8080
- **H2 Console**: http://localhost:8080/h2-console
- **Health Check**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/prometheus

### 인프라 서비스
- **Elasticsearch**: http://localhost:9200
- **Kibana**: http://localhost:5601
- **Redis**: localhost:6379
- **Kafka**: localhost:9092
- **MinIO**: http://localhost:9001 (admin/admin123)
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin123)

## 📊 API 엔드포인트

### User Management
- `GET /api/users` - 전체 사용자 조회
- `POST /api/users` - 사용자 생성
- `GET /api/users/{id}` - 특정 사용자 조회
- `PUT /api/users/{id}` - 사용자 수정
- `DELETE /api/users/{id}` - 사용자 삭제

### Neural Network Models
- `GET /api/models` - 등록된 모델 목록
- `POST /api/models/{modelId}/predict` - 모델 추론 실행
- `GET /api/models/{modelId}/metadata` - 모델 메타데이터
- `GET /api/models/health` - 시스템 상태

### Conversation Management
- `POST /api/conversations` - 새 대화 시작
- `POST /api/conversations/{id}/turns` - 대화 턴 추가
- `GET /api/conversations/{id}/history` - 대화 히스토리

### Knowledge Transfer
- `POST /api/transfer` - 지식 전이 시작
- `GET /api/transfer/{sessionId}` - 전이 세션 상태
- `GET /api/transfer/sessions` - 활성 세션 목록

## 🏗️ 프로젝트 구조

```
src/main/java/com/agi/
├── neural/          # 신경망 코어 시스템
│   ├── core/        # 기본 인터페이스 및 관리자
│   ├── models/      # 모델 구현체
│   └── inference/   # 추론 엔진
├── nlp/             # 자연어 처리
│   ├── processing/  # 텍스트 처리
│   ├── analysis/    # 텍스트 분석
│   ├── generation/  # 텍스트 생성
│   └── understanding/ # 자연어 이해
├── learning/        # 학습 시스템
│   ├── transfer/    # 지식 전이
│   ├── reinforcement/ # 강화 학습
│   └── adaptive/    # 적응형 학습
├── multimodal/      # 멀티모달 처리
│   ├── image/       # 이미지 처리
│   ├── audio/       # 오디오 처리
│   └── video/       # 비디오 처리
└── common/          # 공통 유틸리티
```

## 🧪 개발 및 테스트

### 빌드
```bash
./gradlew clean build
```

### 테스트 실행
```bash
./gradlew test
```

### Docker 이미지 빌드
```bash
docker build -t agi-backend .
```

## 📈 모니터링

시스템 상태는 다음 엔드포인트에서 확인할 수 있습니다:

- **Health**: `/actuator/health`
- **Metrics**: `/actuator/metrics`
- **Prometheus**: `/actuator/prometheus`

Grafana 대시보드에서 실시간 모니터링이 가능합니다.

## 🤝 기여하기

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다.

## 🆘 지원

문제가 발생하면 GitHub Issues를 통해 문의해주세요.

