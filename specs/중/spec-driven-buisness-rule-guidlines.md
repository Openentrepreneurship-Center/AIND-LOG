# spec-driven-buisness-rule-guidlines.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

---

## 1. 개요

이 문서는 `com.backend` 도메인 핵심 메서드(인증, 결제, 예약, 권한, 처방)를 구현할 때 RULE-ID 주석 체계를 통해 비즈니스 규칙과 코드 사이의 추적성을 확보하는 방법을 정의한다.

---

## 2. 원칙

- 핵심 비즈니스 메서드는 어떤 규칙을 구현하는지 주석으로 명시한다.
- RULE-ID는 삭제하지 않는다. 폐기 시에만 `@Deprecated` 표기로 유지한다.
- PR 본문에 변경된 RULE-ID를 명시하여 리뷰어가 명세 변경 여부를 확인할 수 있도록 한다.

---

## 3. 강제 사항

### 3-1. RULE-ID 주석

**must**

- 인증, 결제, 예약, 권한, 처방 도메인의 핵심 메서드에는 메서드 주석 블록에 RULE-ID를 명시한다.

  ```java
  /**
   * 사용자 로그인 처리.
   * RULE: USER-LOGIN-001 - 이메일/비밀번호 기반 인증
   * RULE: USER-LOGIN-002 - 계정 잠금 정책 (5회 실패 시 잠금)
   */
  @Transactional(rollbackFor = Exception.class)
  public LoginResponse login(LoginRequest request) { ... }
  ```

- 규칙이 실행되는 코드 라인에 인라인 주석으로 RULE-ID를 표기한다.

  ```java
  // RULE: USER-LOGIN-002 - 계정 잠금 상태 확인
  if (user.isLocked()) {
      throw new BusinessException(ErrorCode.USER_ACCOUNT_LOCKED);
  }
  ```

- RULE-ID 형식은 `{도메인}-{기능}-{순번}`을 따른다. 예: `USER-LOGIN-001`, `PAY-CONFIRM-001`

**must**

- PR 본문에 추가·수정·폐기된 RULE-ID를 명시한다.

  ```
  ## Business Rules
  - 추가: USER-LOGIN-003 - OTP 2차 인증 적용
  - 폐기: APT-BOOK-002 - deprecated (대체: APT-BOOK-005)
  ```

- RULE-ID를 소스에서 삭제하는 것을 금지한다. 폐기 시 아래와 같이 표기한다.

  ```java
  // RULE: APT-BOOK-002 - @Deprecated (APT-BOOK-005로 대체됨, 2025-03-01)
  ```

**should**

- 일반 CRUD 메서드는 RULE-ID 적용에서 제외할 수 있다. 비즈니스 조건이 포함된 경우에는 적용한다.
- PGM-ID(외부 시스템 연동 식별자)는 이 등급에서 의무 적용에서 제외한다.

---

## 4. 코드 예시 (decapet 인용)

### 결제 승인 — RULE-ID 적용

```java
// com.backend.domain.payment.service.PaymentService
/**
 * 결제 승인 처리.
 * RULE: PAY-CONFIRM-001 - 결제 금액 일치 검증
 * RULE: PAY-CONFIRM-002 - 주문 상태 PENDING 검증
 */
@Transactional(rollbackFor = Exception.class)
public void confirmPayment(String paymentKey, String orderId, long amount) {

    Payment payment = paymentRepository.findByOrderId(orderId)
        .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

    // RULE: PAY-CONFIRM-002
    if (payment.getStatus() != PaymentStatus.PENDING) {
        throw new BusinessException(ErrorCode.PAYMENT_NOT_PENDING);
    }

    // RULE: PAY-CONFIRM-001
    if (payment.getAmount() != amount) {
        throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
    }
}
```

### ErrorCode — 비즈니스 규칙 에러 코드

```java
// com.backend.global.error.ErrorCode (일부 발췌)
PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "PM007", "결제 금액이 일치하지 않습니다."),
PAYMENT_NOT_PENDING(HttpStatus.BAD_REQUEST, "PM003", "대기 중인 결제가 아닙니다."),
USER_ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "U012", "계정이 일시적으로 잠겼습니다."),
```

---

## 5. 체크리스트

### RULE-ID 주석
- [ ] 인증/결제/예약/권한/처방 핵심 메서드에 메서드 주석 블록 RULE-ID 기재
- [ ] 규칙 실행 코드 라인에 인라인 RULE-ID 주석 추가
- [ ] RULE-ID 형식 `{도메인}-{기능}-{순번}` 준수

### PR 관리
- [ ] PR 본문 `## Business Rules` 섹션에 변경 RULE-ID 명시
- [ ] RULE-ID 소스 삭제 없음 (폐기 시 `@Deprecated` 표기)

### 코드 품질
- [ ] 핵심 비즈니스 메서드에 RULE-ID 누락 없음
- [ ] 모든 RULE-ID 구현 완료 여부 검토
- [ ] lint 오류 없음
