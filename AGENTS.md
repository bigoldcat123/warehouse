# Repository Guidelines

## Project Overview

Spring Boot 3.2.4 / Java 21 REST backend for grain-warehouse (粮仓) monitoring: temperature, humidity and gas
sensor points, alarm handling, ventilation (风机/通风) devices, MQTT device polling and EasyExcel report export.

- Coordinates: `com.example:demo:0.0.1-SNAPSHOT`, base package `com.example.demo`.
- External runtime dependencies (not in repo): MySQL database `ware_house_management`, MQTT broker on `127.0.0.1:1883`.
- No frontend here. `src/main/resources/static/*` are placeholders; the real UI lives in a separate project.

## Architecture & Data Flow

Layered, package-by-feature under `com.example.demo.system`; cross-cutting code in `common/`, `security/`, `config/`.

```
controller (R responses) → service (IXService) → service/impl (ServiceImpl<XMapper, PO>) → mapper (BaseMapper) → MySQL
```

- **HTTP contract**: every endpoint returns `R` (a `HashMap`) and **always HTTP 200**, including failures. Check the
  body: `code` (`"200"` success / `"500"` business error / `"403"` auth), `show` (`0`=silent, `1`=success, `2`=error,
  `3`=warning), `message`, `value`. Do not "fix" this to real status codes without being asked.
- **Auth flow**: `SecurityConfiguration` permits only `/ws`, `/static/**`, `/alarm/excel`, `/alarm/arg_excel`,
  `/data/{temperature,humidity,gas}Cube`; everything else needs a token.
  - `CommonUserNamePasswordAuthenticationFilter` → `POST /login` (form params `username`, `password`) returns
    `CurrentUserVo{detail,token}`; `MailCodeAuthenticationFilter` → `POST /mailcode` (`email`, `code`);
    `LogoutFilter` → `POST /logout` (stateless, does not close WebSocket sessions);
    `JwtAuthorizationFilter` reads the `Authorization: Bearer <jwt>` header on every request.
  - `JwtAuthorizationManager.check()` resolves tokens: `devToken` bypass (only when `spring.profiles.active=dev`),
    `guestToken` (read-only: GET/HEAD/OPTIONS + `POST /alarm|/data|/entry/{current}/{size}`), otherwise JWT parse.
    It is also called directly by the WebSocket handshake interceptors.
  - `CurrentUser.get()` reads the caller from `SecurityContextHolder`; `CurrentUser.getDevFakeCurrentUser()` /
    `getGuestFakeCurrentUser(...)` supply the fake identities for the two special tokens.
- **MQTT device flow**: `MqttService.init()` (`@PostConstruct`) connects to `tcp://${czh.mqtt.server}` with client id
  `sinograin_kt_TJML_001abefg`. `MqttTask` (`@Scheduled(fixedRate = 10000)`) publishes a query to
  `/publish/sinograin/devices/{wareHouseNo}/command` for every `gf_kt` row and subscribes to
  `/sinograin/devices/{wareHouseNo}/commandResponse`; the callback updates `House.pvElec/airHourElec/meterElec`.
  A missing broker is non-fatal: `MQTT server not avaliable!` is logged and retried on the next tick.
- **WebSocket**: `/ws` (`WsConfiguration`, origins `*`), handler `MqttWsHandler`. Query string is parsed **by
  position**: `?token=..&wareHouseNo=..&houseNo=..` — `WsAuthInterceptor` needs param 0, `MqttWsInterceptor` needs
  params 1–2. Each session spawns an `MqttExecutor` thread pushing device JSON to the client.
- **Simulated data**: humidity and gas responses are randomly generated, not measured —
  `getHumidityCubeByHouseNo` (`buildRandomCube(house, 20, 90)`), `getGasCubeByHouseNo` (`300, 3000`) and all
  `get*Humidity*` record series. `getTemperatureCubeByHouseNo` is real, but confusingly reads the `gasStrength`
  column through `get_temps(...)`. See `DataServiceImpl`.

## Key Directories

