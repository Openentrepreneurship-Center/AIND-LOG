# project-base-guidelines.md — 중 등급
> 적용 대상: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)
> 상 등급에서 본 등급이 변경/완화하는 항목만 명시. 그 외는 상 등급 권고를 따른다.

## 개요

이 문서는 decapet-official/backend 프로젝트의 기술 스택, 의존성, 빌드 환경을 정의한다.
상 등급 원본은 Spring Boot 3.2.4 + MyBatis + Maven 기반이었으나, 본 프로젝트는 Spring Boot 4.0.1 + JPA + Gradle 환경이다.
아래 의존성 표와 빌드 명령이 본 프로젝트의 실제 기준이다.

## 변경/완화 사항

- 상 등급의 빌드 도구 Maven 3.9 → 본 프로젝트는 Gradle 사용 (`./gradlew build`)
- 상 등급의 Spring Boot 3.2.4 + MyBatis 3.0.3 → Spring Boot 4.0.1 + JPA + Spring Data
- 상 등급의 `com.poc.backend` 패키지 → `com.backend`
- MyBatis, XML Mapper, `mybatis-spring-boot-starter` 의존성은 사용하지 않음

## 이 등급에서 강제하는 것

**must**
- 아래 기술 스택 표를 실제 프로젝트 환경 기준으로 사용한다.
- 빌드 명령은 `./gradlew build`, 테스트는 `./gradlew test`를 사용한다.
- 신규 의존성 추가 시 `build.gradle`의 `dependencies` 블록에 명시하고, BOM이 있는 라이브러리는 BOM을 우선 사용한다.
- 데이터베이스 마이그레이션은 Flyway를 통해 관리하고, 스크립트는 `src/main/resources/db/migration/`에 위치시킨다.

**should**
- Testcontainers를 사용하여 통합 테스트 시 실제 PostgreSQL 컨테이너를 구동한다.
- Swagger UI(`/swagger-ui.html`)를 통해 API 문서를 관리한다.
- `.env` 파일은 `spring-dotenv`를 통해 로드하고 secrets를 코드에 하드코딩하지 않는다.

## 기술 스택

| 항목 | 버전 / 값 |
|---|---|
| Spring Boot | 4.0.1 |
| Java | 17 |
| 빌드 도구 | Gradle (io.spring.dependency-management 1.1.7) |
| 패키지 루트 | com.backend |
| 데이터베이스 | PostgreSQL |
| ORM | Spring Data JPA (spring-boot-starter-data-jpa) |
| 인증 | JWT (jjwt-api 0.12.6) |
| 레이트리밋 | Bucket4j (bucket4j_jdk17-core 8.14.0) |
| 스토리지 | AWS SDK S3 / SNS (bom 2.29.6) |
| 마이그레이션 | Flyway (spring-boot-starter-flyway, flyway-database-postgresql) |
| PK 생성 | ULID (ulid-creator 5.2.3) |
| API 문서 | springdoc-openapi-starter-webmvc-ui 2.7.0 |
| 테스트 | Testcontainers (bom 1.20.4) + JUnit 5 |
| 보안 | Spring Security (spring-boot-starter-security) |
| 검증 | spring-boot-starter-validation |
| HTML 정제 | jsoup 1.18.3 |
| OTP | commons-codec 1.17.1 |
| 환경변수 | spring-dotenv 4.0.0 |

전체 의존성 선언은 `decapet-official/backend/build.gradle:27-81` 참고.

## 빌드 및 실행

```bash
# 빌드
./gradlew build

# 테스트
./gradlew test

# 실행
./gradlew bootRun
```

## 예시 코드

build.gradle 의존성 블록 (build.gradle:27-81):

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    runtimeOnly 'org.postgresql:postgresql'

    // JWT
    implementation 'io.jsonwebtoken:jjwt-api:0.12.6'

    // ULID
    implementation 'com.github.f4b6a3:ulid-creator:5.2.3'

    // Flyway
    implementation 'org.springframework.boot:spring-boot-starter-flyway'
    runtimeOnly 'org.flywaydb:flyway-database-postgresql'

    // Rate Limiting
    implementation 'com.bucket4j:bucket4j_jdk17-core:8.14.0'

    // Testcontainers
    testImplementation platform('org.testcontainers:testcontainers-bom:1.20.4')
    testImplementation 'org.testcontainers:postgresql'
}
```

## 체크리스트

- [ ] Spring Boot 버전이 4.0.1인가?
- [ ] 빌드 도구가 Gradle이고 `./gradlew build`로 빌드되는가?
- [ ] JPA 의존성(`spring-boot-starter-data-jpa`)이 선언되어 있고 MyBatis 의존성이 없는가?
- [ ] 신규 의존성 추가 시 BOM이 있는 경우 BOM을 먼저 선언하였는가?
- [ ] Flyway 마이그레이션 스크립트가 `db/migration/` 하위에 위치하는가?
- [ ] Testcontainers를 사용한 통합 테스트가 PostgreSQL 컨테이너를 사용하는가?
- [ ] secrets(DB 패스워드, JWT secret 등)가 `.env` 또는 환경변수로 관리되고 코드에 하드코딩되지 않는가?
- [ ] Swagger UI를 통해 API 문서가 확인 가능한가?
