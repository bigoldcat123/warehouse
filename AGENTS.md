# Repository Guidelines

## Project Structure & Module Organization

This workspace contains two independent Git repositories plus supporting artifacts:

- `api-26-9-13/`: Spring Boot 3.2 backend. Java code is under `src/main/java/com/example/demo`; controllers, services, mappers, and entities live in `system/`. MyBatis XML and profile configuration are in `src/main/resources/`; tests are in `src/test/java/`.
- `warehouse-fornt 26-9-13/`: Vue 3 + TypeScript frontend. Put pages in `src/views/`, reusable UI in `src/components/`, API clients in `src/api/`, Pinia stores in `src/stores/`, and images/styles in `src/assets/` or `public/`.
- `lhq_at.sql` and the Chinese `.docx` file are database/design references, not application entry points.

Consult each module's existing `AGENTS.md`/`Agents.md` before making module-specific changes.

## Build, Test, and Development Commands

Run commands from the relevant module directory.

```bash
# backend (JDK 21 and system Maven required)
JAVA_HOME=$(/usr/libexec/java_home -v 21) mvn spring-boot:run
JAVA_HOME=$(/usr/libexec/java_home -v 21) mvn test
JAVA_HOME=$(/usr/libexec/java_home -v 21) mvn -DskipTests clean package

# frontend (Yarn 1)
yarn install
yarn dev
yarn build       # type-checks, then creates the production bundle
yarn preview     # serves the built bundle on port 8848
```

The backend expects MySQL; MQTT is needed for live device data.

## Coding Style & Naming Conventions

Use four-space indentation in Java and follow surrounding TypeScript/Vue formatting. No formatter or linter is configured, so avoid unrelated reformatting. Java types follow `XController`, `IXService`, `XServiceImpl`, `XMapper`, `XDTO`, and `XQuery`. Vue components use PascalCase filenames; stores and API modules use camelCase or domain directory names. Preserve established package names and legacy database column mappings, including existing misspellings.

## Testing Guidelines

Backend tests use JUnit 5 and `@SpringBootTest`. Existing tests contact and mutate the development database; verify the target database before running them, and prefer focused runs such as `mvn test -Dtest=DemoApplicationTests`. Add tests under matching package paths with `*Test.java` names. The frontend has no automated test suite; require `yarn build` as the minimum type and build check, then manually exercise affected screens.

## Commit & Pull Request Guidelines

Recent history uses brief, imperative Chinese subjects (for example, `添加自动旋转开关`). Keep commits focused and state the user-visible change. Open pull requests against the correct module repository, summarize backend/frontend impact, note database or configuration requirements, list verification performed, link relevant issues, and include screenshots for UI changes. Never commit production credentials or new secrets in `application-*.properties`.
