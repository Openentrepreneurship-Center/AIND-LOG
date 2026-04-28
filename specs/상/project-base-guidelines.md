# project-base-guidelines.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

---

## 1. 개요

이 문서는 `decapet-official/backend` 프로젝트의 기술 스택, 의존성, 빌드 명령, 환경 분리 전략을 정의한다.
신규 팀원 온보딩, 개발 환경 설정, CI/CD 파이프라인 구성 시 기준 문서로 사용한다.
모든 의존성 추가·제거는 이 문서의 의존성 표를 기준으로 검토한 뒤 `build.gradle`에 반영한다.

---

## 2. 기술 스택

| 항목 | 내용 |
|------|------|
| 언어 | Java 17 (LTS) |
| 프레임워크 | Spring Boot 4.0.1 |
| 빌드 도구 | Gradle (Groovy DSL) |
| ORM | Spring Data JPA (Hibernate) |
| DB | PostgreSQL |
| 마이그레이션 | Flyway |
| 보안 | Spring Security + JJWT 0.12.6 |
| 레이트 리밋 | Bucket4j 8.14.0 |
| 파일 스토리지 | AWS SDK v2 (S3) |
| 알림 | AWS SDK v2 (SNS) |
| API 문서 | springdoc-openapi 2.7.0 (Swagger UI) |
| 테스트 | JUnit 5 + Testcontainers 1.20.4 |
| PK 전략 | ULID (ulid-creator 5.2.3) |
| 기타 | Bucket4j, jsoup(HTML sanitization), TOTP(commons-codec) |

---

## 3. 의존성 상세 (`build.gradle` 기준)

```groovy
// decapet-official/backend/build.gradle:27-80

dependencies {
    // Core
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-webmvc'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'org.springframework.boot:spring-boot-starter-flyway'
    runtimeOnly 'org.flywaydb:flyway-database-postgresql'
    runtimeOnly 'org.postgresql:postgresql'

    // JWT
    implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.6'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.6'

    // AWS SDK v2
    implementation platform('software.amazon.awssdk:bom:2.29.6')
    implementation 'software.amazon.awssdk:s3'
    implementation 'software.amazon.awssdk:sns'

    // ULID
    implementation 'com.github.f4b6a3:ulid-creator:5.2.3'

    // OpenAPI/Swagger
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0'

    // Rate Limiting
    implementation 'com.bucket4j:bucket4j_jdk17-core:8.14.0'

    // HTML Sanitization
    implementation 'org.jsoup:jsoup:1.18.3'

    // OTP (TOTP)
    implementation 'commons-codec:commons-codec:1.17.1'

    // Testcontainers
    testImplementation platform('org.testcontainers:testcontainers-bom:1.20.4')
    testImplementation 'org.springframework.boot:spring-boot-testcontainers'
    testImplementation 'org.testcontainers:postgresql'
    testImplementation 'org.testcontainers:junit-jupiter'
}
```

---

## 4. Java Toolchain (must)

```groovy
// decapet-official/backend/build.gradle:11-15
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
```

- 로컬 개발 환경의 JDK 버전과 무관하게 Gradle toolchain이 Java 17을 강제한다.
- JDK 17 미만 버전으로 빌드를 시도하면 Gradle이 오류를 반환한다.

---

## 5. 빌드 및 실행 명령

| 명령 | 목적 |
|------|------|
| `./gradlew build` | 컴파일 + 테스트 + JAR 생성 |
| `./gradlew test` | 단위·통합 테스트 실행 |
| `./gradlew bootRun` | 로컬 서버 실행 (`local` 프로파일) |
| `./gradlew clean build` | 전체 클린 빌드 |
| `./gradlew dependencies` | 의존성 트리 확인 |

- CI 파이프라인에서는 `./gradlew build`를 기본 명령으로 사용한다.
- 테스트 없이 빌드하는 `-x test` 옵션은 배포 파이프라인에서 사용하지 않는다.

---

## 6. 환경 분리 전략 (must)

### 6.1 프로파일 구조
```
src/main/resources/
  application.yml          # 공통 설정
  application-local.yml    # 로컬 개발 환경
  application-dev.yml      # 개발 서버
  application-prod.yml     # 프로덕션
```

- 프로파일별 설정은 `application-{profile}.yml`로 분리한다.
- DB URL, 비밀 키 등 민감 정보는 `.env` 파일 또는 환경 변수로 주입하며 소스 저장소에 커밋하지 않는다.
- `spring-dotenv`(me.paulschwarz:spring-dotenv:4.0.0)를 통해 `.env` 파일을 로컬 개발에서 읽는다.

### 6.2 Docker Compose 연동
- `developmentOnly 'org.springframework.boot:spring-boot-docker-compose'` 의존성이 선언되어 있어, 로컬 실행 시 `compose.yaml`이 자동으로 PostgreSQL 컨테이너를 기동한다.
- 프로덕션 환경에서는 Docker Compose 의존성이 제외된다(`developmentOnly` 스코프).

---

## 7. 데이터베이스 마이그레이션 (must)

- 스키마 변경은 반드시 Flyway 마이그레이션 파일로 관리한다.
- 파일 위치: `src/main/resources/db/migration/`
- 파일 명명: `V{버전}__{설명}.sql` (예: `V2__add_user_phone_column.sql`)
- Entity 필드 추가 시 반드시 대응하는 Flyway 파일을 함께 작성한다.
- 마이그레이션 파일은 한 번 커밋된 이후 내용을 수정하지 않는다.

---

## 8. 테스트 전략 (must)

- 통합 테스트는 Testcontainers를 사용해 실제 PostgreSQL 컨테이너 위에서 실행한다.
- 단위 테스트는 Mockito를 활용해 외부 의존성을 Mock 처리한다.
- 테스트 코드도 프로덕션 코드와 동일한 패키지 구조를 유지한다.
- 코드 커버리지 목표: 라인 기준 70% 이상 (JaCoCo 기준).
- `@SpringBootTest` 테스트는 Testcontainers와 함께 사용하고, H2 인메모리 DB는 사용하지 않는다.

---

## 9. API 문서 (Swagger)

- Swagger UI 경로: `/swagger-ui/index.html`
- API 명세: `/api-docs`
- 퍼블릭 접근 허용 경로:

```java
// decapet-official/backend/src/main/java/com/backend/global/config/SecurityConfig.java:67
.requestMatchers("/docs", "/swagger-ui/**", "/api-docs/**").permitAll()
```

- 모든 Controller 메서드는 `*Api` 인터페이스에 `@Operation`, `@ApiResponse` 어노테이션을 선언하고, Controller 구현체에는 중복 선언하지 않는다.

---

## 10. 체크리스트

- [ ] `build.gradle`의 Java toolchain이 17로 설정되어 있는가
- [ ] `./gradlew build`가 오류 없이 완료되는가
- [ ] `./gradlew test`가 오류 없이 완료되는가
- [ ] 민감 정보(DB 비밀번호, JWT 시크릿)가 소스에 하드코딩되지 않았는가
- [ ] 환경별 설정이 `application-{profile}.yml`로 분리되어 있는가
- [ ] 신규 의존성 추가 시 BOM 또는 버전 관리가 되어 있는가
- [ ] Entity 필드 추가 시 대응 Flyway 마이그레이션 파일이 함께 작성되었는가
- [ ] 통합 테스트가 Testcontainers 기반 PostgreSQL을 사용하는가
- [ ] 커버리지가 70% 이상인가 (JaCoCo 리포트 확인)
- [ ] Swagger 퍼블릭 경로가 SecurityConfig에 등록되어 있는가