| Path | Purpose |
| --- | --- |
| `src/main/java/com/example/demo/DemoApplication.java` | Entry point; `@MapperScan("com.example.demo.system.mapper")`, `@EnableScheduling` |
| `src/main/java/com/example/demo/system/controller/` | REST endpoints, grouped by resource (`/house`, `/data`, `/alarm`, `/warehouse`, `/entry`, `/gfkt`, `/nhlstt`, `/user`, `/warehouseSettings`) |
| `src/main/java/com/example/demo/system/service/` + `impl/` | `IXService` interfaces + `ServiceImpl<XMapper, PO>` implementations |
| `src/main/java/com/example/demo/system/mapper/` | MyBatis-Plus `BaseMapper<T>` interfaces |
| `src/main/java/com/example/demo/system/entity/PO/` | Table entities (`@TableName`, `@TableField`) |
| `src/main/java/com/example/demo/system/entity/DTO/`, `query/` | Response DTOs (incl. EasyExcel models) and request query objects |
| `src/main/java/com/example/demo/common/` | `R`, `CurrentUser`, `CurrentUserVo`, unhandled-by-Spring exception types, `mqtt/`, `tasks/` |
| `src/main/java/com/example/demo/security/` | `config/`, `filter/`, `provider/`, `jwt/`, `authentication/`, `hadnler/` (note: typo is intentional/committed) |
| `src/main/java/com/example/demo/config/` | `MyBatisConfiguration` (pagination interceptor), `WebConfiguration` (static handler), `WsConfiguration` |
| `src/main/resources/mapper/` | MyBatis XML (mybatis-plus default location `classpath*:/mapper/**/*.xml`) |
| `src/main/resources/application*.properties` | Base + `dev` / `prod` profiles |

Dev-only leftovers under `system/controller/test/` (`NoConftoller`→`/no`, `TestController`→`/test`), `Sample/`,
`common/sender/Sender.java` (empty interface), plus the empty `ReceiverDataController` and `TongfengDevController`
stubs. They are compiled, not `permitAll`, and not wired into anything — leave them unless asked.

## Development Commands

JDK 21 is required. The machine default `java`/`mvn` runs JDK 25, which breaks Lombok annotation processing for this
build — always pin `JAVA_HOME`:

```bash
# run (dev profile is the default from application.properties)
JAVA_HOME=$(/usr/libexec/java_home -v 21) mvn spring-boot:run

# run another profile
JAVA_HOME=$(/usr/libexec/java_home -v 21) mvn spring-boot:run -Dspring-boot.run.profiles=prod

# package / run the fat jar
JAVA_HOME=$(/usr/libexec/java_home -v 21) mvn -DskipTests clean package
java -jar target/demo-0.0.1-SNAPSHOT.jar

# tests (see Testing & QA before running — they hit the real dev database)
JAVA_HOME=$(/usr/libexec/java_home -v 21) mvn test
JAVA_HOME=$(/usr/libexec/java_home -v 21) mvn test -Dtest=InsertRandomDataTest
```

- Lint/format: **none configured** (no spotless, checkstyle, editorconfig). Match surrounding style manually; tabs in
  `pom.xml`, 4 spaces in Java, Chinese Javadoc preserved.
- Smoke check after starting: `curl -s http://127.0.0.1:8080/warehouse` → `{"code":"403",...}` means the app is up
  (unauthorized, but responding). Full check: `POST /login` with valid credentials, then reuse the returned token as
  `Authorization: Bearer <token>`.
- `maven-compiler-plugin` deliberately disables the default `default-compile`/`default-testCompile` executions and
  re-registers them as `compile`/`testCompile`; do not "clean that up".

## Code Conventions & Common Patterns

- **Naming**: `IXService` / `XServiceImpl` / `XMapper` / `XDTO` / `XQuery`; controllers are `XController` mapped to a
  lowercase singular resource (`@RequestMapping("/warehouseSettings")`, `/gfkt`, `/nhlstt`).
- **Controller style**: `@RestController`, `@Autowired` field injection, returns `R` — `R.ok(value)`, `R.okShow(msg)`,
  `R.errorShow(msg)`, `R.error()`. Validation failures return `R.errorShow("格式错误")`.
- **Paging**: either `GET /resource?current=&size=` (MyBatis-Plus `Page`) or `POST /resource/{current}/{size}` with a
  `XQuery` body (`/alarm`, `/data`, `/entry`) — keep the existing shape of each controller.
- **Business logic often lives in controllers** (QueryWrapper building, permission filters via `CurrentUser.get()`).
  Follow the local pattern instead of introducing a service layer where none exists.
- **Persistence**: explicit column mapping is mandatory — table/column names are legacy mixed-case
  (`@TableField("HouseNo")`, backticked ``@TableField("`InT`")``); PK always `@TableId(..., IdType.AUTO)`. Complex SQL
  goes in `src/main/resources/mapper/*.xml` (only `EntryMapper.xml#getNewest` currently); the rest use `QueryWrapper`.
- **Errors**: throw the domain exceptions in `common/exception/` (`NoSuchUserException`, `MailCode*Exception`,
  `MailSendFailedException`, `MyCommonException`); `GlobalExceptionHandler` (`@RestControllerAdvice`) converts them
  into `R`. Anything unhandled becomes `R.errorShow("服务器裂开啦～😣~")`.
