# project-base-guidelines.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

---

## 1. 개요

이 문서는 `decapet-official/backend` 프로젝트의 핵심 기술 스택과 빌드 명령을 정의한다.

---

## 2. 핵심 기술 스택

| 항목 | 내용 |
|------|------|
| 언어 | Java 17 |
| 프레임워크 | Spring Boot 4.0.1 |
| 빌드 도구 | Gradle (Groovy DSL) |
| ORM | Spring Data JPA |
| DB | PostgreSQL |
| 마이그레이션 | Flyway |
| 보안 | Spring Security + JJWT 0.12.6 |
| PK 전략 | ULID (ulid-creator 5.2.3) |

---

## 3. 빌드 명령

| 명령 | 목적 |
|------|------|
| `./gradlew build` | 컴파일 + 테스트 + JAR 생성 |
| `./gradlew test` | 테스트 실행 |
| `./gradlew bootRun` | 로컬 서버 실행 |

Java Toolchain 설정:

```groovy
// decapet-official/backend/build.gradle:11-15
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
```

---

## 4. 환경 분리

- 환경별 설정은 `application-{profile}.yml`로 분리한다.
- 민감 정보(DB 비밀번호, JWT 시크릿)는 소스에 하드코딩하지 않는다.

---

## 5. 체크리스트

- [ ] `./gradlew build`가 오류 없이 완료되는가
- [ ] 민감 정보가 소스에 하드코딩되지 않았는가
- [ ] 환경별 설정이 `application-{profile}.yml`로 분리되어 있는가
- [ ] Entity 필드 추가 시 Flyway 마이그레이션 파일이 함께 작성되었는가
