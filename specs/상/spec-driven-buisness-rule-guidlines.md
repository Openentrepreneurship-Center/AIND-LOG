# spec-driven-buisness-rule-guidlines.md
> 적용 환경: decapet-official/backend (Spring Boot 4.0.1, JPA, Gradle, com.backend)

---

## 1. 개요

이 문서는 `com.backend` 도메인 핵심 로직(인증, 결제, 정산, 권한, 적립금)을 구현할 때 비즈니스 규칙과 코드를 연결하는 주석 표기 규칙을 정의한다. RULE-ID와 PGM-ID 주석 체계를 통해 코드와 기획 명세 사이의 추적성(traceability)을 확보한다.

---

## 2. 원칙

- 코드만 보고는 어떤 비즈니스 규칙을 구현하는지 파악하기 어렵다. RULE-ID 주석이 코드와 명세를 연결하는 유일한 연결고리다.
- RULE-ID는 삭제하지 않는다. 규칙이 폐기된 경우에도 `@Deprecated` 표기와 함께 유지한다.
- RULE-ID 변경은 PR 본문에 반드시 명시하여 리뷰어가 명세 변경 여부를 확인할 수 있도록 한다.
- 비즈니스 규칙 문서(기획서, 요구사항 정의서)와 코드의 RULE-ID 매핑 표를 최신 상태로 유지한다.

---

## 3. 강제 사항

### 3-1. RULE-ID 주석

**must**

- 인증, 결제, 정산, 권한, 적립금 도메인의 핵심 메서드에는 메서드 주석 블록(`/** ... */`)에 RULE-ID를 명시한다.

  ```java
  /**
   * 사용자 로그인 처리.
   * RULE: USER-LOGIN-001 - 이메일/비밀번호 기반 인증
   * RULE: USER-LOGIN-002 - 계정 잠금 정책 (5회 실패 시 잠금)
   * RULE: USER-LOGIN-003 - 승인 대기/거절 사용자 로그인 차단
   */
  @Transactional(rollbackFor = Exception.class)
  public LoginResponse login(LoginRequest request) { ... }
  ```

- 해당 규칙이 실제로 실행되는 코드 라인에 인라인 주석으로 RULE-ID를 재표기한다.

  ```java
  public LoginResponse login(LoginRequest request) {
      User user = userRepository.findByEmail(request.email())
          .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

      // RULE: USER-LOGIN-003 - 승인되지 않은 사용자 접근 차단
      if (user.getStatus() != UserStatus.APPROVED) {
          throw new BusinessException(ErrorCode.USER_NOT_APPROVED);
      }

      // RULE: USER-LOGIN-002 - 계정 잠금 상태 확인
      if (user.isLocked()) {
          throw new BusinessException(ErrorCode.USER_ACCOUNT_LOCKED);
      }

      // RULE: USER-LOGIN-001 - 비밀번호 검증
      if (!passwordEncoder.matches(request.password(), user.getPassword())) {
          // 실패 카운트 증가 로직 ...
          throw new BusinessException(ErrorCode.INVALID_PASSWORD);
      }
      ...
  }
  ```

- RULE-ID 형식은 `{도메인}-{기능}-{순번}` 을 따른다.

  | 도메인 | 접두사 예시 |
  |--------|------------|
  | 사용자 인증 | `USER-LOGIN-` |
  | 결제 | `PAY-CONFIRM-`, `PAY-CANCEL-` |
  | 예약 | `APT-BOOK-`, `APT-APPROVE-` |
  | 권한 | `AUTH-PERM-` |
  | 처방 | `RX-APPROVE-` |

---

### 3-2. PGM-ID 주석

**must**

- 외부 시스템(PG사, SMS, S3 등)과 연동하는 메서드에는 PGM-ID를 메서드 주석에 명시한다.

  ```java
  /**
   * TossPayments 결제 승인 요청.
   * PGM-ID: PAY-TOSS-CONFIRM-001
   * RULE: PAY-CONFIRM-001 - 결제 금액 검증
   * RULE: PAY-CONFIRM-002 - 주문 상태 전이
   */
  public PaymentConfirmResponse confirmPayment(String paymentKey, String orderId, long amount) { ... }
  ```

- PGM-ID는 외부 시스템 명세 또는 연동 계약서의 프로그램 식별자와 일치해야 한다.

---

### 3-3. RULE-ID 변경 정책

**must**

- RULE-ID가 추가·수정·삭제될 때는 PR 본문의 `## Business Rules` 섹션에 변경 내역을 명시한다.

  ```
  ## Business Rules
  - 추가: USER-LOGIN-004 - OTP 2차 인증 적용 (관리자 전용)
  - 수정: PAY-CONFIRM-001 - 결제 금액 허용 범위 변경 (최소 100원)
  - 폐기: APT-BOOK-002 - deprecated (대체: APT-BOOK-005)
  ```

- RULE-ID를 소스에서 삭제하는 것을 금지한다. 규칙이 폐기된 경우 아래와 같이 표기한다.

  ```java
  // RULE: APT-BOOK-002 - @Deprecated (APT-BOOK-005로 대체됨, 2025-03-01)
  ```

---

### 3-4. RULE-ID 매핑 표 유지

**must**

