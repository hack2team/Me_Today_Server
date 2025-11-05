# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Self Journey (나를 기록하다) is a Kotlin/Ktor-based REST API for a daily self-reflection journaling service with AI-powered analysis. Users answer daily questions, and the Gemini AI provides insights including summaries, keywords, strengths, weaknesses, path to ideal self, and relationship mapping.

## Tech Stack

- **Framework**: Ktor 2.3.7 (Kotlin 1.9.22)
- **Database**: MySQL 8.0 with Exposed ORM
- **Migration**: Flyway
- **AI**: Google Gemini API (direct HTTP calls, no SDK)
- **Build**: Gradle 8.5 with Kotlin DSL
- **Deployment**: Docker multi-stage builds with Docker Compose

## Essential Development Commands

### Docker Operations (Primary Development Method)
```bash
# Full rebuild with clean slate
docker-compose down -v && docker-compose build --no-cache && docker-compose up -d

# Quick rebuild after code changes
docker-compose down && docker-compose build && docker-compose up -d

# View logs
docker-compose logs -f app

# Check health
curl http://localhost:8080/health
```

### Local Development (Without Docker)
```bash
# Run application
./gradlew run

# Run tests
./gradlew test

# Build fat JAR
./gradlew buildFatJar
```

### Database Access
- **Adminer UI**: http://localhost:8081 (Server: mysql, User: appuser, Password: apppass, Database: self_journey)
- **CLI**: `docker-compose exec mysql mysql -u appuser -p self_journey`

## Architecture Patterns

### Layered Architecture
The codebase follows a strict 4-layer architecture:

1. **Routes** (`src/main/kotlin/com/selfjourney/routes/`): HTTP endpoint definitions, request validation, response mapping
2. **Services** (`src/main/kotlin/com/selfjourney/service/`): Business logic, transaction orchestration, suspend function handling
3. **Repositories** (`src/main/kotlin/com/selfjourney/repository/`): Database operations using Exposed DSL
4. **Domain** (`src/main/kotlin/com/selfjourney/domain/`):
   - `Models.kt`: Exposed table definitions
   - `DTOs.kt`: Serializable request/response objects

### Key Architectural Decisions

**Suspend Functions and Transactions**:
- Gemini AI calls are `suspend` functions and MUST be called outside `transaction {}` blocks
- Pattern: Split transactions before/after AI calls to avoid "suspension inside transaction" errors
- Example in `AnswerService.submitAnswer()`: Three separate transactions (fetch prev answer → AI analysis → save results)

**Repository Instantiation**:
- Repositories are instantiated in route functions (not as singletons)
- Services are instantiated with repository dependencies in route functions
- This pattern is consistent across all routes

**Configuration Loading**:
- Environment variables take precedence: `System.getenv("KEY") ?: config.propertyOrNull("key")?.getString() ?: "default"`
- HOCON configuration loaded via `ConfigFactory.load()` in `Application.kt`
- Database configuration in `Database.kt` follows the env → config → default priority

**API URL Structure**:
- All APIs use `/api/*` prefix; avoid versioned path segments.
- Example: `/api/answers`, `/api/user-goals`, `/api/questions`

## Critical Implementation Details

### Gemini AI Integration
- **No SDK used**: Direct HTTP POST to `https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent`
- API key passed as query parameter: `?key={GEMINI_API_KEY}`
- Response parsing: Extract text from `candidates[0].content.parts[0].text` and parse as JSON
- Error handling: Always provide fallback `AnalysisResult` on failure

### AI Analysis Structure (Recent Update)
The AI analysis now provides:
- `summary`: Brief overview of the answer
- `keywords`: List of extracted keywords
- `strengths`: User's positive aspects (장점)
- `weaknesses`: Areas for improvement (단점)
- `pathToIdeal`: Guidance toward ideal self (내가 원하는 사람이 될려면)
- `relationshipMap`: Map of mentioned people and their relationships (나의 관계도)
- `comparison`: Change analysis if previous answer exists

**Note**: `emotion` field has been REMOVED from the system.

### Daily Question Flow (Recent Update)
- 365-question catalog is seeded via `V5__reset_questions.sql`; IDs map directly to the day order.
- `/api/questions/today` returns the next unanswered question plus `answeredCount`, `totalQuestions`, and `remainingQuestions`.
- Answers must reference the question ID returned from the API; `UserProgress.total_answers` stays in sync through `ProgressRepository.incrementAnswerCount`.

### User Goals System
- Users can set diary periods with start/end dates and ideal person descriptions
- `UserGoalRepository.findActiveGoalByUserId()` finds goals where today falls between start_date and end_date
- The ideal person description is passed to Gemini AI for personalized analysis
- Accessed via `/api/user-goals` endpoints

