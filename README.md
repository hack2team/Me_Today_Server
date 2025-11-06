# Self Journey API

**나를 기록하다** - 자기 성찰과 성장을 위한 백엔드 API

## 🎯 프로젝트 개요

Self Journey는 하루 한 질문을 통해 자신을 돌아보고, 누적 답변으로 개인 리포트를 쌓아가는 자기 성찰 서비스입니다.

### 주요 기능

- ✅ **하루 질문/답변**: 매일 맞춤형 질문 제공 및 답변 기록
- 🤖 **AI 자동 분석**: Gemini AI가 답변 제출 시 자동으로 4가지 항목 분석
  - 장단점 (strengths/weaknesses)
  - 가치관 (values)
  - 개선사항 (improvement suggestions)
  - 관계도 (relationship map)
- 📊 **자동 리포트**: 누적 답변을 기반으로 한 AI 기반 자기 성찰 리포트 생성
- 🗓️ **주기 설정**: 6/12/24개월 중 원하는 질문 주기를 선택
- 🎯 **관심사/페르소나**: 사용자 맞춤 질문 제공
- 📈 **진행도 추적**: 연속 답변일, 자기인식 레벨 관리
- 🔄 **과거 비교**: 같은 질문에 대한 과거 답변과 현재 답변 비교

## 🛠 기술 스택

- **Language**: Kotlin 1.9.22
- **Framework**: Ktor 2.3.7
- **Database**: MySQL 8.0
- **ORM**: Exposed
- **Migration**: Flyway
- **AI**: Google Gemini 2.0 Flash API
- **Documentation**: OpenAPI 3.0 + Swagger UI
- **Build**: Gradle Kotlin DSL
- **Container**: Docker + Docker Compose

## 🚀 빠른 시작

### 사전 요구사항

- Docker Desktop
- Git

### 1. 프로젝트 클론

```bash
git clone <repository-url>
cd hack2team
```

### 2. 환경 변수 설정

```bash
cp .env.example .env
```

`.env` 파일 내용 (기본값 사용 가능):

```env
# Database Configuration
MYSQL_DATABASE=self_journey
MYSQL_USER=appuser
MYSQL_PASSWORD=apppass
MYSQL_ROOT_PASSWORD=rootpass

# Application Configuration
APP_PORT=8080
DB_HOST=mysql
DB_PORT=3306
DB_NAME=self_journey
DB_USER=appuser
DB_PASS=apppass

```

### 3. 서비스 시작

```bash
docker compose up -d --build
```

이 명령어 하나로:
- MySQL 데이터베이스 생성 및 시작
- Flyway 마이그레이션 자동 실행
- 시드 데이터 자동 삽입
- API 서버 시작

### 4. 서비스 확인

- **API 서버**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger
- **OpenAPI Spec**: http://localhost:8080/openapi
- **Health Check**: http://localhost:8080/health
- **Adminer (DB 관리)**: http://localhost:8081
  - System: MySQL
  - Server: mysql
  - Username: appuser
  - Password: apppass
  - Database: self_journey

### 5. 서비스 중지

```bash
docker compose down
```

데이터까지 삭제하려면:

```bash
docker compose down -v
```

## 📡 API 엔드포인트

### Health Check

- `GET /health` - 서버 및 데이터베이스 상태 확인

### Users

- `GET /api/users` - 모든 사용자 조회
- `GET /api/users/{id}` - 특정 사용자 조회
- `POST /api/users` - 사용자 생성
- `PUT /api/users/{id}` - 사용자 정보 수정
- `DELETE /api/users/{id}` - 사용자 삭제

### Questions

- `GET /api/questions` - 모든 질문 조회
- `GET /api/questions/today?userId={userId}` - 다음 순번의 질문과 진행도 조회
- `GET /api/questions/{id}` - 특정 질문 조회
- `GET /api/questions/interest/{interestId}` - 관심사별 질문 조회
- `POST /api/questions` - 질문 생성

### Answers

