# package_rule.md — 하 등급
> 적용 대상: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)
> 상 등급에서 본 등급이 변경/완화하는 항목만 명시. 그 외는 상 등급 권고를 따른다.

## 개요

이 문서는 decapet-official/backend에서 하 등급 작업자가 따라야 할 최소 패키지 규칙을 정의한다.
도메인 코드는 `com.backend.domain.{domain}`, 글로벌 공통 코드는 `com.backend.global` 두 갈래만 강제한다.
상 등급 원본(`package_rule.md`)은 내용이 없었으므로, 본 문서가 기준 역할을 한다.

## 변경/완화 사항

- 상 등급 원본 미정의 → 본 등급에서 핵심 구조만 정의
- 서브패키지 세분화(dto.internal, dto.mapper, event 등) → 하 등급은 필수 아님
- cross-domain service 참조 원칙 → 하 등급은 권장 수준

## 이 등급에서 강제하는 것

**must**
- 도메인 코드는 `com.backend.domain.{domain}` 하위에 위치한다.
- 글로벌 공통 코드는 `com.backend.global` 하위에 위치한다.
- 패키지 루트 `com.backend` 외부에 소스코드를 두지 않는다.

**should**
- 다른 도메인의 repository를 직접 주입하지 않고 service를 통해 참조한다.

## 패키지 구조

```
com.backend
├── domain
│   └── {domain}          예: user, auth, pet
│       ├── controller
│       ├── service
│       ├── repository
│       ├── entity
│       ├── dto
│       └── exception
└── global
    ├── config
    ├── common
    ├── error
    └── security
```

## 체크리스트

- [ ] 도메인 코드가 `com.backend.domain.{domain}` 하위에 위치하는가?
- [ ] 글로벌 공통 코드가 `com.backend.global` 하위에 위치하는가?
- [ ] `com.backend` 루트 밖에 소스코드가 없는가?
- [ ] 패키지명이 소문자 단어로만 구성되었는가?