- 프로젝트 문서 저장소에 RULE-ID와 코드 매핑 표를 유지한다. 신규 RULE-ID 추가 시 표를 함께 갱신한다.

  | RULE-ID | 도메인 | 구현 클래스 | 구현 메서드 | 비고 |
  |---------|--------|------------|------------|------|
  | USER-LOGIN-001 | 사용자 | `AuthService` | `login()` | 이메일/비밀번호 인증 |
  | PAY-CONFIRM-001 | 결제 | `PaymentService` | `confirmPayment()` | 금액 검증 |
  | APT-BOOK-001 | 예약 | `AppointmentService` | `createAppointment()` | 중복 예약 방지 |

---

### 3-5. 적용 대상 도메인

**must**

RULE-ID 주석을 반드시 적용해야 하는 도메인 핵심 메서드 목록:

- **인증**: 로그인, 로그아웃, 비밀번호 변경, OTP 검증
- **결제**: 결제 생성, 결제 승인, 결제 취소, 환불
- **예약**: 예약 생성, 예약 승인/거절, 예약 완료 처리
- **권한**: 역할 부여, 권한 체크, 계정 상태 전이
- **처방**: 처방전 승인, 처방전 연동 결제

**should**

- 일반 CRUD(단순 조회·수정)는 RULE-ID 주석 의무 대상에서 제외할 수 있다. 단, 비즈니스 조건이 포함된 경우 적용한다.

---

## 4. 코드 예시 (decapet 인용)

### 결제 승인 — 다중 RULE-ID 적용

```java
// com.backend.domain.payment.service.PaymentService
/**
 * 결제 승인 처리.
 * RULE: PAY-CONFIRM-001 - 결제 금액 일치 검증
 * RULE: PAY-CONFIRM-002 - 주문 상태 PENDING 검증
 * RULE: PAY-CONFIRM-003 - 재고 차감 원자적 처리
 * PGM-ID: PAY-TOSS-CONFIRM-001
 */
@Transactional(rollbackFor = Exception.class)
public void confirmPayment(String paymentKey, String orderId, long amount) {

    Payment payment = paymentRepository.findByOrderId(orderId)
        .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

    // RULE: PAY-CONFIRM-002 - 대기 중인 결제만 승인 가능
    if (payment.getStatus() != PaymentStatus.PENDING) {
        throw new BusinessException(ErrorCode.PAYMENT_NOT_PENDING);
    }

    // RULE: PAY-CONFIRM-001 - 요청 금액과 저장 금액 일치 확인
    if (payment.getAmount() != amount) {
        throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
    }

    // RULE: PAY-CONFIRM-003 - PG사 승인 및 재고 처리
    // PGM-ID: PAY-TOSS-CONFIRM-001
    tossPaymentsClient.confirm(paymentKey, orderId, amount);
    ...
}
```

### 예약 생성 — 중복 방지

```java
// com.backend.domain.appointment.service.AppointmentService
/**
 * 예약 생성.
 * RULE: APT-BOOK-001 - 동일 시간대 중복 예약 방지
 * RULE: APT-BOOK-002 - 승인된 사용자만 예약 가능
 */
@Transactional(rollbackFor = Exception.class)
public void createAppointment(String userId, CreateAppointmentRequest request) {
    // RULE: APT-BOOK-002 - 예약 권한 확인
    if (!user.hasPermission(Permission.APPOINTMENT_BOOKING)) {
        throw new BusinessException(ErrorCode.APPOINTMENT_BOOKING_NOT_ALLOWED);
    }

    // RULE: APT-BOOK-001 - 동일 시간대 중복 확인
    boolean duplicate = appointmentRepository.existsByUserIdAndTimeSlotId(userId, request.timeSlotId());
    if (duplicate) {
        throw new BusinessException(ErrorCode.DUPLICATE_USER_APPOINTMENT);
    }
    ...
}
```

### ErrorCode — 비즈니스 규칙과 매핑된 에러 코드

```java
// com.backend.global.error.ErrorCode (일부 발췌)
PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "PM007", "결제 금액이 일치하지 않습니다."),
PAYMENT_NOT_PENDING(HttpStatus.BAD_REQUEST, "PM003", "대기 중인 결제가 아닙니다."),
DUPLICATE_USER_APPOINTMENT(HttpStatus.CONFLICT, "AP013", "이미 해당 시간대에 예약이 있습니다."),
USER_ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "U012", "계정이 일시적으로 잠겼습니다."),
```

---

## 5. 체크리스트

### RULE-ID 주석
- [ ] 인증/결제/예약/권한/처방 핵심 메서드에 메서드 주석 블록에 RULE-ID 기재
- [ ] 규칙이 실행되는 코드 라인에 인라인 RULE-ID 주석 추가
- [ ] RULE-ID 형식 `{도메인}-{기능}-{순번}` 준수

### PGM-ID 주석
- [ ] 외부 시스템 연동 메서드에 PGM-ID 기재
- [ ] PGM-ID가 외부 시스템 명세와 일치

### 변경 관리
- [ ] PR 본문 `## Business Rules` 섹션에 추가/수정/폐기 RULE-ID 명시
- [ ] RULE-ID 소스에서 삭제 없음 (폐기 시 `@Deprecated` 표기)
- [ ] RULE-ID 매핑 표 최신 상태 유지

### 코드 품질
- [ ] RULE-ID가 없는 핵심 비즈니스 메서드 없음
- [ ] 모든 RULE-ID에 대한 구현 완료 여부 검토
- [ ] lint 오류 없음
