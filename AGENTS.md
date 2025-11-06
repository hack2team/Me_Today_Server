# Repository Guidelines

## Project Structure & Module Organization
Application code lives in `src/main/kotlin/com/selfjourney`, split into `domain` (data models and DTOs), `repository` (Exposed table access), `service` (business rules), `routes` (Ktor endpoints), and `plugins` (server configuration). Runtime configuration, logging, OpenAPI specs, and Flyway migrations sit in `src/main/resources`. Tests mirror the production packages under `src/test/kotlin`, with `ApplicationTest.kt` as the entry point for integration-style checks.

## Build, Test, and Development Commands
Run `./gradlew clean build` for a full compile, lint, and test cycle before submitting work. Use `./gradlew test` during feedback cycles to execute the Kotest and JUnit suites only. For local iteration against the API, `./gradlew run` starts the Ktor server on the port defined in `.env`. To exercise the full stack (app + MySQL + Adminer), rely on `docker compose up -d --build`; tear it down with `docker compose down[-v]`.

## Coding Style & Naming Conventions
Stick to idiomatic Kotlin: four-space indentation, `val` over `var` when possible, and `UpperCamelCase` for classes, `lowerCamelCase` for functions and properties. Keep route, repository, and service filenames aligned with their primary type (e.g., `UserRoutes.kt`, `UserRepository.kt`) so package discovery remains predictable. Prefer descriptive DTO names (`CreateUserRequest`, `UserSummaryResponse`) and document non-obvious transformations with concise comments.

## Testing Guidelines
Tests should sit beside the feature they cover, mirroring the `com.selfjourney` package tree and ending with `Test.kt`. Use Kotest assertions within the existing JUnit 5 runner (`useJUnitPlatform()` is already configured). Add regression coverage for new routes or service logic, and ensure `./gradlew test` passes locally before opening a PR; aim to verify both happy paths and failure handling around database interactions and report aggregation.

## Commit & Pull Request Guidelines
The current Git history is empty, so establish clear messages using the imperative mood and, when helpful, Conventional Commit prefixes (`feat: add progress routes`). Bundle related changes together and include migration, resource, or OpenAPI updates in the same commit. PRs should describe intent, reference any tracked issue, list validation steps (tests run, curl calls, or Docker workflows), and include screenshots or JSON samples when adjusting responses.

## Environment & Configuration
Duplicate `.env.example` to `.env` for local runs, but never commit personal secrets. When adding configuration, document the variable in both files and update `src/main/resources/application.conf` if defaults change. Verify Docker workflows after modifying Flyway migrations or database credentials, since `docker compose up -d --build` applies them automatically.
