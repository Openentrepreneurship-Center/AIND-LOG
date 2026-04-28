# project-base-guidelines.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

---

## 1. 개요

이 문서는 `decapet-official/backend` 프로젝트의 기술 스택, 의존성, 빌드 명령, 환경 분리 전략을 정의한다.
개발 환경 설정과 신규 의존성 추가 시 이 문서를 기준으로 검토한다.

---

## 2. 기술 스택

| 항목 | 내용 |
|------|------|
| 언어 | Java 17 |
| 프레임워크 | Spring Boot 4.0.1 |
| 빌드 도구 | Gradle (Groovy DSL) |
| ORM | Spring Data JPA |
| DB | PostgreSQL |
| 마이그레이션 | Flyway |
| 보안 | Spring Security + JJWT 0.12.6 |
| 레이트 리밋 | Bucket4j 8.14.0 |
| 파일 스토리지 | AWS SDK v2 (S3, SNS) |
| API 문서 | springdoc-openapi 2.7.0 |
| 테스트 | JUnit 5 + Testcontainers 1.20.4 |
| PK 전략 | ULID (ulid-creator 5.2.3) |

---

## 3. 핵심 의존성 (`build.gradle` 기준)

```groovy
// decapet-official/backend/build.gradle:27-80
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-webmvc'
    implementation 'org.springframework.boot:spring-boot-starter-flyway'
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

    // OpenAPI
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0'

    // Rate Limiting
    implementation 'com.bucket4j:bucket4j_jdk17-core:8.14.0'

    // Testcontainers
    testImplementation platform('org.testcontainers:testcontainers-bom:1.20.4')
    testImplementation 'org.testcontainers:postgresql'
}
```

---

## 4. Java Toolchain

```groovy
// decapet-official/backend/build.gradle:11-15
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
```

---

## 5. 빌드 명령

| 명령 | 목적 |
|------|------|
| `./gradlew build` | 컴파일 + 테스트 + JAR 생성 |
| `./gradlew test` | 테스트 실행 |
| `./gradlew bootRun` | 로컬 서버 실행 |
| `./gradlew clean build` | 클린 빌드 |

---

## 6. 환경 분리 (must)

- 환경별 설정은 `application-{profile}.yml`로 분리한다.
- DB URL, JWT 시크릿 등 민감 정보는 `.env` 또는 환경 변수로 주입하고 저장소에 커밋하지 않는다.
- `spring-dotenv`를 통해 로컬 개발 시 `.env` 파일을 읽는다.

```
src/main/resources/
  application.yml
  application-local.yml
  application-dev.yml
  application-prod.yml
```

---

## 7. 데이터베이스 마이그레이션 (must)

- 스키마 변경은 Flyway 마이그레이션 파일로 관리한다.
- 파일 위치: `src/main/resources/db/migration/`
- 파일 명명: `V{버전}__{설명}.sql`
- Entity 필드 추가 시 대응 Flyway 파일을 함께 작성한다.

---

## 8. 체크리스트

- [ ] `./gradlew build`가 오류 없이 완료되는가
- [ ] 민감 정보가 소스에 하드코딩되지 않았는가
- [ ] 환경별 설정이 `application-{profile}.yml`로 분리되어 있는가
- [ ] Entity 필드 추가 시 Flyway 파일이 함께 작성되었는가
- [ ] 신규 의존성 추가 시 버전이 명시되거나 BOM으로 관리되는가
- [ ] 통합 테스트가 Testcontainers 기반 PostgreSQL을 사용하는가