- `GET /api/answers/user/{userId}` - 사용자의 모든 답변 조회
- `GET /api/answers/question/{questionId}` - 특정 질문에 대한 모든 답변 조회
- `GET /api/answers/user/{userId}/question/{questionId}` - 동일 질문에 대한 사용자 과거 답변 조회
- `POST /api/answers` - 답변 저장
- `GET /api/answers/report/{userId}` - 누적 자기 성찰 리포트 조회

### Progress

- `GET /api/progress/{userId}` - 사용자 진행도 조회

## 🎯 주요 비즈니스 로직

### 1. 하루 질문 시스템

- 365개의 고정 질문을 사용자별로 순서대로 제공
- `/api/questions/today` 응답에서 누적 답변 수와 남은 질문 수 확인
- 연속 답변일 계산 및 레벨 업 시스템

### 2. 자동 리포트 생성

답변이 저장될 때마다 시스템이 전체 히스토리를 훑어보고 아래 정보를 갱신합니다:
- 누적 답변 수와 고유 질문 수
- 자주 언급된 키워드와 강조된 관계
- 최근 답변 목록과 간단한 하이라이트/제안

최신 리포트는 `GET /api/answers/report/{userId}` 또는 `GET /api/answers/insights/{userId}`로 확인할 수 있으며, `GET /api/answers/user/{userId}/question/{questionId}`에서 동일한 질문에 대한 과거 답변을 비교할 수 있습니다.

### 3. 과거 답변 비교

동일한 질문에 대한 이전 답변은 응답 본문(`prevAnswer`)으로 반환되므로 사용자가 직접 변화를 비교할 수 있습니다.

## 🗄 데이터베이스 스키마

### 주요 테이블

- **users**: 사용자 정보
- **interests**: 관심사
- **goal_personas**: 목표 페르소나
- **questions**: 질문 데이터
- **answers**: 답변 데이터
- **reports**: 사용자 리포트
- **user_progress**: 사용자 진행도
- **notifications**: 알림
- **answer_comparisons**: 답변 비교 기록

자세한 스키마는 `src/main/resources/db/migration/V1__init.sql` 참조

## 🧪 테스트

### 로컬 테스트 실행

```bash
./gradlew test
```

### Swagger UI를 통한 API 테스트

1. http://localhost:8080/swagger 접속
2. 원하는 엔드포인트 선택
3. "Try it out" 클릭
4. 파라미터 입력 후 "Execute"

### 예제 요청

#### 1. 사용자 생성

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "홍길동",
    "age": 28,
    "email": "hong@example.com",
    "planDurationMonths": 6
  }'
```

> `planDurationMonths`는 6, 12, 24 중 하나를 선택하며, 선택한 기간만큼 질문이 순환됩니다.

#### 2. 오늘의 질문 조회

```bash
curl http://localhost:8080/api/questions/today?userId=1
```

#### 3. 답변 제출

```bash
curl -X POST http://localhost:8080/api/answers \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "questionId": 1,
    "content": "오늘은 새로운 기술을 배워서 뿌듯했습니다."
  }'
```

응답 예시:
```json
{
  "success": true,
  "data": {
    "answerId": 1,
    "prevAnswer": null,
    "savedAt": "2025-01-01T12:00:00"
  }
}
```

#### 4. 특정 질문 과거 답변 조회

```bash
curl http://localhost:8080/api/answers/user/1/question/1
```

#### 5. 누적 리포트 조회

```bash
curl http://localhost:8080/api/answers/report/1
```

#### 6. 진행도 조회

```bash
curl http://localhost:8080/api/progress/1
```

## 🔧 개발 가이드

### 로컬 개발 환경 설정

1. **MySQL 설치 및 데이터베이스 생성**

```bash
mysql -u root -p
CREATE DATABASE self_journey CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. **환경 변수 설정**

로컬 개발 시에는 `src/main/resources/application.conf` 수정:

```hocon
database {
    host = "localhost"
    port = "3306"
    name = "self_journey"
    user = "root"
    password = "your_password"
}
```

3. **애플리케이션 실행**

```bash
./gradlew run
```

### 새로운 마이그레이션 추가

1. `src/main/resources/db/migration/` 에 새 파일 생성
2. 파일명 규칙: `V{version}__{description}.sql`
   - 예: `V3__add_bookmarks_table.sql`
