# AGI Project - MyBatis Edition

Spring Boot + MyBatis + H2 Database 기반의 AGI 프로젝트입니다.

## 기술 스택

- **Backend**: Spring Boot 3.4.5
- **Database**: H2 Database (In-Memory)
- **ORM**: MyBatis 3.0.3
- **Build Tool**: Gradle 8.5
- **Java Version**: 17
- **Testing**: JUnit 5, Mockito, AssertJ

## 아키텍처 특징

- **tb_ 접두사**: 모든 테이블명에 tb_ 접두사 사용으로 H2 예약어 충돌 방지
- **Repository 패턴**: MyBatis Mapper를 Repository로 네이밍하여 DDD 원칙 적용
- **Service-ServiceImpl 구조**: 인터페이스와 구현체 분리
- **Builder 패턴**: Lombok을 활용한 불변 객체 생성
- **ResponseEntity**: 일관된 API 응답 형태
- **CORS 설정**: Vue.js 프론트엔드 연동 준비

## 프로젝트 구조

```
src/main/java/com/agi/
├── common/
│   ├── config/          # 설정 클래스
│   ├── exception/       # 예외 처리
│   └── response/        # 응답 형태
├── user/
│   ├── vo/             # Value Object
│   ├── repository/     # Repository 인터페이스
│   ├── service/        # Service 인터페이스 및 구현체
│   └── controller/     # REST Controller
└── [other domains]/

src/main/resources/
├── mapper/             # MyBatis XML 매퍼
├── db/                 # DB 스키마 및 초기 데이터
└── application.yml     # 설정 파일
```

## 데이터베이스 스키마

### 주요 테이블
- `tb_users`: 사용자 정보
- `tb_roles`: 역할 정보
- `tb_permissions`: 권한 정보
- `tb_user_roles`: 사용자-역할 매핑
- `tb_role_permissions`: 역할-권한 매핑
- `tb_conversation`: 대화 정보
- `tb_message`: 메시지 정보
- `tb_knowledge`: 지식 베이스
- `tb_tool`: 도구 정보
- `tb_plan`: 계획 정보
- `tb_sandbox`: 샌드박스 환경

## 실행 방법

### 1. 프로젝트 빌드
```bash
./gradlew build
```

### 2. 애플리케이션 실행
```bash
./gradlew bootRun
```

### 3. H2 콘솔 접속
- URL: http://localhost:8080/h2-console
- JDBC URL: jdbc:h2:mem:agidb
- Username: sa
- Password: (비워둠)

## API 엔드포인트

### User API
- `GET /api/users` - 모든 사용자 조회
- `GET /api/users/{id}` - ID로 사용자 조회
- `GET /api/users/username/{username}` - 사용자명으로 조회
- `GET /api/users/active` - 활성 사용자 조회
- `POST /api/users` - 새 사용자 생성
- `PUT /api/users/{id}` - 사용자 정보 수정
- `DELETE /api/users/{id}` - 사용자 삭제
- `GET /api/users/check-username/{username}` - 사용자명 중복 확인
- `GET /api/users/check-email/{email}` - 이메일 중복 확인
- `GET /api/users/count` - 전체 사용자 수 조회

## 테스트

```bash
./gradlew test
```

## 개발 가이드

### 새 도메인 추가 시
1. `src/main/java/com/agi/{domain}/` 디렉토리 생성
2. VO, Repository, Service, Controller 클래스 작성
3. `src/main/resources/mapper/{Domain}Mapper.xml` 매퍼 파일 작성
4. 테스트 코드 작성

### 코딩 컨벤션
- Lombok @Builder 패턴 사용
- Setter 메서드 지양
- Repository 패턴 네이밍
- ResponseEntity 사용
- 테스트 코드 필수 작성

## 향후 계획

- [ ] 나머지 도메인 구현 (Conversation, Knowledge, Tool, Plan, Sandbox)
- [ ] Spring Security 적용
- [ ] JWT 인증 구현
- [ ] Vue.js 프론트엔드 연동
- [ ] Docker 컨테이너화
- [ ] CI/CD 파이프라인 구축

## 라이선스

MIT License