- **Serialization**: `fastjson2` (`com.alibaba.fastjson2.JSON`) in filters/handlers/WebSocket; Jackson is used for MVC
  and `@JsonFormat` on `LocalDateTime`/`LocalTime` PO fields. `Lombok` everywhere (`@Data`, `@Setter`, `@Slf4j`,
  `lombok.val`).
- **No `@Transactional` and no `@Async`** anywhere in the codebase — do not assume transactional semantics.
- Passwords are BCrypt (`MyDaoAuthenticationProvider`, `UserAuth.getSafePassword()`); never return the `psw` column —
  `UserAuthController` blanks it before serializing.

## Important Files

- `pom.xml` — Spring Boot parent 3.2.4, MyBatis-Plus 3.5.5, `jjwt` 0.12.5, fastjson2 2.0.48, EasyExcel 4.0.3,
  Paho MQTT 1.2.5, Lombok 1.18.32. `kotlin.version` is declared but the Kotlin plugin is commented out and there are
  no `.kt` files — ignore it.
- `src/main/resources/application.properties` — shared config: `spring.profiles.active=dev`, `server.port=8080`,
  `staticRootDir=d:\minglun\`, `staticPrefix=static/`, `guestToken`, `guest.username`, `czh.mqtt.*`.
- `src/main/resources/application-dev.properties` — MySQL `root/root@localhost:3306/ware_house_management`,
  `devToken=123456789`. `czh.path`/`czh.module`/`czh.tableName` here are **unused** by this codebase (leftovers from
  the code generator jar).
- `src/main/resources/application-prod.properties` — MySQL `root/123456`, `devToken=123456789asdasdasd...`.
- `src/main/java/com/example/demo/security/config/SecurityConfiguration.java` — the permit-list and filter ordering.
- `src/main/java/com/example/demo/security/JwtAuthorizationManager.java` — token/guest/dev policy.
- `src/main/java/com/example/demo/system/service/impl/DataServiceImpl.java` — biggest file; all sensor aggregation and
  the random-data helpers.
- `src/main/java/com/example/demo/system/service/impl/MqttService.java`, `common/tasks/MqttTask.java`,
  `common/mqtt/MqttWsHandler.java` — device polling and push.
- `code-generator-1.0-SNAPSHOT-jar-with-dependencies.jar` — 24 MB leftover at the repo root, referenced by nothing;
  gitignored.

## Runtime/Tooling Preferences

- **JDK 21** (`/usr/libexec/java_home -v 21`, Temurin 21.0.11 installed) and Maven 3.9. No `mvnw` wrapper — use the
  system `mvn`.
- **MySQL 8** at `localhost:3306`, database `ware_house_management`. The schema is **not** in the repo (no `.sql`
  files) — the DB must exist and be populated before startup; entities map tables `house`, `warehouse`,
  `warehouse_settings`, `receiver_data`, `_entry`, `_user`, `alarm`, `gf_kt`, `nhlstt`, `tongfeng_dev`.
- **MQTT broker** (EMQX-style topics) at `127.0.0.1:1883`, credentials `admin/123456`. Optional for boot, required for
  device data.
- `staticRootDir` is a Windows path in the committed properties; on macOS image/static file serving under
  `${staticPrefix}**` resolves to nothing until it is overridden (`--staticRootDir=/some/dir/`).
- No Docker, no CI, no scripts, no `.http`/Postman collections, no README besides this file. `git` branch: `main`.
- Committed secrets (do not add more, and treat as dev-only): MySQL passwords, MQTT password, `guestToken`,
  `devToken`, and the fixed HS256 key in `security/jwt/JwtService.java`.

## Testing & QA

- Framework: JUnit 5 via `spring-boot-starter-test` (parent-managed). No Testcontainers, no H2, no surefire/JaCoCo
  customization, no coverage gate, no CI.
- Two test classes, both `@SpringBootTest` against the live dev MySQL, both without assertions, both **write to the
  database**: `src/test/java/com/example/demo/DemoApplicationTests.java` (`IEntryService.save` of one row) and
  `InsertRandomDataTest.java` (`IDataService.insertRandomData("003", 100, ...)` inserts 100 sensor rows). Running
  `mvn test` therefore requires MySQL to be up and pollutes the dev data — prefer targeted `-Dtest=...` runs, and do
  not add new DB-writing `@SpringBootTest`s.
- Preferred verification for changes here: start the app (`mvn spring-boot:run`) and exercise the endpoint with
  `curl`, or write a throwaway script; use `MockMvc`/mocked services if a permanent test is genuinely warranted.
- When asserting behavior, assert the `R` body (`code`/`message`/`value`), not the HTTP status — the app always
  returns 200.
