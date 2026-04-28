#quality-rule.md
## 1. Controller 보안 규칙

### 입력 검증
- `@Validated`, `@Valid`로 모든 DTO 및 파라미터 검증
- `@NotNull`, `@Pattern` 등 제약 어노테이션 활용 (예: 카드번호 정규식 검증)
- PathVariable, RequestParam에도 정규식/NotNull 적용

### 예외 처리
- `@ControllerAdvice` + `@ExceptionHandler`로 글로벌 예외 처리
- 표준 응답 포맷(`ApiResponse.error`) 사용 → 내부 스택 정보 노출 금지
- 적절한 HTTP 상태코드 반환 (404, 400, 500 등)

### 인증/인가
- JWT 기반 `@AuthenticationPrincipal` 활용하여 사용자 범위 제한
- 컨트롤러 호출 시 membId 등 사용자 식별자를 Service로 전달

### 로깅 보안
- 구조화된 로깅 적용
- 민감정보(cardNumber, email 등)는 마스킹 후 기록
- 비밀번호/토큰 등은 절대 로그 남기지 않음
 
## 2. Service 보안 규칙

### 도메인 검증
- 카드/사용자 존재 여부 확인
- 상태(활성/비활성), 잔액, 일일 한도 등 도메인 규칙 검증
- 중복 값(CardNumber 등) 체크

### 트랜잭션 안전성
- 서비스 계층은 `@Transactional` 기본 적용
- 조회용 메서드는 `@Transactional(readOnly = true)`
- `rollbackFor` 설정으로 일관된 롤백 보장
- 감사/알림은 `Propagation.REQUIRES_NEW`로 별도 트랜잭션

### 예외 처리
- `BusinessException` + `errorCode` 기반 커스텀 예외 사용
- 내부 Exception은 wrapping 후 변환 (`CardServiceException`, `TransferException` 등)
- 사용자 친화적 메시지 제공, 내부 구현 세부정보 차단

### 로깅/감사
- 카드번호 등 민감정보는 마스킹 후 로깅
- 거래 기록/감사 로그는 반드시 영속화하여 추적성 확보
 
## 3. Mapper 보안 규칙

### 파라미터 바인딩
- 항상 `#{...}` 사용, `${...}` 금지 (SQL 인젝션 방지)
- 다중 파라미터는 `@Param`으로 명시
- IN 절은 `<foreach> #{id} </foreach>`로 안전하게 처리

### 안전한 쿼리 작성
- `SELECT *` 금지 → 필요한 컬럼만 명시
- ResultMap 사용으로 노출 데이터 통제
- 동적 정렬은 `<choose>` 문으로 화이트리스트 적용

### 데이터 접근 제한
- WHERE 절에 사용자/테넌트 경계 조건 포함 (`WHERE user_id = #{userId}`)
- 페이징 쿼리 적용, 무제한 조회 금지

### 배치/삭제 쿼리
- 대량 UPDATE/DELETE 시 범위 조건(`WHERE IN (...)`) 필수
- Soft Delete 적용 권장

### 캐시 주의
- 민감 데이터는 2차 캐시에 저장하지 않음
- TTL/무효화 전략을 반드시 정의

### 타입 안정성
- Enum/TypeHandler 사용하여 코드값 안전하게 매핑
 
## 4. MyBatis Query 보안 규칙

### 바인딩 원칙
- 모든 입력값은 `#{...}` 바인딩
- `${...}` 사용 금지 (테이블명, 컬럼명 포함)

### 동적 SQL
- 동적 정렬/컬럼명은 화이트리스트 기반 `<choose>` 처리
- 동적 테이블명은 허용된 값만 enum/검증 후 매핑

### 조회 제한
- WHERE 절에 상태/삭제여부 조건 추가 (`status != 'DELETED'`, `del_yn = 'N'`)
- LIMIT 적용으로 과도한 결과 노출 방지

### 배치/페이징
- 페이징은 `LIMIT #{limit} OFFSET #{offset}` → 정수 바인딩
- 다중 UPDATE/DELETE 시 WHERE 절 필수, 전체 테이블 조작 금지

### 결과 매핑
- ResultMap과 명시적 컬럼만 사용
- PII/민감 컬럼은 기본적으로 제외

### 공통 코드/CTE
- `${table}` 형태는 사용 금지, 반드시 고정 선택지로 제한
- `#{langCd}` 등 코드값은 상위 레이어에서 화이트리스트 검증
 
## ✅ 보안 체크리스트

### Controller
- [ ] 입력값 모두 검증 (`@Valid`, `@Pattern` 등)
- [ ] 예외 발생 시 표준 에러 응답 반환
- [ ] JWT 기반 사용자 범위 제한 적용
- [ ] 로그에 민감정보 기록 금지/마스킹

### Service
- [ ] 도메인 검증 (잔액/한도/상태 등)
- [ ] 트랜잭션 롤백 정책 정의
- [ ] 감사 로그 별도 트랜잭션으로 처리
- [ ] 비즈니스 예외 정의 및 사용자 친화적 메시지 제공

### Mapper
- [ ] `#{...}` 바인딩만 사용, `${...}` 금지
- [ ] ResultMap 사용 및 필요한 컬럼만 조회
- [ ] WHERE 조건으로 사용자 경계 제한
- [ ] 배치/삭제 쿼리에는 범위 조건 필수

### Query
- [ ] 동적 정렬/컬럼명은 화이트리스트만 허용
- [ ] 무제한 조회 금지, 페이징 적용
- [ ] Soft Delete 적용 권장
- [ ] 공통 코드 조인 시 테이블명/코드값 검증