### Database Schema Notes

**Removed Fields**:
- `users.gender` - Gender field removed from Users table (V3 migration)
- `answers.emotion` - Emotion field removed from Answers table (V3 migration)

**New Tables (V3 migration)**:
- `user_goals`: Diary period and ideal person description
- `ai_analysis`: Stores strengths, weaknesses, path_to_ideal, relationship_map per answer

**365 Questions**:
- Originally seeded in V4; V5 resets the list to the latest fixed set and drops `difficulty_level`
- Questions are fixed and created with `created_by='system'`

### Flyway Migration Pattern
- Files: `src/main/resources/db/migration/V{N}__{description}.sql`
- Executed automatically on application startup
- Current migrations:
  - V1: Initial schema
  - V2: Seed data
  - V3: Remove gender, add user_goals and ai_analysis tables
- V4: Seed initial 365 questions
- V5: Drop `difficulty_level` column, reset question catalog, zero out progress

## Common Patterns

### Creating New Endpoints
1. Define DTOs in `DTOs.kt` with `@Serializable` annotation
2. Define Exposed table in `Models.kt` extending `LongIdTable`
3. Create Repository with CRUD methods using Exposed DSL
4. Create Service with `transaction {}` blocks wrapping repository calls
5. Create Route function with validation and error handling
6. Register route in `Routing.kt`'s `configureRouting()`

### Error Response Pattern
All API errors follow this structure:
```kotlin
ApiResponse<Unit>(
    success = false,
    error = ErrorDetail(
        code = "ERROR_CODE",
        message = "Human readable message",
        details = optionalDetails
    )
)
```

### Exposed Query Pattern (Deprecated Warning)
Many repository files show deprecation warnings for `.select()`. This is expected - the codebase uses the older Exposed DSL pattern. When creating new queries, continue using the existing pattern for consistency.

## Testing and Validation

### API Testing
- Swagger UI: http://localhost:8080/swagger
- OpenAPI spec: http://localhost:8080/openapi
- Health check: http://localhost:8080/health

### Example Request Flow
```bash
# 1. Create user (no gender field)
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name": "테스트", "age": 25, "email": "test@example.com"}'

# 2. Set user goal
curl -X POST http://localhost:8080/api/user-goals \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "startDate": "2025-01-01",
    "endDate": "2025-12-31",
    "idealPersonDescription": "긍정적이고 자기개발에 힘쓰는 사람"
  }'

# 3. Get today's question
curl http://localhost:8080/api/questions/today?userId=1

# 4. Submit answer (no emotion field)
curl -X POST http://localhost:8080/api/answers \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "questionId": 1,
    "content": "오늘은 새로운 것을 배웠습니다."
  }'

# 5. View answer history with AI analysis
curl http://localhost:8080/api/answers/history/1?date=2025-01-01
```

## Important Constraints

- **JVM Version**: JDK 17 (enforced by `jvmToolchain(17)`)
- **Kotlin Version**: 1.9.22
- **Ktor Version**: 2.3.7
- **No Authentication**: JWT structure exists but not enforced (future work)
- **CORS**: Fully open with `anyHost()` - restrict in production
- **Gemini API Key**: Hardcoded default in routes - use env var `GEMINI_API_KEY` in production

## Troubleshooting Guide

### Build Failures
- **Unresolved reference errors**: Check imports - repository classes need explicit imports
- **Compilation errors after migration**: Run `docker-compose down -v` to clear all state
- **Gradle cache issues**: Clear with `./gradlew clean` or `docker system prune -af`

### Runtime Issues
- **Property not found**: Check environment variable precedence in `Database.kt`
- **Suspension function errors**: Ensure AI calls are outside transaction blocks
- **Flyway checksum mismatch**: Development - `docker-compose down -v`; Production - use `flywayRepair`

### Database Issues
- **Connection refused**: Wait for healthcheck - MySQL takes ~30s to initialize
- **Migration failures**: Check V3 and V4 migrations match current schema expectations
- **Data issues**: Use Adminer (http://localhost:8081) for visual inspection

## Recent Major Changes

1. **Gender Removal**: `users.gender` field completely removed from system
2. **Emotion Removal**: `answers.emotion` field removed, no longer used in AI analysis
3. **Diary Feature**: Added user goals with ideal person description and 365 fixed questions
4. **Enhanced AI Analysis**: Now includes strengths, weaknesses, pathToIdeal, and relationshipMap
5. **API Simplification**: All APIs use `/api/*` rather than versioned prefixes.
