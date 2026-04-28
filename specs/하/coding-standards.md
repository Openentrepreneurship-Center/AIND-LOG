# coding-standards.md — 하 등급
> 적용 대상: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)
> 상 등급에서 본 등급이 변경/완화하는 항목만 명시. 그 외는 상 등급 권고를 따른다.

## 개요

이 문서는 decapet-official/backend에서 하 등급 작업자(초급·외부 기여자)가 반드시 지켜야 할 최소 코딩 규칙을 정의한다.
SOLID 원칙은 참고 수준이며, 명명·포매팅·생성자 주입 세 가지만 강제한다.

## 변경/완화 사항

- 상 등급의 SOLID 5원칙 강제 → 하 등급은 참고만
- 상 등급의 record DTO 권장 → 하 등급에서는 강제하지 않음 (기존 클래스 패턴 유지 허용)
- 상 등급의 테스트 가능 구조 → 하 등급은 기존 테스트가 통과하는 수준으로 완화
- MyBatis / XML Mapper 관련 항목 해당 없음

## 이 등급에서 강제하는 것

**must**
- 생성자 주입만 사용한다. `@RequiredArgsConstructor` 또는 명시적 생성자를 사용한다. `@Autowired` 필드 주입 금지.
- 클래스명은 PascalCase, 메서드·변수명은 camelCase, 상수는 UPPER_SNAKE_CASE를 사용한다.
- 코드 포매팅(들여쓰기 4칸, 중괄호 동일 라인 시작)을 준수한다.
- `import`에 와일드카드(`*`)를 사용하지 않는다.

**should**
- 주석은 "왜(Why)"에 집중한다.
- 메서드 길이는 50줄 이하를 권장한다.

## 예시 코드

생성자 주입 (domain/user/controller/UserController.java:27-33):

```java
// 올바른 예 - @RequiredArgsConstructor 사용
@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
}

// 금지 예
@RestController
public class UserController {
    @Autowired
    private UserService userService;  // 금지
}
```

## 체크리스트

- [ ] `@Autowired` 필드 주입을 사용하지 않는가?
- [ ] 클래스명이 PascalCase, 메서드·변수명이 camelCase인가?
- [ ] import에 와일드카드(`*`)가 없는가?
- [ ] 들여쓰기가 4칸(스페이스)으로 일관되게 적용되었는가?
- [ ] 상수가 UPPER_SNAKE_CASE로 선언되었는가?
