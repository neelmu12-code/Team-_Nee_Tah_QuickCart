# QuickCart Software Testing Project — Starter Repository

This repository intentionally contains a small production codebase with imperfect behaviour. Your job is not to build a new application. Your job is to design, automate, evaluate, and communicate a defensible testing strategy.

## Technology
- Java 17+
- Maven
- JUnit 5
- Mockito
- jqwik (property-based testing)
- JaCoCo (coverage)
- PIT (mutation testing)

## Useful commands
- `mvn test`
- `mvn test jacoco:report`
- `mvn org.pitest:pitest-maven:mutationCoverage`

## Rules
- Treat PROJECT_SPECIFICATION.md as authoritative.
- Do not change production code merely to make a failing test pass.
- A behaviour-preserving refactor for testability is allowed only if clearly identified and justified.
- Add your own tests under `src/test/java`.
- Keep evidence needed for the final video/report in the repository.