3. 재시작 시 자동 실행

### 새로운 엔드포인트 추가

1. DTO 정의: `src/main/kotlin/com/selfjourney/domain/DTOs.kt`
2. Repository 구현: `src/main/kotlin/com/selfjourney/repository/`
3. Service 구현: `src/main/kotlin/com/selfjourney/service/`
4. Route 추가: `src/main/kotlin/com/selfjourney/routes/`
5. OpenAPI 문서 업데이트: `src/main/resources/openapi/documentation.yaml`

## 📊 모니터링 및 로깅

### 로그 확인

```bash
# 실시간 로그
docker compose logs -f app

# 특정 서비스 로그
docker compose logs -f mysql
```

### 데이터베이스 접근

**Adminer 사용** (권장):
- http://localhost:8081 접속
- GUI로 편리하게 데이터 확인 및 관리

**MySQL CLI 사용**:
```bash
docker compose exec mysql mysql -u appuser -p self_journey
# Password: apppass
```

## 🔐 보안 고려사항

### 프로덕션 배포 시 필수 변경사항

1. **환경 변수 보안**
   - 모든 비밀번호 변경
   - `.env` 파일을 Git에 커밋하지 않음 (`.gitignore`에 포함됨)
   - 실제 서버에서는 Docker Secrets 또는 환경 변수 관리 시스템 사용

2. **데이터베이스 보안**
   - 강력한 비밀번호 사용
   - 외부 접근 제한 (포트 3306 노출 제거)
   - SSL/TLS 연결 활성화

3. **API 보안**
   - JWT 인증 활성화 (현재 기본 틀만 구현됨)
   - Rate limiting 추가
   - HTTPS 사용 (리버스 프록시 설정)

## 🐛 트러블슈팅

### 포트 충돌

**증상**: "port is already allocated" 오류

**해결**:
```bash
# 실행 중인 컨테이너 확인
docker ps

# 포트 사용 확인
lsof -i :8080
lsof -i :3306

# .env 파일에서 포트 변경
APP_PORT=8090
```

### 데이터베이스 연결 실패

**증상**: "Communications link failure" 오류

**해결**:
```bash
# MySQL 컨테이너 상태 확인
docker compose logs mysql

# 데이터베이스 재시작
docker compose restart mysql

# 헬스체크 대기 후 앱 재시작
docker compose restart app
```

### Flyway 마이그레이션 실패

**증상**: "Migration checksum mismatch" 오류

**해결**:
```bash
# 개발 환경: 데이터베이스 초기화
docker compose down -v
docker compose up -d

# 프로덕션: Flyway repair 실행
docker compose exec app ./gradlew flywayRepair
```

## 📁 프로젝트 구조

```
.
├── build.gradle.kts           # Gradle 빌드 설정
├── settings.gradle.kts        # Gradle 프로젝트 설정
├── Dockerfile                 # 멀티스테이지 Docker 빌드
├── docker-compose.yml         # Docker Compose 설정
├── .env.example               # 환경 변수 예제
├── README.md                  # 이 파일
└── src
    ├── main
    │   ├── kotlin/com/selfjourney
    │   │   ├── Application.kt           # 메인 애플리케이션
    │   │   ├── domain/
    │   │   │   ├── Models.kt           # Exposed 테이블 정의
    │   │   │   └── DTOs.kt             # 데이터 전송 객체
    │   │   ├── repository/             # 데이터 접근 계층
    │   │   ├── service/                # 비즈니스 로직
    │   │   ├── routes/                 # API 라우트
    │   │   └── plugins/                # Ktor 플러그인 설정
    │   └── resources
    │       ├── application.conf        # 애플리케이션 설정
    │       ├── logback.xml            # 로깅 설정
    │       ├── openapi/               # OpenAPI 문서
    │       └── db/migration/          # Flyway 마이그레이션
    └── test
        └── kotlin/                    # 테스트 코드
```

## 🤝 기여 가이드

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 라이선스

This project is licensed under the MIT License.

## 📞 문의

프로젝트 관련 문의사항이 있으시면 이슈를 등록해주세요.

---

**Built with ❤️ using Kotlin + Ktor + Docker**
