# Decapet-Official Backend 코딩 스펙 (상 / 중 / 하)

본 디렉토리는 `decapet-official/backend` 프로젝트에 적용할 **세 단계 코딩 스펙**을 모아둔 곳이다.

| 등급 | 위치 | 성격 | 출처 |
| --- | --- | --- | --- |
| 상 | `specs/상/` | 가장 엄격한 표준. 외부 참조용 원본. | 외부 전달본 그대로 (Spring Boot 3.2.4 + MyBatis 가정) |
| 중 | `specs/중/` | decapet-official 실제 스택(JPA / Spring Boot 4 / Gradle)에 맞춘 **권장 표준**. | 본 저장소에서 신규 작성 |
| 하 | `specs/하/` | 신규 도메인·프로토타입·해커톤 단계의 **최소 골격**. | 본 저장소에서 신규 작성 |

## 적용 기준

| 상황 | 권장 등급 |
| --- | --- |
| 결제·인증·정산·민감정보 도메인, 외부 감사 대상 코드 | **상** |
| 사용자 도메인 본 기능, 신규 정식 도메인 | **중** (기본값) |
| PoC, 실험 기능, 단기 스파이크 | **하** |

같은 도메인 안에서도 메서드 단위로 등급이 다를 수 있다. 이 경우 메서드 주석에 `// 등급: 상` 처럼 명시한다.

## 등급 간 관계

- 중 / 하 문서는 상위(상) 문서를 베이스로 하고, **완화·치환되는 항목만 명시**한다. 명시되지 않은 부분은 상 등급 권고를 따른다.
- 단, 상 등급은 MyBatis 기반 가정이라 JPA 환경에서 그대로 적용 불가능한 섹션이 있다. 그런 섹션은 **중 등급의 같은 이름 문서로 대체**된다 (예: `mapper-pattern.md`, `query-patterns.md`).

## 실제 프로젝트 스택 (중/하 문서가 가정하는 환경)

- Spring Boot 4.0.1, Java 17, Gradle
- JPA + Spring Data, Specification, QueryDSL 가용
- 패키지 루트: `com.backend`, 도메인 기반 구조 (`com.backend.domain.{x}`)
- DTO: Java `record`, Entity↔DTO 변환은 `@Component` 클래스(`*Mapper`)
- 글로벌: `GlobalExceptionHandler` + `ErrorCode` enum + `BusinessException`, 공통응답 `SuccessResponse`
- 검증: `ValidationConstants`(정규식·메시지 상수) + `@Valid` + `@Pattern`
- 인증/보안: JWT, Bucket4j 레이트리밋, 다중 Filter(`JwtFilter` → `AccountValidationFilter` → `PermissionFilter`)
- 그 외: ULID PK + `BaseEntity`, soft-delete(`@SQLRestriction`), 도메인 이벤트, Swagger(springdoc), Testcontainers + Flyway

## 문서 목록

| 영역 | 파일 |
| --- | --- |
| 기초 | `coding-standards.md`, `naming_rule.md`, `project-base-guidelines.md`, `package_rule.md`, `code-generate-guidelines.md` |
| 레이어 패턴 | `controller-pattern.md`, `service-pattern.md`, `mapper-pattern.md`, `query-patterns.md` |
| 횡단 관심사 | `exception-handling-pattern.md`, `domain-structure.md`, `springboot-standard.md` |
| 보안 / 품질 | `quality-rule.md`, `encryption-decryption-guidelines.md`, `masking-guildlines.md`, `spec-driven-buisness-rule-guidlines.md` |

각 등급 디렉토리에 위 16개 파일이 동일한 이름으로 들어 있다.

## 변경 이력

- 2026-04-28: 초안 작성. 상은 외부 전달본 복사, 중·하 신규 작성.
