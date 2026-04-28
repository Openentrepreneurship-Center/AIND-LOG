# project-base-guidelines.md — 하 등급
> 적용 대상: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)
> 상 등급에서 본 등급이 변경/완화하는 항목만 명시. 그 외는 상 등급 권고를 따른다.

## 개요

이 문서는 decapet-official/backend 프로젝트의 기술 스택 핵심 항목만 정의한다.
상 등급 원본은 Spring Boot 3.2.4 + MyBatis + Maven 기반이었으나, 본 프로젝트는 Spring Boot 4.0.1 + JPA + Gradle 환경이다.

## 변경/완화 사항

- 상 등급의 전체 의존성 표 → 하 등급은 핵심 스택만 기재
- 상 등급의 Maven 3.9 → Gradle 사용
- MyBatis, XML Mapper 관련 항목 해당 없음

## 이 등급에서 강제하는 것

**must**
- 아래 핵심 기술 스택을 기준으로 코드를 작성한다.
- 빌드는 `./gradlew build`를 사용한다.

## 핵심 기술 스택

| 항목 | 값 |
|---|---|
| Spring Boot | 4.0.1 |
| Java | 17 |
| 빌드 도구 | Gradle |
| 패키지 루트 | com.backend |
| ORM | JPA (Spring Data JPA) |
| 데이터베이스 | PostgreSQL |
| 인증 | JWT |
| PK 생성 | ULID |

## 빌드

```bash
./gradlew build   # 전체 빌드
./gradlew test    # 테스트
```

## 체크리스트

- [ ] Spring Boot 버전이 4.0.1인가?
- [ ] 빌드 도구가 Gradle인가?
- [ ] ORM으로 JPA를 사용하고 있는가? (MyBatis 미사용)
- [ ] 패키지 루트가 `com.backend`인가?
- [ ] secrets가 환경변수 또는 `.env`로 관리되는가?
